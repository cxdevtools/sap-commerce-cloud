package me.cxdev.commerce.proxy.interceptor;

import io.undertow.server.HttpServerExchange;

/**
 * Interface for applying custom rules and headers to an Undertow HttpServerExchange
 * before it is proxied to the target server.
 */
public interface ProxyExchangeInterceptor {
	/**
	 * Applies rules or modifications to the exchange.
	 * * @param exchange the current HTTP server exchange
	 */
	void apply(HttpServerExchange exchange);
}
