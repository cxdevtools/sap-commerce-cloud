package me.cxdev.commerce.proxy.condition;

import io.undertow.server.HttpServerExchange;

import org.apache.commons.lang3.StringUtils;

/**
 * Condition that matches if a specific cookie is present in the request.
 * Useful for routing based on feature toggles, A/B tests, or specific mock users.
 */
public class CookieExistsCondition implements ExchangeCondition {
	private String cookieName;

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (StringUtils.isBlank(cookieName)) {
			return false;
		}
		return exchange.getRequestCookie(cookieName) != null;
	}

	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}
}
