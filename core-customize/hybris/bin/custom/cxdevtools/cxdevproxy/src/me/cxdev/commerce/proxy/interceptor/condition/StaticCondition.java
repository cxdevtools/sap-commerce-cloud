package me.cxdev.commerce.proxy.interceptor.condition;

import io.undertow.server.HttpServerExchange;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * A static condition representing a fixed boolean value.
 * Only use the static constants ALWAYS and NEVER.
 */
class StaticCondition implements ProxyExchangeInterceptorCondition {
	static final StaticCondition ALWAYS = new StaticCondition(true);
	static final StaticCondition NEVER = new StaticCondition(false);

	private final boolean value;

	private StaticCondition(boolean value) {
		this.value = value;
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		return value;
	}
}
