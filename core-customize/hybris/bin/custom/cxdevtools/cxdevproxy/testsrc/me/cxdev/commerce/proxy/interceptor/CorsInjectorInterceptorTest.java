package me.cxdev.commerce.proxy.interceptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.HttpMethod;

@ExtendWith(MockitoExtension.class)
class CorsInjectorInterceptorTest {
	@Mock
	private HttpServerExchange exchangeMock;

	private CorsInjectorInterceptor interceptor;
	private HeaderMap requestHeaders;
	private HeaderMap responseHeaders;

	private static final HttpString ALLOW_ORIGIN = new HttpString("Access-Control-Allow-Origin");
	private static final HttpString ALLOW_CREDENTIALS = new HttpString("Access-Control-Allow-Credentials");

	@BeforeEach
	void setUp() {
		interceptor = new CorsInjectorInterceptor();

		requestHeaders = new HeaderMap();
		responseHeaders = new HeaderMap();

		lenient().when(exchangeMock.getRequestMethod()).thenReturn(HttpString.tryFromString(HttpMethod.OPTIONS));
		lenient().when(exchangeMock.getRequestHeaders()).thenReturn(requestHeaders);
		lenient().when(exchangeMock.getResponseHeaders()).thenReturn(responseHeaders);
	}

	@Test
	void testApply_WithOriginHeader_InjectsCorsResponseHeaders() throws Exception {
		requestHeaders.add(Headers.ORIGIN, "http://localhost:4200");

		interceptor.apply(exchangeMock);

		assertEquals("http://localhost:4200", responseHeaders.getFirst(ALLOW_ORIGIN));
		assertNull(responseHeaders.getFirst(ALLOW_CREDENTIALS));
		assertTrue(responseHeaders.contains(new HttpString("Access-Control-Allow-Methods")),
				"Methods should usually be handled by Spring/Hybris, unless explicitly set in proxy");
	}

	@Test
	void testApply_WithoutOriginHeader_DoesNothing() throws Exception {
		interceptor.apply(exchangeMock);

		assertFalse(responseHeaders.contains(ALLOW_ORIGIN));
	}

	@Test
	void testApply_WithAllowCredentialsTrue() throws Exception {
		interceptor.setAllowCredentials(true);
		requestHeaders.add(Headers.ORIGIN, "https://local.cxdev.me:4200");

		interceptor.apply(exchangeMock);

		assertEquals("https://local.cxdev.me:4200", responseHeaders.getFirst(ALLOW_ORIGIN));
		assertEquals("true", responseHeaders.getFirst(ALLOW_CREDENTIALS));
	}

	@Test
	void testApply_WithEmptyMethodsAndHeaders_DoesNotSetThem() throws Exception {
		requestHeaders.add(Headers.ORIGIN, "http://localhost:4200");

		interceptor.setAllowedMethods("");
		interceptor.setAllowedHeaders(null);
		interceptor.apply(exchangeMock);

		assertFalse(responseHeaders.contains(new HttpString("Access-Control-Allow-Methods")),
				"Empty allowedMethods should not result in a header");
		assertFalse(responseHeaders.contains(new HttpString("Access-Control-Allow-Headers")),
				"Null allowedHeaders should not result in a header");
		assertEquals("http://localhost:4200", responseHeaders.getFirst(ALLOW_ORIGIN));
	}

	@Test
	void testApply_OptionsRequest_EndsExchangeForPreflight() throws Exception {
		requestHeaders.add(Headers.ORIGIN, "http://localhost:4200");

		Mockito.lenient().when(exchangeMock.getRequestMethod())
				.thenReturn(io.undertow.util.Methods.OPTIONS);

		interceptor.apply(exchangeMock);

		verify(exchangeMock).endExchange();
	}

	@Test
	void testApply_NonOptionsRequest_DoesNotEndExchange() throws Exception {
		requestHeaders.add(Headers.ORIGIN, "http://localhost:4200");
		Mockito.lenient().when(exchangeMock.getRequestMethod())
				.thenReturn(io.undertow.util.Methods.GET);

		interceptor.apply(exchangeMock);

		verify(exchangeMock, org.mockito.Mockito.never()).endExchange();
	}
}
