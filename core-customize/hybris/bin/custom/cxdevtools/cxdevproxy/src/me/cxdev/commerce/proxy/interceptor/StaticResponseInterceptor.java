package me.cxdev.commerce.proxy.interceptor;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import org.apache.commons.lang3.StringUtils;

/**
 * Short-circuits the request and returns a predefined status code and payload.
 * Useful for mocking endpoints that do not yet exist in the backend API,
 * or for simulating specific error states (e.g., forcing a 500 Internal Server Error).
 */
class StaticResponseInterceptor implements ProxyExchangeInterceptor {
	private int statusCode;
	private String contentType;
	private String responseBody;

	StaticResponseInterceptor(int statusCode, String contentType, String responseBody) {
		assert StringUtils.isNotBlank(contentType);
		assert responseBody != null;

		this.statusCode = statusCode;
		this.contentType = contentType;
		this.responseBody = responseBody;
	}

	@Override
	public void apply(HttpServerExchange exchange) {
		exchange.setStatusCode(statusCode);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);

		// Sending the response and ending the exchange prevents further routing to the backend
		exchange.getResponseSender().send(responseBody);
		exchange.endExchange();
	}
}
