package me.cxdev.commerce.proxy.interceptor.condition;

import io.undertow.server.HttpServerExchange;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * Condition that matches the request path using Spring's AntPathMatcher.
 * Highly useful for matching paths with standard wildcards.
 */
class PathAntMatcherCondition implements ProxyExchangeInterceptorCondition {
	private final AntPathMatcher antPathMatcher;
	private final String pattern;

	PathAntMatcherCondition(String pattern) {
		this.antPathMatcher = new AntPathMatcher();
		this.pattern = pattern;
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (StringUtils.isBlank(pattern)) {
			return false;
		}

		// Match the resolved path against the configured Ant pattern
		return antPathMatcher.match(pattern, exchange.getRequestPath());
	}
}
