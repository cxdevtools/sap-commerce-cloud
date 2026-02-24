package me.cxdev.commerce.proxy.condition;

import io.undertow.server.HttpServerExchange;

import org.apache.commons.lang3.StringUtils;

/**
 * Condition that matches if the request path starts with a specific prefix.
 */
public class PathStartsWithCondition implements ExchangeCondition {
	private String prefix;

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (StringUtils.isBlank(prefix)) {
			return false;
		}
		return exchange.getRequestPath().startsWith(prefix);
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
