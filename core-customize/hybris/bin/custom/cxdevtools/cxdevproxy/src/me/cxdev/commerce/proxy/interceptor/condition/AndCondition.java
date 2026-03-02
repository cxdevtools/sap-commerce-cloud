package me.cxdev.commerce.proxy.interceptor.condition;

import java.util.Arrays;
import java.util.List;

import io.undertow.server.HttpServerExchange;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * A logical AND condition that evaluates to true only if all
 * of its underlying conditions match.
 */
class AndCondition implements ProxyExchangeInterceptorCondition {
	private final List<ProxyExchangeInterceptorCondition> conditions;

	AndCondition(ProxyExchangeInterceptorCondition[] conditions) {
		this.conditions = Arrays.asList(conditions);
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (conditions == null || conditions.isEmpty()) {
			return false;
		}
		return conditions.stream().allMatch(c -> c.matches(exchange));
	}
}
