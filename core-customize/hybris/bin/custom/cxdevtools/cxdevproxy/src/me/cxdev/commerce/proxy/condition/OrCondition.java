package me.cxdev.commerce.proxy.condition;

import java.util.List;

import io.undertow.server.HttpServerExchange;

/**
 * A logical OR condition that evaluates to true if at least one
 * of its underlying conditions matches.
 */
public class OrCondition implements ExchangeCondition {
	private List<ExchangeCondition> conditions;

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (conditions == null || conditions.isEmpty()) {
			return false;
		}
		return conditions.stream().anyMatch(c -> c.matches(exchange));
	}

	public void setConditions(List<ExchangeCondition> conditions) {
		this.conditions = conditions;
	}
}
