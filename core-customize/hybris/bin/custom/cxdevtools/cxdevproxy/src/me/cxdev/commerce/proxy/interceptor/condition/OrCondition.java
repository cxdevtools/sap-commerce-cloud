package me.cxdev.commerce.proxy.interceptor.condition;

import java.util.Arrays;
import java.util.List;

import io.undertow.server.HttpServerExchange;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * A logical OR condition that evaluates to true if at least one
 * of its underlying conditions matches.
 */
class OrCondition implements ProxyExchangeInterceptorCondition {
	private final List<ProxyExchangeInterceptorCondition> conditions;

	OrCondition(ProxyExchangeInterceptorCondition[] conditions) {
		this.conditions = Arrays.asList(conditions);
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (conditions == null || conditions.isEmpty()) {
			return false;
		}
		return conditions.stream().anyMatch(c -> c.matches(exchange));
	}
}
