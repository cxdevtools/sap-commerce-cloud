package me.cxdev.commerce.proxy.interceptor.condition;

import java.util.regex.Pattern;

import io.undertow.server.HttpServerExchange;

import org.apache.commons.lang3.StringUtils;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * Condition that matches the request path against a regular expression.
 * Highly useful for targeting REST API endpoints with path variables
 * (e.g., matching /occ/v2/.+/users/current).
 */
class PathRegexCondition implements ProxyExchangeInterceptorCondition {
	private final Pattern compiledPattern;

	PathRegexCondition(String regex) {
		if (StringUtils.isNotBlank(regex)) {
			this.compiledPattern = Pattern.compile(regex);
		} else {
			this.compiledPattern = null;
		}
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (compiledPattern == null) {
			return false;
		}
		// Match the resolved path (e.g., "/occ/v2/electronics/users/current")
		return compiledPattern.matcher(exchange.getRequestPath()).matches();
	}
}
