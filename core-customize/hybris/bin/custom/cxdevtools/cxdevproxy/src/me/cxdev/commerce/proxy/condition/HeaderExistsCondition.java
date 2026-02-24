package me.cxdev.commerce.proxy.condition;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import org.apache.commons.lang3.StringUtils;

/**
 * Condition that matches if the request contains a specific HTTP header.
 */
public class HeaderExistsCondition implements ExchangeCondition {
	private String headerName;

	@Override
	public boolean matches(HttpServerExchange exchange) {
		if (StringUtils.isBlank(headerName)) {
			return false;
		}
		return exchange.getRequestHeaders().contains(new HttpString(headerName));
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}
}
