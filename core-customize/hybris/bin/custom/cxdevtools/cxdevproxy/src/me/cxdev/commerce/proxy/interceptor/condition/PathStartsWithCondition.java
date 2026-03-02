package me.cxdev.commerce.proxy.interceptor.condition;

import io.undertow.server.HttpServerExchange;

import org.apache.commons.lang3.StringUtils;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * Condition that matches if the request path starts with a specific prefix.
 */
class PathStartsWithCondition implements ProxyExchangeInterceptorCondition {
	private String prefix;

	PathStartsWithCondition(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (StringUtils.isBlank(prefix)) {
			return false;
		}
		return exchange.getRequestPath().startsWith(prefix);
	}
}
