package me.cxdev.commerce.proxy.condition;

import io.undertow.server.HttpServerExchange;

/**
 * A logical NOT condition that negates the result of a single underlying condition.
 */
public class NotCondition implements ExchangeCondition {
	private ExchangeCondition condition;

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (condition == null) {
			return false; // Fail-safe if not properly configured
		}
		return !condition.matches(exchange);
	}

	public void setCondition(ExchangeCondition condition) {
		this.condition = condition;
	}
}
