package me.cxdev.commerce.proxy.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * Interface for handling requests directly within the Undertow proxy,
 * bypassing the standard routing to the frontend or backend.
 * Useful for serving local HTML pages or mocking endpoints.
 */
public interface ProxyRouteHandler extends HttpHandler {
	/**
	 * Determines if this handler is responsible for the current request.
	 *
	 * @param exchange the current HTTP server exchange
	 * @return true if this handler should process the request, false otherwise
	 */
	boolean matches(HttpServerExchange exchange);
}
