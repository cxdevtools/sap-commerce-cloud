package me.cxdev.commerce.proxy.handler;

import de.hybris.platform.core.Registry;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.cxdev.commerce.proxy.livecycle.ProxyLocalRouteHandler;

/**
 * A local route handler that serves static files directly from the extension's classpath.
 * <p>
 * It looks for files within the {@code resources/static-content} directory. If a requested
 * file exists locally, this handler intercepts the request and bypasses the standard
 * frontend or backend proxy routing.
 * </p>
 */
public class StaticContentHandler implements ProxyLocalRouteHandler {
	private static final Logger LOG = LoggerFactory.getLogger(StaticContentHandler.class);
	private static final String STATIC_FOLDER = "cxdevproxy/static-content";

	private final ResourceManager resourceManager;
	private final ResourceHandler resourceHandler;

	/**
	 * Initializes the static content handler.
	 * Sets up Undertow's native resource management using the current classloader.
	 */
	public StaticContentHandler() {
		// Uses the extension's classloader to resolve files from the resources folder
		this.resourceManager = new ClassPathResourceManager(Registry.class.getClassLoader(), STATIC_FOLDER);

		// Undertow's native handler for serving static files securely and efficiently
		this.resourceHandler = new ResourceHandler(this.resourceManager);
	}

	/**
	 * Evaluates whether the incoming request targets an existing static file.
	 *
	 * @param exchange The current HTTP server exchange.
	 * @return {@code true} if the requested path matches an existing file in the static folder, {@code false} otherwise.
	 */
	@Override
	public boolean matches(HttpServerExchange exchange) {
		try {
			String path = exchange.getRequestPath();
			Resource resource = resourceManager.getResource(path);

			// Match only if the resource actually exists and is a file (not a directory)
			return resource != null && !resource.isDirectory();
		} catch (Exception e) {
			LOG.error("Error checking for static resource: {}", exchange.getRequestPath(), e);
			return false;
		}
	}

	/**
	 * Serves the matched static file to the client.
	 *
	 * @param exchange The current HTTP server exchange.
	 * @throws Exception If an error occurs while reading or writing the file.
	 */
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		// Delegate the actual file serving (MIME types, caching headers, etc.) to Undertow
		resourceHandler.handleRequest(exchange);
	}
}
