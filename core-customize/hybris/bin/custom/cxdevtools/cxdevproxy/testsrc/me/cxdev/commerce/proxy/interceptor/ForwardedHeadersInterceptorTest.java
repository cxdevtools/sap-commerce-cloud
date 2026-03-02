package me.cxdev.commerce.proxy.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

import java.net.InetSocketAddress;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ForwardedHeadersInterceptorTest {
	@Mock
	private HttpServerExchange exchangeMock;

	private ForwardedHeadersInterceptor interceptor;
	private HeaderMap requestHeaders;

	@BeforeEach
	void setUp() {
		interceptor = new ForwardedHeadersInterceptor();

		// Setup der Fallback-Properties (als kämen sie aus Spring)
		interceptor.setServerHostname("fallback.local.cxdev.me");
		interceptor.setServerProtocol("https");
		interceptor.setServerPort(8080);

		requestHeaders = new HeaderMap();
		lenient().when(exchangeMock.getRequestHeaders()).thenReturn(requestHeaders);

		// Mock für X-Forwarded-For
		InetSocketAddress sourceAddress = new InetSocketAddress("192.168.1.100", 54321);
		lenient().when(exchangeMock.getSourceAddress()).thenReturn(sourceAddress);
	}

	@Test
	void testApply_HostHeaderWithValidPort() throws Exception {
		requestHeaders.put(Headers.HOST, "my.custom.host:9002");

		interceptor.apply(exchangeMock);

		assertEquals("my.custom.host", requestHeaders.getFirst(new HttpString("X-Forwarded-Host")));
		assertEquals("9002", requestHeaders.getFirst(new HttpString("X-Forwarded-Port")));
		assertEquals("192.168.1.100", requestHeaders.getFirst(new HttpString("X-Forwarded-For")));
	}

	@Test
	void testApply_HostHeaderWithoutPort_HttpsFallback() throws Exception {
		interceptor.setServerProtocol("https");
		requestHeaders.put(Headers.HOST, "my.custom.host");

		interceptor.apply(exchangeMock);

		assertEquals("my.custom.host", requestHeaders.getFirst(new HttpString("X-Forwarded-Host")));
		// Fallback zu 443 bei HTTPS
		assertEquals("443", requestHeaders.getFirst(new HttpString("X-Forwarded-Port")));
	}

	@Test
	void testApply_HostHeaderWithoutPort_HttpFallback() throws Exception {
		interceptor.setServerProtocol("http"); // Protokoll ändern
		requestHeaders.put(Headers.HOST, "my.custom.host");

		interceptor.apply(exchangeMock);

		assertEquals("my.custom.host", requestHeaders.getFirst(new HttpString("X-Forwarded-Host")));
		// Fallback zu 80 bei HTTP
		assertEquals("80", requestHeaders.getFirst(new HttpString("X-Forwarded-Port")));
	}

	@Test
	void testApply_HostHeaderWithInvalidPort_CatchesNumberFormatException() throws Exception {
		interceptor.setServerProtocol("https");
		requestHeaders.put(Headers.HOST, "my.custom.host:invalid"); // Unparseable port

		interceptor.apply(exchangeMock);

		assertEquals("my.custom.host", requestHeaders.getFirst(new HttpString("X-Forwarded-Host")));
		// Exception gefangen -> Fallback zu Protokoll-Default (443)
		assertEquals("443", requestHeaders.getFirst(new HttpString("X-Forwarded-Port")));
	}

	@Test
	void testApply_MissingHostHeader_UsesConfiguredSpringDefaults() throws Exception {
		// Wir setzen keinen Host-Header im Request

		interceptor.apply(exchangeMock);

		// Fallback zu den in setUp() konfigurierten Werten aus der XML
		assertEquals("fallback.local.cxdev.me", requestHeaders.getFirst(new HttpString("X-Forwarded-Host")));
		assertEquals("8080", requestHeaders.getFirst(new HttpString("X-Forwarded-Port")));
		assertEquals("https", requestHeaders.getFirst(new HttpString("X-Forwarded-Proto")));
	}

	@Test
	void testApply_WithExistingForwardedHeaders_AppendsToForwardedFor() throws Exception {
		// Setup: Der Request kommt bereits durch einen anderen Proxy/Load Balancer
		HttpString forwardedForHeader = new HttpString("X-Forwarded-For");
		HttpString forwardedHostHeader = new HttpString("X-Forwarded-Host");
		HttpString forwardedProtoHeader = new HttpString("X-Forwarded-Proto");
		HttpString forwardedPortHeader = new HttpString("X-Forwarded-Port");

		requestHeaders.put(forwardedForHeader, "203.0.113.195");
		requestHeaders.put(forwardedHostHeader, "original.cxdev.me");
		requestHeaders.put(forwardedProtoHeader, "http");
		requestHeaders.put(forwardedPortHeader, "80");

		// Ausführung
		interceptor.apply(exchangeMock);

		// Assert 1: Die aktuelle Source-IP muss an die bestehende Kette angehängt werden
		String resultingForwardedFor = requestHeaders.getFirst(forwardedForHeader);
		assertEquals("203.0.113.195, 192.168.1.100", resultingForwardedFor,
				"Existing X-Forwarded-For must be preserved and the new IP appended");

		// Assert 2: Die anderen bestehenden Forwarded-Header dürfen nicht überschrieben werden,
		// da sie den echten Client-Ursprung (Original Host/Proto) repräsentieren.
		assertEquals("original.cxdev.me", requestHeaders.getFirst(forwardedHostHeader),
				"Existing X-Forwarded-Host should be preserved");
		assertEquals("http", requestHeaders.getFirst(forwardedProtoHeader),
				"Existing X-Forwarded-Proto should be preserved");
		assertEquals("80", requestHeaders.getFirst(forwardedPortHeader),
				"Existing X-Forwarded-Port should be preserved");
	}
}
