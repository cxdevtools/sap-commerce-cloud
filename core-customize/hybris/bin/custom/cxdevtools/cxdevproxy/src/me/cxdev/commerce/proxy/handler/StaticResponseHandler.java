package me.cxdev.commerce.proxy.handler;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import me.cxdev.commerce.proxy.livecycle.ProxyHttpServerExchangeHandler;

/**
 * Short-circuits the request and returns a predefined status code and payload.
 * Useful for mocking endpoints that do not yet exist in the backend API,
 * or for simulating specific error states (e.g., forcing a 500 Internal Server Error).
 */
public class StaticResponseHandler implements ProxyHttpServerExchangeHandler {
	private int statusCode = 200;
	private String contentType = "application/json";
	private String responseBody = "{}";

	@Override
	public void apply(HttpServerExchange exchange) {
		exchange.setStatusCode(statusCode);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);

		// Sending the response and ending the exchange prevents further routing to the backend
		exchange.getResponseSender().send(responseBody);
		exchange.endExchange();
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
}
