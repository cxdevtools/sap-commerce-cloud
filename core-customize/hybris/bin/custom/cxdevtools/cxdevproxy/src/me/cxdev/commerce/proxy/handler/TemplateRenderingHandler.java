package me.cxdev.commerce.proxy.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hybris.platform.servicelayer.config.ConfigurationService;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.Methods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

import me.cxdev.commerce.proxy.util.ResourcePathUtils;

/**
 * Intercepts requests for local HTML files, resolves Spring properties (${...})
 * and i18n message bundles (#{...}), and serves the rendered HTML to the browser.
 */
public class TemplateRenderingHandler implements ProxyRouteHandler, ResourceLoaderAware {
	private static final Logger LOG = LoggerFactory.getLogger(TemplateRenderingHandler.class);
	private static final Pattern PROPERTY_PATTERN = Pattern.compile("%\\{([^}]+)\\}");
	private static final Pattern I18N_PATTERN = Pattern.compile("#\\{([^}]+)\\}");

	private final String baseLocation;
	private final ConfigurationService configurationService;
	private final MessageSource messageSource;
	private ResourceLoader resourceLoader;

	public TemplateRenderingHandler(
			String baseLocation,
			ConfigurationService configurationService,
			MessageSource messageSource) {
		this.baseLocation = ResourcePathUtils.normalizeDirectoryPath(baseLocation, "UI base location");
		this.configurationService = configurationService;
		this.messageSource = messageSource;
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (!Methods.GET.equals(exchange.getRequestMethod())) {
			return false;
		}

		String path = exchange.getRequestPath();
		if (!path.endsWith(".html")) {
			return false;
		}

		Resource resource = resourceLoader.getResource(baseLocation + path);
		return resource.exists() && resource.isReadable();
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) {
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}

		String path = exchange.getRequestPath();
		String fullLocation = baseLocation + path;
		Resource resource = resourceLoader.getResource(fullLocation);

		if (!resource.exists()) {
			LOG.error("Template suddenly not found at: {}", fullLocation);
			exchange.setStatusCode(404);
			exchange.getResponseSender().send("404 - Template not found");
			return;
		}

		try (InputStream is = resource.getInputStream()) {
			String rawHtml = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
			String propertiesResolvedHtml = resolveProperties(rawHtml);
			Locale userLocale = determineLocale(exchange);
			String fullyRenderedHtml = resolveI18nMessages(propertiesResolvedHtml, userLocale);

			exchange.setStatusCode(200);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html; charset=UTF-8");
			exchange.getResponseSender().send(fullyRenderedHtml);

		} catch (IOException e) {
			LOG.error("Error reading template file: {}", fullLocation, e);
			exchange.setStatusCode(500);
			exchange.getResponseSender().send("500 - Internal Server Error rendering template");
		}
	}

	/**
	 * Resolves custom property placeholders in the format %{property.key:defaultValue}.
	 * This custom syntax prevents collisions with JavaScript template literals (${...}).
	 */
	private String resolveProperties(String template) {
		if (template == null || !template.contains("%{")) {
			return template;
		}

		Matcher matcher = PROPERTY_PATTERN.matcher(template);
		StringBuilder sb = new StringBuilder();

		while (matcher.find()) {
			String expression = matcher.group(1);
			String key = expression;
			String defaultValue = null;

			int colonIndex = expression.indexOf(':');
			if (colonIndex != -1) {
				key = expression.substring(0, colonIndex);
				defaultValue = expression.substring(colonIndex + 1);
			}

			String resolvedValue = configurationService.getConfiguration().getString(key, defaultValue);
			if (resolvedValue == null) {
				resolvedValue = key;
			}
			matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(resolvedValue));
		}
		matcher.appendTail(sb);

		return sb.toString();
	}

	private String resolveI18nMessages(String html, Locale locale) {
		Matcher matcher = I18N_PATTERN.matcher(html);
		StringBuilder sb = new StringBuilder();

		while (matcher.find()) {
			String matchContent = matcher.group(1);
			String key = matchContent;
			String defaultValue = key;

			int defaultSeparatorIndex = matchContent.indexOf(':');
			if (defaultSeparatorIndex != -1) {
				key = matchContent.substring(0, defaultSeparatorIndex);
				defaultValue = matchContent.substring(defaultSeparatorIndex + 1);
			}

			String resolvedMessage = messageSource.getMessage(key, null, defaultValue, locale);
			matcher.appendReplacement(sb, Matcher.quoteReplacement(resolvedMessage));
		}
		matcher.appendTail(sb);

		return sb.toString();
	}

	private Locale determineLocale(HttpServerExchange exchange) {
		String acceptLanguage = exchange.getRequestHeaders().getFirst(Headers.ACCEPT_LANGUAGE);
		if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
			String primaryTag = acceptLanguage.split(",")[0].trim();
			try {
				return Locale.forLanguageTag(primaryTag);
			} catch (Exception e) {
				LOG.trace("Could not parse language tag: {}", primaryTag);
			}
		}
		return Locale.ENGLISH;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
}
