package me.cxdev.commerce.proxy.livecycle;

import io.undertow.server.HttpServerExchange;

/**
 * Interface for handling requests directly within the Undertow proxy,
 * bypassing the standard routing to the frontend or backend.
 * Useful for serving local HTML pages or mocking endpoints.
 */
public interface ProxyLocalRouteHandler {
	/**
	 * Determines if this handler is responsible for the current request.
	 *
	 * @param exchange the current HTTP server exchange
	 * @return true if this handler should process the request, false otherwise
	 */
	boolean matches(HttpServerExchange exchange);

	/**
	 * Processes the request and sends a direct response to the client.
	 *
	 * @param exchange the current HTTP server exchange
	 * @throws Exception if an error occurs during processing
	 */
	void handleRequest(HttpServerExchange exchange) throws Exception;
}
