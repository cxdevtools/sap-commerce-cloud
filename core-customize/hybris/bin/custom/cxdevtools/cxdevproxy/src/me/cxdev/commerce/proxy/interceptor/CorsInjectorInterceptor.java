package me.cxdev.commerce.proxy.interceptor;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import org.apache.commons.lang3.StringUtils;

/**
 * Injects configurable CORS (Cross-Origin Resource Sharing) headers into the response.
 * Acts as an "Auto-CORS" modifier by dynamically echoing the incoming 'Origin' header
 * back to the client. If no Origin header is present in the request, no CORS headers
 * are injected.
 */
public class CorsInjectorInterceptor implements ProxyExchangeInterceptor {
	private String allowedMethods = "GET, POST, PUT, DELETE, OPTIONS, PATCH";
	private String allowedHeaders = "Authorization, Content-Type, Accept, Origin, X-Requested-With";
	private boolean allowCredentials = false;

	@Override
	public void apply(HttpServerExchange exchange) {
		String requestOrigin = exchange.getRequestHeaders().getFirst(Headers.ORIGIN);

		// Only inject CORS headers if an Origin is actually present in the request
		if (StringUtils.isNotBlank(requestOrigin)) {
			exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), requestOrigin);

			if (StringUtils.isNotBlank(allowedMethods)) {
				exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Methods"), allowedMethods);
			}

			if (StringUtils.isNotBlank(allowedHeaders)) {
				exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Headers"), allowedHeaders);
			}

			if (allowCredentials) {
				exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Credentials"), "true");
			}
		}

		// If it's a preflight OPTIONS request, answer it immediately
		if (exchange.getRequestMethod().toString().equalsIgnoreCase("OPTIONS")) {
			exchange.setStatusCode(200);
			exchange.endExchange();
		}
	}

	public void setAllowedMethods(String allowedMethods) {
		this.allowedMethods = allowedMethods;
	}

	public void setAllowedHeaders(String allowedHeaders) {
		this.allowedHeaders = allowedHeaders;
	}

	public void setAllowCredentials(boolean allowCredentials) {
		this.allowCredentials = allowCredentials;
	}
}
