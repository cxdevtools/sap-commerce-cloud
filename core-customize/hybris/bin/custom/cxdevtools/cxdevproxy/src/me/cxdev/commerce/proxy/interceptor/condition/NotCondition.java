package me.cxdev.commerce.proxy.interceptor.condition;

import io.undertow.server.HttpServerExchange;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * A logical NOT condition that negates the result of a single underlying condition.
 */
class NotCondition implements ProxyExchangeInterceptorCondition {
	private final ProxyExchangeInterceptorCondition condition;

	NotCondition(ProxyExchangeInterceptorCondition condition) {
		assert condition != null;
		this.condition = condition;
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		return !condition.matches(exchange);
	}
}
