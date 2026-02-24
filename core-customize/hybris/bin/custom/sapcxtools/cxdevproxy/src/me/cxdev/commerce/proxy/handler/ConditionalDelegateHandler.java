package me.cxdev.commerce.proxy.handler;

import java.util.List;

import io.undertow.server.HttpServerExchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.cxdev.commerce.proxy.condition.ExchangeCondition;
import me.cxdev.commerce.proxy.livecycle.ProxyHttpServerExchangeHandler;

/**
 * A composite handler that delegates execution to a list of underlying handlers
 * only if a configured set of conditions is met.
 * <p>
 * By default, ALL conditions must evaluate to {@code true} (AND logic).
 * This can be changed to OR logic by setting {@code requireAllConditions} to {@code false}.
 * </p>
 */
public class ConditionalDelegateHandler implements ProxyHttpServerExchangeHandler {
	private static final Logger LOG = LoggerFactory.getLogger(ConditionalDelegateHandler.class);

	private List<ExchangeCondition> conditions;
	private List<ProxyHttpServerExchangeHandler> delegates;

	// If true, acts as AND. If false, acts as OR.
	private boolean requireAllConditions = true;

	/**
	 * Evaluates the configured conditions. If the criteria are met, the request
	 * is passed to all configured delegate handlers.
	 *
	 * @param exchange The current HTTP server exchange.
	 */
	@Override
	public void apply(HttpServerExchange exchange) {
		if (conditions == null || conditions.isEmpty() || delegates == null || delegates.isEmpty()) {
			return;
		}

		boolean match = requireAllConditions
				? conditions.stream().allMatch(c -> c.matches(exchange))
				: conditions.stream().anyMatch(c -> c.matches(exchange));

		if (match) {
			LOG.debug("Conditions met. Executing {} delegate handler(s) for {}", delegates.size(), exchange.getRequestPath());
			for (ProxyHttpServerExchangeHandler delegate : delegates) {
				delegate.apply(exchange);
			}
		}
	}

	public void setConditions(List<ExchangeCondition> conditions) {
		this.conditions = conditions;
	}

	public void setDelegates(List<ProxyHttpServerExchangeHandler> delegates) {
		this.delegates = delegates;
	}

	public void setRequireAllConditions(boolean requireAllConditions) {
		this.requireAllConditions = requireAllConditions;
	}
}
