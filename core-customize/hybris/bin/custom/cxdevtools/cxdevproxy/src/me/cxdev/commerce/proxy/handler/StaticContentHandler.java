package me.cxdev.commerce.proxy.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.Methods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

import me.cxdev.commerce.proxy.util.ResourcePathUtils;

/**
 * Serves static assets (CSS, JS, images, fonts) from the configured base location.
 * Must be registered after the TemplateRenderingHandler to ensure HTML files
 * are interpolated before this handler attempts to serve them as raw bytes.
 */
public class StaticContentHandler implements ProxyRouteHandler, ResourceLoaderAware {
	private static final Logger LOG = LoggerFactory.getLogger(StaticContentHandler.class);

	private final String baseLocation;
	private ResourceLoader resourceLoader;

	// A lightweight MIME type map for the most common web assets
	private static final Map<String, String> MIME_TYPES = new HashMap<>();
	static {
		MIME_TYPES.put("css", "text/css");
		MIME_TYPES.put("js", "application/javascript");
		MIME_TYPES.put("json", "application/json");
		MIME_TYPES.put("png", "image/png");
		MIME_TYPES.put("jpg", "image/jpeg");
		MIME_TYPES.put("jpeg", "image/jpeg");
		MIME_TYPES.put("svg", "image/svg+xml");
		MIME_TYPES.put("ico", "image/x-icon");
		MIME_TYPES.put("woff", "font/woff");
		MIME_TYPES.put("woff2", "font/woff2");
		MIME_TYPES.put("ttf", "font/ttf");
	}

	public StaticContentHandler(String baseLocation) {
		this.baseLocation = ResourcePathUtils.normalizeDirectoryPath(baseLocation, "UI base location");
		;
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (!Methods.GET.equals(exchange.getRequestMethod())) {
			return false;
		}

		String path = exchange.getRequestPath();
		if ("/".equals(path)) {
			return false;
		}

		Resource resource = resourceLoader.getResource(baseLocation + path);
		// isReadable() ensures we don't accidentally match directories
		return resource.exists() && resource.isReadable();
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) {
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}

		String path = exchange.getRequestPath();
		Resource resource = resourceLoader.getResource(baseLocation + path);

		if (!resource.exists() || !resource.isReadable()) {
			LOG.warn("Static resource matched but could not be read: {}", path);
			exchange.setStatusCode(404);
			return;
		}

		String extension = getExtension(path);
		String mimeType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");

		exchange.setStatusCode(200);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, mimeType);

		// Undertow requires starting blocking mode before writing to the raw OutputStream
		exchange.startBlocking();

		try (InputStream is = resource.getInputStream()) {
			StreamUtils.copy(is, exchange.getOutputStream());
		} catch (IOException e) {
			LOG.error("Error serving static file: {}", path, e);
			if (!exchange.isResponseStarted()) {
				exchange.setStatusCode(500);
			}
		}
	}

	private String getExtension(String path) {
		int lastDot = path.lastIndexOf('.');
		if (lastDot != -1 && lastDot < path.length() - 1) {
			return path.substring(lastDot + 1).toLowerCase();
		}
		return "";
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
}
