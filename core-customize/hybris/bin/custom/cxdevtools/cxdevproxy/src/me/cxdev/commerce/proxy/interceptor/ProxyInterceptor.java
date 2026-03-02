package me.cxdev.commerce.proxy.interceptor;

import java.util.List;

import io.undertow.server.HttpServerExchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A composite interceptor that delegates execution to a list of underlying interceptors
 * only if a configured set of conditions is met.
 * <p>
 * By default, ALL conditions must evaluate to {@code true} (AND logic).
 * This can be changed to OR logic by setting {@code requireAllConditions} to {@code false}.
 * </p>
 */
class ProxyInterceptor implements ProxyExchangeInterceptor {
	private static final Logger LOG = LoggerFactory.getLogger(ProxyInterceptor.class);

	private List<ProxyExchangeInterceptorCondition> conditions;
	private List<ProxyExchangeInterceptor> interceptors;

	// If true, acts as AND. If false, acts as OR.
	private boolean requireAllConditions = true;

	ProxyInterceptor(
			List<ProxyExchangeInterceptorCondition> conditions,
			List<ProxyExchangeInterceptor> interceptors,
			boolean requireAllConditions) {
		this.conditions = List.copyOf(conditions);
		this.interceptors = List.copyOf(interceptors);
		this.requireAllConditions = requireAllConditions;
	}

	/**
	 * Evaluates the configured conditions. If the criteria are met, the request
	 * is passed to all configured delegate handlers.
	 *
	 * @param exchange The current HTTP server exchange.
	 */
	@Override
	public void apply(HttpServerExchange exchange) {
		if (conditions == null || conditions.isEmpty() || interceptors == null || interceptors.isEmpty()) {
			return;
		}

		boolean match = requireAllConditions
				? conditions.stream().allMatch(c -> c.matches(exchange))
				: conditions.stream().anyMatch(c -> c.matches(exchange));

		if (match) {
			LOG.debug("Conditions met. Executing {} delegate handler(s) for {}", interceptors.size(), exchange.getRequestPath());
			for (ProxyExchangeInterceptor delegate : interceptors) {
				delegate.apply(exchange);
			}
		}
	}
}
