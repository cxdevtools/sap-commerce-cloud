package me.cxdev.commerce.proxy.condition;

import io.undertow.server.HttpServerExchange;

/**
 * Represents a condition that evaluates an incoming HTTP request.
 * Used to determine if a specific proxy handler should be executed.
 */
public interface ExchangeCondition {
	/**
	 * Evaluates the condition against the current HTTP exchange.
	 *
	 * @param exchange The current Undertow HTTP server exchange.
	 * @return {@code true} if the condition is met, {@code false} otherwise.
	 */
	boolean matches(HttpServerExchange exchange);
}
