package me.cxdev.commerce.proxy.interceptor.condition;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import org.apache.commons.lang3.StringUtils;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * Condition that matches if the request contains a specific HTTP header.
 */
class HeaderExistsCondition implements ProxyExchangeInterceptorCondition {
	private final String headerName;

	HeaderExistsCondition(String headerName) {
		this.headerName = headerName;
	}

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (StringUtils.isBlank(headerName)) {
			return false;
		}
		return exchange.getRequestHeaders().contains(new HttpString(headerName));
	}
}
