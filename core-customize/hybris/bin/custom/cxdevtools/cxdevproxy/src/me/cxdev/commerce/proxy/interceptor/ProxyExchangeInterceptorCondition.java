package me.cxdev.commerce.proxy.interceptor;

import io.undertow.server.HttpServerExchange;

import me.cxdev.commerce.proxy.interceptor.condition.Conditions;

/**
 * Represents a condition that evaluates an incoming HTTP request.
 * Used to determine if a specific proxy interceptor should be executed.
 */
public interface ProxyExchangeInterceptorCondition {
	/**
	 * Evaluates the condition against the current HTTP exchange.
	 *
	 * @param exchange The current Undertow HTTP server exchange.
	 * @return {@code true} if the condition is met, {@code false} otherwise.
	 */
	boolean matches(HttpServerExchange exchange);

	default ProxyExchangeInterceptorCondition not() {
		return Conditions.not(this);
	}

	default ProxyExchangeInterceptorCondition and(ProxyExchangeInterceptorCondition... others) {
		return Conditions.and(combineWith(others));
	}

	default ProxyExchangeInterceptorCondition or(ProxyExchangeInterceptorCondition... others) {
		return Conditions.or(combineWith(others));
	}

	/**
	 * Helper method to efficiently merge 'this' condition with an array of other conditions
	 * without creating intermediate Collection objects.
	 */
	private ProxyExchangeInterceptorCondition[] combineWith(ProxyExchangeInterceptorCondition... others) {
		if (others == null || others.length == 0) {
			return new ProxyExchangeInterceptorCondition[] { this };
		}

		ProxyExchangeInterceptorCondition[] combined = new ProxyExchangeInterceptorCondition[others.length + 1];
		combined[0] = this;
		System.arraycopy(others, 0, combined, 1, others.length);
		return combined;
	}
}
