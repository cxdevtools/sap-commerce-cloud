package me.cxdev.commerce.proxy.interceptor.condition;

import io.undertow.server.HttpServerExchange;

import org.apache.commons.lang3.StringUtils;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * Condition that matches if the request URL contains a specific query parameter.
 */
class QueryParameterExistsCondition implements ProxyExchangeInterceptorCondition {
	private String name;

	QueryParameterExistsCondition(String name) {
		this.name = name;
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (StringUtils.isBlank(name)) {
			return false;
		}
		return exchange.getQueryParameters().containsKey(name);
	}
}
