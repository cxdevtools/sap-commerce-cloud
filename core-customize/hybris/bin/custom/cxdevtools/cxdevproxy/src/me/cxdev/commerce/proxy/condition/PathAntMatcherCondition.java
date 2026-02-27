package me.cxdev.commerce.proxy.condition;

import io.undertow.server.HttpServerExchange;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

/**
 * Condition that matches the request path using Spring's AntPathMatcher.
 * Highly useful for matching paths with standard wildcards.
 */
public class PathAntMatcherCondition implements ExchangeCondition {
	private String pattern;
	private final AntPathMatcher antPathMatcher = new AntPathMatcher();

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (StringUtils.isBlank(pattern)) {
			return false;
		}
		// Match the resolved path against the configured Ant pattern
		return antPathMatcher.match(pattern, exchange.getRequestPath());
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}
