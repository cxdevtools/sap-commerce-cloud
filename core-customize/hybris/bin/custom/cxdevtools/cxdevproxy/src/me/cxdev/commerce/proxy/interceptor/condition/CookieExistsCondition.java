package me.cxdev.commerce.proxy.interceptor.condition;

import io.undertow.server.HttpServerExchange;

import org.apache.commons.lang3.StringUtils;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * Condition that matches if a specific cookie is present in the request.
 * Useful for routing based on feature toggles, A/B tests, or specific mock users.
 */
class CookieExistsCondition implements ProxyExchangeInterceptorCondition {
	private final String cookieName;

	CookieExistsCondition(String cookieName) {
		this.cookieName = cookieName;
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (StringUtils.isBlank(cookieName)) {
			return false;
		}
		return exchange.getRequestCookie(cookieName) != null;
	}
}
