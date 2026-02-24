package me.cxdev.commerce.proxy.handler;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import de.hybris.platform.core.MasterTenant;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.core.TenantListener;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import me.cxdev.commerce.proxy.livecycle.ProxyLocalRouteHandler;

/**
 * Intercepts incoming requests while the SAP Commerce server is still in its startup phase.
 * <p>
 * This handler acts as a {@link TenantListener} to monitor the lifecycle of the 'master' tenant.
 * As long as the master tenant is not fully started, this handler intercepts all traffic and
 * serves an auto-refreshing "503 Service Unavailable" maintenance page using native Java ResourceBundles.
 * </p>
 */
public class StartupPageHandler implements ProxyLocalRouteHandler, TenantListener, InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(StartupPageHandler.class);
	private static final String BUNDLE_BASE_NAME = "localization/cxdevproxy-locales";

	// volatile ensures thread visibility between Hybris startup threads and Undertow worker threads
	private volatile boolean masterTenantReady = false;

	@Override
	public void afterPropertiesSet() {
		Registry.registerTenantListener(this);
	}

	@Override
	public void afterTenantStartUp(Tenant tenant) {
		if (MasterTenant.getInstance().equals(tenant)) {
			LOG.info("Master tenant has started. Proxy is now routing traffic.");
			this.masterTenantReady = true;
		}
	}

	@Override
	public void beforeTenantShutDown(Tenant tenant) {
		if (MasterTenant.getInstance().equals(tenant)) {
			LOG.info("Master tenant is shutting down. Proxy will block traffic.");
			this.masterTenantReady = false;
		}
	}

	@Override
	public void afterSetActivateSession(Tenant tenant) {
	}

	@Override
	public void beforeUnsetActivateSession(Tenant tenant) {
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		return !masterTenantReady;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) {
		exchange.setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html; charset=UTF-8");

		Locale requestLocale = determineLocale(exchange);
		String title = "Starting up...";
		String message = "SAP Commerce is currently starting. Please wait...";

		try {
			// Loads the message bundle natively from the classpath, bypassing the Hybris DB
			ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, requestLocale);
			title = bundle.getString("cxdevproxy.startup.page.title");
			message = bundle.getString("cxdevproxy.startup.page.message");
		} catch (MissingResourceException e) {
			LOG.warn("Could not find message bundle '{}' or keys for locale '{}'. Falling back to default text.", BUNDLE_BASE_NAME, requestLocale);
		}

		String html = String.format(
				"<!DOCTYPE html>" +
						"<html lang=\"en\">" +
						"<head>" +
						"<meta charset=\"UTF-8\">" +
						"<title>%s</title>" +
						"<meta http-equiv=\"refresh\" content=\"5\">" +
						"<style>" +
						"body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; " +
						"text-align: center; padding-top: 10%%; background-color: #f4f4f9; color: #333; }" +
						".spinner { margin: 20px auto; width: 40px; height: 40px; border: 4px solid rgba(0,0,0,0.1); " +
						"border-left-color: #0056b3; border-radius: 50%%; animation: spin 1s linear infinite; }" +
						"@keyframes spin { 0%% { transform: rotate(0deg); } 100%% { transform: rotate(360deg); } }" +
						"</style>" +
						"</head>" +
						"<body>" +
						"<h1>%s</h1>" +
						"<p>%s</p>" +
						"<div class=\"spinner\"></div>" +
						"</body>" +
						"</html>",
				title, title, message);

		exchange.getResponseSender().send(html);
	}

	/**
	 * Determines the preferred locale from the browser's Accept-Language header.
	 * Supports English and German. Defaults to English.
	 */
	private Locale determineLocale(HttpServerExchange exchange) {
		String acceptLanguage = exchange.getRequestHeaders().getFirst(Headers.ACCEPT_LANGUAGE);
		if (StringUtils.isNotBlank(acceptLanguage)) {
			return Locale.forLanguageTag(acceptLanguage.toLowerCase());
		}
		return Locale.ENGLISH;
	}
}
