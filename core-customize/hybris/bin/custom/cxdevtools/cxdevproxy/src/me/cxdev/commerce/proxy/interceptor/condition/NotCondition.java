package me.cxdev.commerce.proxy.interceptor.condition;

import io.undertow.server.HttpServerExchange;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * A logical NOT condition that negates the result of a single underlying condition.
 */
class NotCondition implements ProxyExchangeInterceptorCondition {
	private final ProxyExchangeInterceptorCondition condition;

	NotCondition(ProxyExchangeInterceptorCondition condition) {
		this.condition = condition;
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (condition == null) {
			return false; // Fail-safe if not properly configured
		}
		return !condition.matches(exchange);
	}
}
