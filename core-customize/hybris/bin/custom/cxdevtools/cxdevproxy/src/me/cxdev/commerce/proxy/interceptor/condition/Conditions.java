package me.cxdev.commerce.proxy.interceptor.condition;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

/**
 * Static factory methods for creating {@link ProxyExchangeInterceptorCondition} instances fluently within the Groovy DSL.
 * <p>
 * This class provides a highly readable, AssertJ-style API for composing request matching rules.
 * Because these methods are statically imported into the Groovy script context by the rule engine,
 * developers can use them directly to build concise routing conditions
 * (e.g., {@code pathMatches("/occ/**").and(hasHeader("Authorization"))}).
 * </p>
 */
public final class Conditions {
	private Conditions() {
		// Prevent instantiation
	}

	/**
	 * Combines multiple conditions using a logical AND operation.
	 * Evaluates to true only if ALL provided conditions evaluate to true.
	 *
	 * @param conditions The conditions to combine.
	 * @return A composite AND condition.
	 */
	public static ProxyExchangeInterceptorCondition and(final ProxyExchangeInterceptorCondition... conditions) {
		if (conditions == null || conditions.length == 0) {
			return never();
		} else if (conditions.length == 1) {
			return conditions[0];
		} else {
			return new AndCondition(conditions);
		}
	}

	/**
	 * Combines multiple conditions using a logical OR operation.
	 * Evaluates to true if AT LEAST ONE of the provided conditions evaluates to true.
	 *
	 * @param conditions The conditions to combine.
	 * @return A composite OR condition.
	 */
	public static ProxyExchangeInterceptorCondition or(final ProxyExchangeInterceptorCondition... conditions) {
		if (conditions == null || conditions.length == 0) {
			return never();
		} else if (conditions.length == 1) {
			return conditions[0];
		} else {
			return new OrCondition(conditions);
		}
	}

	/**
	 * Negates the given condition using a logical NOT operation.
	 * Evaluates to true only if the provided condition evaluates to false.
	 *
	 * @param condition The condition to negate.
	 * @return A negated condition, or a condition that never matches if the input is null.
	 */
	public static ProxyExchangeInterceptorCondition not(final ProxyExchangeInterceptorCondition condition) {
		return condition == null ? never() : new NotCondition(condition);
	}

	/**
	 * Returns a condition that inherently always evaluates to true.
	 * Useful as a fallback, default route, or starting point for logical chaining.
	 *
	 * @return A condition that always matches.
	 */
	public static ProxyExchangeInterceptorCondition always() {
		return StaticCondition.ALWAYS;
	}

	/**
	 * Returns a condition that inherently never evaluates to true.
	 * Useful for disabling routes or as a base for dynamic logical structures.
	 *
	 * @return A condition that never matches.
	 */
	public static ProxyExchangeInterceptorCondition never() {
		return StaticCondition.NEVER;
	}

	/**
	 * Matches if the incoming request URI strictly starts with the specified prefix.
	 *
	 * @param prefix The exact URL prefix (e.g., "/authorizationserver").
	 * @return A path prefix matching condition.
	 */
	public static ProxyExchangeInterceptorCondition pathStartsWith(String prefix) {
		return new PathStartsWithCondition(prefix);
	}

	/**
	 * Matches the incoming request URI against a Spring Ant-style pattern.
	 *
	 * @param pattern The Ant-style pattern (e.g., "/occ/v2/**").
	 * @return An Ant-pattern matching condition.
	 */
	public static ProxyExchangeInterceptorCondition pathMatches(String pattern) {
		return new PathAntMatcherCondition(pattern);
	}

	/**
	 * Matches the incoming request URI against a regular expression.
	 *
	 * @param regex The regular expression to test against the request path.
	 * @return A regex matching condition.
	 */
	public static ProxyExchangeInterceptorCondition pathRegexMatches(String regex) {
		return new PathRegexCondition(regex);
	}

	/**
	 * Matches if the incoming request contains the specified HTTP header.
	 * The value of the header is not checked, only its presence.
	 *
	 * @param headerName The exact name of the HTTP header (e.g., "Authorization").
	 * @return A header existence matching condition.
	 */
	public static ProxyExchangeInterceptorCondition hasHeader(String headerName) {
		return new HeaderExistsCondition(headerName);
	}

	/**
	 * Matches if the incoming request contains the specified HTTP cookie.
	 * The value of the cookie is not checked, only its presence.
	 *
	 * @param cookieName The exact name of the cookie (e.g., "cxdevproxy_user_id").
	 * @return A cookie existence matching condition.
	 */
	public static ProxyExchangeInterceptorCondition hasCookie(String cookieName) {
		return new CookieExistsCondition(cookieName);
	}

	/**
	 * Matches if the incoming request URL contains the specified query parameter.
	 *
	 * @param parameterName The name of the query parameter (e.g., "fields").
	 * @return A query parameter existence matching condition.
	 */
	public static ProxyExchangeInterceptorCondition hasParameter(String parameterName) {
		return new QueryParameterExistsCondition(parameterName);
	}

	/**
	 * Matches if the incoming HTTP request method strictly equals the specified value.
	 *
	 * @param httpMethod The HTTP method to match (e.g., "GET", "POST", "OPTIONS").
	 * @return An HTTP method matching condition.
	 */
	public static ProxyExchangeInterceptorCondition isMethod(String httpMethod) {
		return new HttpMethodCondition(httpMethod);
	}
}
