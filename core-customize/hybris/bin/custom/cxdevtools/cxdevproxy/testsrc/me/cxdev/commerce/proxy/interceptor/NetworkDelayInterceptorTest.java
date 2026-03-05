package me.cxdev.commerce.proxy.interceptor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import io.undertow.server.HttpServerExchange;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NetworkDelayInterceptorTest {
	@Mock
	private HttpServerExchange exchangeMock;

	@Test
	@Timeout(value = 1, unit = TimeUnit.SECONDS)
	void testApply_WithFixedDelay() throws Exception {
		long delayMs = 50L;
		ProxyExchangeInterceptor interceptor = Interceptors.networkDelay("50ms");

		long start = System.currentTimeMillis();
		interceptor.apply(exchangeMock);
		long duration = System.currentTimeMillis() - start;

		assertTrue(duration >= delayMs, "Execution should be delayed by at least " + delayMs + "ms");
		assertTrue(duration < (delayMs + 30), "Execution should not take significantly longer than the delay");
	}

	@Test
	@Timeout(value = 1, unit = TimeUnit.SECONDS)
	void testApply_WithVariableDelay() throws Exception {
		long minDelay = 30L;
		long maxDelay = 80L;
		ProxyExchangeInterceptor interceptor = Interceptors.networkDelay("30ms", "80ms");

		long start = System.currentTimeMillis();
		interceptor.apply(exchangeMock);
		long duration = System.currentTimeMillis() - start;

		assertTrue(duration >= minDelay, "Execution should be delayed by at least minDelay (" + minDelay + "ms)");
		assertTrue(duration < (maxDelay + 30), "Execution should not exceed maxDelay + buffer (" + maxDelay + "ms)");
	}

	@Test
	@Timeout(value = 1, unit = TimeUnit.SECONDS)
	void testApply_WithNegativeValues_ShouldNotBlockForeverOrCrash() throws Exception {
		ProxyExchangeInterceptor interceptor = Interceptors.networkDelay("-100ms", "-50ms");

		long start = System.currentTimeMillis();
		interceptor.apply(exchangeMock);
		long duration = System.currentTimeMillis() - start;

		assertTrue(duration < 20, "Negative delays should be handled gracefully without long blocking");
	}

	@Test
	@Timeout(value = 1, unit = TimeUnit.SECONDS)
	void testApply_WithMinGreaterThanMax_UsesMinForBoth() throws Exception {
		long minDelay = 80L;
		long maxDelay = 30L;
		ProxyExchangeInterceptor interceptor = Interceptors.networkDelay("80ms", "30ms");

		long start = System.currentTimeMillis();
		interceptor.apply(exchangeMock);
		long duration = System.currentTimeMillis() - start;

		assertTrue(duration >= minDelay, "Execution should be delayed by at least minDelay (" + minDelay + "ms) when min > max. maxDelay (" + maxDelay + ")");
		assertTrue(duration < (minDelay + 30), "Execution should treat minDelay as the fixed delay when min > max");
	}
}
