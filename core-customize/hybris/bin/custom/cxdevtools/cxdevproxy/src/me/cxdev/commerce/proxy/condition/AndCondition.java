package me.cxdev.commerce.proxy.condition;

import java.util.List;

import io.undertow.server.HttpServerExchange;

/**
 * A logical AND condition that evaluates to true only if all
 * of its underlying conditions match.
 */
public class AndCondition implements ExchangeCondition {
	private List<ExchangeCondition> conditions;

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (conditions == null || conditions.isEmpty()) {
			return false;
		}
		return conditions.stream().allMatch(c -> c.matches(exchange));
	}

	public void setConditions(List<ExchangeCondition> conditions) {
		this.conditions = conditions;
	}
}
