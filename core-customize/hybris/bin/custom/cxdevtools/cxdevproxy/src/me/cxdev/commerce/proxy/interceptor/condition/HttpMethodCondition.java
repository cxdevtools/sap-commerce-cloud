package me.cxdev.commerce.proxy.interceptor.condition;

import io.undertow.server.HttpServerExchange;

import org.apache.commons.lang3.StringUtils;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * Condition that matches if the HTTP request method (e.g., GET, POST)
 * equals the configured method.
 */
class HttpMethodCondition implements ProxyExchangeInterceptorCondition {
	private final String method;

	HttpMethodCondition(String method) {
		this.method = method;
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (StringUtils.isBlank(method)) {
			return false;
		}
		return exchange.getRequestMethod().toString().equalsIgnoreCase(method);
	}
}
