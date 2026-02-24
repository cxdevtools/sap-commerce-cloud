package me.cxdev.commerce.proxy.condition;

import io.undertow.server.HttpServerExchange;

import org.apache.commons.lang3.StringUtils;

/**
 * Condition that matches if the HTTP request method (e.g., GET, POST)
 * equals the configured method.
 */
public class HttpMethodCondition implements ExchangeCondition {
	private String method;

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (StringUtils.isBlank(method)) {
			return false;
		}
		return exchange.getRequestMethod().toString().equalsIgnoreCase(method);
	}

	public void setMethod(String method) {
		this.method = method;
	}
}
