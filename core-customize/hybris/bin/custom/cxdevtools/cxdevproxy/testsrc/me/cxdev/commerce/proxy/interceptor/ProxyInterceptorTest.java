package me.cxdev.commerce.proxy.interceptor;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.undertow.server.HttpServerExchange;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProxyInterceptorTest {

	@Mock
	private HttpServerExchange exchangeMock;

	@Mock
	private ProxyExchangeInterceptorCondition cond1;

	@Mock
	private ProxyExchangeInterceptorCondition cond2;

	@Mock
	private ProxyExchangeInterceptor delegate1;

	@Mock
	private ProxyExchangeInterceptor delegate2;

	// --- 1. Edge Cases & Fail-Safes ---

	@Test
	void testApplyWithEmptyConditionsOrDelegates() throws Exception {
		// Test empty setup
		ProxyInterceptor emptyInterceptor = ProxyInterceptor.interceptor().perform();
		emptyInterceptor.apply(exchangeMock);
		verifyNoInteractions(exchangeMock);

		// Test with conditions but no delegates
		ProxyInterceptor noDelegates = ProxyInterceptor.interceptor()
				.constrainedBy(cond1)
				.perform();
		noDelegates.apply(exchangeMock);
		verifyNoInteractions(cond1, exchangeMock);

		// Test null safety in builder
		ProxyInterceptor nullSafety = ProxyInterceptor.interceptor()
				.constrainedBy((ProxyExchangeInterceptorCondition[]) null)
				.perform((ProxyExchangeInterceptor[]) null);
		nullSafety.apply(exchangeMock);
		verifyNoInteractions(exchangeMock);
	}

	// --- 2. AND Logic (requireAllConditions = true) ---

	@Test
	void testApplyRequireAllTrue_AllMatch() throws Exception {
		when(cond1.matches(exchangeMock)).thenReturn(true);
		when(cond2.matches(exchangeMock)).thenReturn(true);

		ProxyInterceptor interceptor = ProxyInterceptor.interceptor()
				.constrainedBy(cond1, cond2)
				.requireAll(true) // default, but explicit for test
				.perform(delegate1, delegate2);

		interceptor.apply(exchangeMock);

		// Both conditions must be checked
		verify(cond1).matches(exchangeMock);
		verify(cond2).matches(exchangeMock);

		// Both delegates must be executed
		verify(delegate1, times(1)).apply(exchangeMock);
		verify(delegate2, times(1)).apply(exchangeMock);
	}

	@Test
	void testApplyRequireAllTrue_OneFails() throws Exception {
		when(cond1.matches(exchangeMock)).thenReturn(true);
		when(cond2.matches(exchangeMock)).thenReturn(false);

		ProxyInterceptor interceptor = ProxyInterceptor.interceptor()
				.constrainedBy(cond1, cond2)
				.perform(delegate1);

		interceptor.apply(exchangeMock);

		// cond1 was true, cond2 was false -> match fails
		// Delegate must NEVER be called
		verify(delegate1, never()).apply(exchangeMock);
	}

	// --- 3. OR Logic (requireAllConditions = false) ---

	@Test
	void testApplyRequireAllFalse_OneMatches() throws Exception {
		// Set first condition to false, second to true
		when(cond1.matches(exchangeMock)).thenReturn(false);
		when(cond2.matches(exchangeMock)).thenReturn(true);

		ProxyInterceptor interceptor = ProxyInterceptor.interceptor()
				.constrainedBy(cond1, cond2)
				.requireAll(false) // Act as OR
				.perform(delegate1);

		interceptor.apply(exchangeMock);

		// Because it's OR and cond2 is true, it should execute the delegate
		verify(delegate1, times(1)).apply(exchangeMock);
	}

	@Test
	void testApplyRequireAllFalse_NoneMatches() throws Exception {
		when(cond1.matches(exchangeMock)).thenReturn(false);
		when(cond2.matches(exchangeMock)).thenReturn(false);

		ProxyInterceptor interceptor = ProxyInterceptor.interceptor()
				.constrainedBy(cond1, cond2)
				.requireAll(false)
				.perform(delegate1);

		interceptor.apply(exchangeMock);

		// Neither condition matched -> delegate must NEVER be called
		verify(delegate1, never()).apply(exchangeMock);
	}

	// --- 4. Short-Circuiting Optimization ---

	@Test
	void testAndLogicShortCircuits() throws Exception {
		// If cond1 is false in an AND logic, cond2 should not even be evaluated
		when(cond1.matches(exchangeMock)).thenReturn(false);

		ProxyInterceptor interceptor = ProxyInterceptor.interceptor()
				.constrainedBy(cond1, cond2)
				.perform(delegate1);

		interceptor.apply(exchangeMock);

		verify(cond1).matches(exchangeMock);
		verify(cond2, never()).matches(exchangeMock); // Stream.allMatch short-circuits!
		verifyNoInteractions(delegate1);
	}

	@Test
	void testOrLogicShortCircuits() throws Exception {
		// If cond1 is true in an OR logic, cond2 should not even be evaluated
		when(cond1.matches(exchangeMock)).thenReturn(true);

		ProxyInterceptor interceptor = ProxyInterceptor.interceptor()
				.constrainedBy(cond1, cond2)
				.requireAll(false)
				.perform(delegate1);

		interceptor.apply(exchangeMock);

		verify(cond1).matches(exchangeMock);
		verify(cond2, never()).matches(exchangeMock); // Stream.anyMatch short-circuits!
		verify(delegate1).apply(exchangeMock);
	}
}
