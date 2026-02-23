package me.cxdev.commerce.proxy.condition;

import io.undertow.server.HttpServerExchange;

import org.apache.commons.lang3.StringUtils;

/**
 * Condition that matches if the request URL contains a specific query parameter.
 */
public class QueryParameterExistsCondition implements ExchangeCondition {
	private String paramName;

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (StringUtils.isBlank(paramName)) {
			return false;
		}
		return exchange.getQueryParameters().containsKey(paramName);
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}
}
