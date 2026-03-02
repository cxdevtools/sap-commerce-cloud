package me.cxdev.commerce.proxy.interceptor.condition;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

@ExtendWith(MockitoExtension.class)
class ConditionsTest {

	@Mock
	private HttpServerExchange exchangeMock;

	@BeforeEach
	void setUp() {
		// Reset mock behavior before each test if needed
	}

	// --- Logical Operators (AND, OR, NOT) Edge Cases ---

	@Test
	void testNotCondition() {
		when(exchangeMock.getRequestPath()).thenReturn("/occ/v2/");

		ProxyExchangeInterceptorCondition isOcc = Conditions.pathStartsWith("/occ");
		ProxyExchangeInterceptorCondition isNotOcc = isOcc.not(); // Using default method from interface
		ProxyExchangeInterceptorCondition isNotOccViaFactory = Conditions.not(isOcc);

		assertTrue(isOcc.matches(exchangeMock));
		assertFalse(isNotOcc.matches(exchangeMock));
		assertFalse(isNotOccViaFactory.matches(exchangeMock));
	}

	@Test
	void testAndCondition() {
		when(exchangeMock.getRequestPath()).thenReturn("/smartedit/");

		HeaderMap headers = new HeaderMap();
		headers.add(new HttpString("Authorization"), "Bearer token123");
		when(exchangeMock.getRequestHeaders()).thenReturn(headers);

		ProxyExchangeInterceptorCondition isSmartEdit = Conditions.pathStartsWith("/smartedit");
		ProxyExchangeInterceptorCondition hasAuth = Conditions.hasHeader("Authorization");
		ProxyExchangeInterceptorCondition isOcc = Conditions.pathStartsWith("/occ");

		// Test Factory method
		ProxyExchangeInterceptorCondition smartEditAndAuth = Conditions.and(isSmartEdit, hasAuth);
		assertTrue(smartEditAndAuth.matches(exchangeMock), "Both conditions are true");

		// Test Default Interface method chaining
		ProxyExchangeInterceptorCondition chainedFailing = isSmartEdit.and(isOcc);
		assertFalse(chainedFailing.matches(exchangeMock), "One condition is false, should be false");
	}

	@Test
	void testOrCondition() {
		when(exchangeMock.getRequestPath()).thenReturn("/occ/v2/");

		ProxyExchangeInterceptorCondition isSmartEdit = Conditions.pathStartsWith("/smartedit");
		ProxyExchangeInterceptorCondition isOcc = Conditions.pathStartsWith("/occ");

		// Test Factory method
		ProxyExchangeInterceptorCondition occOrSmartEdit = Conditions.or(isSmartEdit, isOcc);
		assertTrue(occOrSmartEdit.matches(exchangeMock), "One condition is true, should be true");

		// Test Default Interface method chaining
		ProxyExchangeInterceptorCondition chainedFailing = Conditions.pathStartsWith("/hac")
				.or(Conditions.pathStartsWith("/backoffice"));
		assertFalse(chainedFailing.matches(exchangeMock), "Both conditions are false, should be false");
	}

	@Test
	void testComplexChaining() {
		// Simulating: /occ request WITH an Authorization header
		when(exchangeMock.getRequestPath()).thenReturn("/occ/v2/users");

		HeaderMap headers = new HeaderMap();
		headers.add(new HttpString("Authorization"), "Bearer xyz");
		when(exchangeMock.getRequestHeaders()).thenReturn(headers);

		ProxyExchangeInterceptorCondition isOcc = Conditions.pathStartsWith("/occ");
		ProxyExchangeInterceptorCondition isSmartEdit = Conditions.pathStartsWith("/smartedit");
		ProxyExchangeInterceptorCondition hasAuth = Conditions.hasHeader("Authorization");

		// (isOcc OR isSmartEdit) AND (NOT hasAuth)
		ProxyExchangeInterceptorCondition complexCondition = isOcc.or(isSmartEdit).and(hasAuth.not());

		// Should be false because hasAuth is true, so hasAuth.not() is false
		assertFalse(complexCondition.matches(exchangeMock), "Complex chain should evaluate correctly");
	}

	@Test
	void testLogicalConditionsWithNullOrEmpty() {
		assertFalse(Conditions.and((ProxyExchangeInterceptorCondition[]) null).matches(exchangeMock), "Null AND should be false");
		assertFalse(Conditions.and(new ProxyExchangeInterceptorCondition[0]).matches(exchangeMock), "Empty AND should be false");

		assertFalse(Conditions.or((ProxyExchangeInterceptorCondition[]) null).matches(exchangeMock), "Null OR should be false");
		assertFalse(Conditions.or(new ProxyExchangeInterceptorCondition[0]).matches(exchangeMock), "Empty OR should be false");

		assertFalse(Conditions.not(null).matches(exchangeMock), "Null NOT should be false");
	}

	@Test
	void testLogicalConditionsWithSingleElement() {
		// Wir nehmen eine beliebige Condition als Dummy
		ProxyExchangeInterceptorCondition singleCondition = Conditions.always();

		// Rufen die Factory mit genau einem Element auf
		ProxyExchangeInterceptorCondition andResult = Conditions.and(singleCondition);
		ProxyExchangeInterceptorCondition orResult = Conditions.or(singleCondition);

		// Prüfen auf exakte Speicherreferenz (Identity)
		assertSame(singleCondition, andResult, "AND with a single condition should return the condition itself");
		assertSame(singleCondition, orResult, "OR with a single condition should return the condition itself");
	}

	// --- Cookie Condition ---

	@Test
	void testCookieExists() {
		when(exchangeMock.getRequestCookie("cxdevproxy_user_id")).thenReturn(new CookieImpl("cxdevproxy_user_id", "admin"));
		when(exchangeMock.getRequestCookie("missing_cookie")).thenReturn(null);

		assertTrue(Conditions.hasCookie("cxdevproxy_user_id").matches(exchangeMock));
		assertFalse(Conditions.hasCookie("missing_cookie").matches(exchangeMock));

		// Edge cases
		assertFalse(Conditions.hasCookie(null).matches(exchangeMock));
		assertFalse(Conditions.hasCookie("").matches(exchangeMock));
	}

	// --- Header Condition ---

	@Test
	void testHeaderExists() {
		HeaderMap headers = new HeaderMap();
		headers.add(new HttpString("Authorization"), "Bearer token");
		when(exchangeMock.getRequestHeaders()).thenReturn(headers);

		assertTrue(Conditions.hasHeader("Authorization").matches(exchangeMock));
		assertFalse(Conditions.hasHeader("Accept").matches(exchangeMock));

		// Edge cases for null/empty/blank
		assertFalse(Conditions.hasHeader(null).matches(exchangeMock), "Null header should be false");
		assertFalse(Conditions.hasHeader("").matches(exchangeMock), "Empty header should be false");
		assertFalse(Conditions.hasHeader("   ").matches(exchangeMock), "Blank header should be false");
	}

	// --- HTTP Method Condition ---

	@Test
	void testHttpMethodCondition() {
		when(exchangeMock.getRequestMethod()).thenReturn(new HttpString("POST"));

		assertTrue(Conditions.isMethod("POST").matches(exchangeMock));
		assertTrue(Conditions.isMethod("post").matches(exchangeMock), "Should be case-insensitive");
		assertFalse(Conditions.isMethod("GET").matches(exchangeMock));

		// Edge cases
		assertFalse(Conditions.isMethod(null).matches(exchangeMock));
		assertFalse(Conditions.isMethod("").matches(exchangeMock));
	}

	// --- Path Conditions (StartsWith, Ant, Regex) ---

	@Test
	void testPathStartsWith() {
		lenient().when(exchangeMock.getRequestPath()).thenReturn("/occ/v2/electronics");

		assertTrue(Conditions.pathStartsWith("/occ").matches(exchangeMock));
		assertFalse(Conditions.pathStartsWith("/backoffice").matches(exchangeMock));

		// Edge cases for null/empty/blank -> should be TRUE according to logic
		assertTrue(Conditions.pathStartsWith(null).matches(exchangeMock), "Null prefix should be true");
		assertTrue(Conditions.pathStartsWith("").matches(exchangeMock), "Empty prefix should be true");
		assertTrue(Conditions.pathStartsWith("   ").matches(exchangeMock), "Blank prefix should be true");
	}

	@Test
	void testPathAntMatcherCondition() {
		lenient().when(exchangeMock.getRequestPath()).thenReturn("/occ/v2/electronics/users/current");

		assertTrue(Conditions.pathMatches("/occ/v2/**").matches(exchangeMock));
		assertTrue(Conditions.pathMatches("/**/users/*").matches(exchangeMock));
		assertFalse(Conditions.pathMatches("/backoffice/**").matches(exchangeMock));

		// Edge cases
		assertFalse(Conditions.pathMatches(null).matches(exchangeMock));
		assertFalse(Conditions.pathMatches("").matches(exchangeMock));
	}

	@Test
	void testPathRegexCondition() {
		lenient().when(exchangeMock.getRequestPath()).thenReturn("/occ/v2/electronics/users/current");

		assertTrue(Conditions.pathRegexMatches("^/occ/v[0-9]+.*").matches(exchangeMock));
		assertFalse(Conditions.pathRegexMatches("^/backoffice/.*").matches(exchangeMock));

		// Edge cases
		assertFalse(Conditions.pathRegexMatches(null).matches(exchangeMock));
		assertFalse(Conditions.pathRegexMatches("").matches(exchangeMock));
	}

	// --- Query Parameter Condition ---

	@Test
	void testQueryParameterExists() {
		Map<String, Deque<String>> queryParams = new HashMap<>();
		Deque<String> values = new ArrayDeque<>();
		values.add("FULL");
		queryParams.put("fields", values);

		when(exchangeMock.getQueryParameters()).thenReturn(queryParams);

		assertTrue(Conditions.hasParameter("fields").matches(exchangeMock));
		assertFalse(Conditions.hasParameter("lang").matches(exchangeMock));

		// Edge cases
		assertFalse(Conditions.hasParameter(null).matches(exchangeMock));
		assertFalse(Conditions.hasParameter("").matches(exchangeMock));
	}

	// --- Static Conditions ---

	@Test
	void testStaticCondition() {
		assertTrue(Conditions.always().matches(exchangeMock));
		assertFalse(Conditions.never().matches(exchangeMock));
	}
}
