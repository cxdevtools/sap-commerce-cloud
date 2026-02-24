package me.cxdev.commerce.proxy.handler;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.Headers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.cxdev.commerce.proxy.livecycle.ProxyHttpServerExchangeHandler;
import me.cxdev.commerce.proxy.service.JwtTokenService;

/**
 * Interceptor that injects a mocked JWT into the HTTP request before routing it to the backend.
 * <p>
 * It checks the incoming request for a specific cookie ({@code cxdev_user}) set by the
 * proxy's developer portal. If found, it requests a signed JWT from the {@link JwtTokenService}
 * and appends it as an standard {@code Authorization: Bearer <token>} header.
 * </p>
 */
public class JwtInjectorHandler implements ProxyHttpServerExchangeHandler {
	private static final Logger LOG = LoggerFactory.getLogger(JwtInjectorHandler.class);
	private static final String USER_ID_COOKIE_NAME = "cxdevproxy_user_id";
	private static final String USER_TYPE_COOKIE_NAME = "cxdevproxy_user_type";

	private JwtTokenService jwtTokenService;

	/**
	 * Evaluates the request cookies and injects the JWT Authorization header if applicable.
	 *
	 * @param exchange The current HTTP server exchange.
	 */
	@Override
	public void apply(HttpServerExchange exchange) {
		Cookie userIdCookie = exchange.getRequestCookie(USER_ID_COOKIE_NAME);
		Cookie userTypeCookie = exchange.getRequestCookie(USER_TYPE_COOKIE_NAME);

		if (userIdCookie != null && StringUtils.isNotBlank(userIdCookie.getValue())) {
			String userType = userTypeCookie.getValue();
			String userId = userIdCookie.getValue();
			String token = jwtTokenService.getOrGenerateToken(userType, userId);

			if (token != null) {
				// Remove any existing authorization header from the client to enforce our mock token
				exchange.getRequestHeaders().remove(Headers.AUTHORIZATION);

				// Inject the mocked token
				exchange.getRequestHeaders().put(Headers.AUTHORIZATION, "Bearer " + token);
				LOG.debug("Injected mocked JWT for user '{}' into request to {}", userId, exchange.getRequestPath());
			}
		}
	}

	public void setJwtTokenService(JwtTokenService jwtTokenService) {
		this.jwtTokenService = jwtTokenService;
	}
}
