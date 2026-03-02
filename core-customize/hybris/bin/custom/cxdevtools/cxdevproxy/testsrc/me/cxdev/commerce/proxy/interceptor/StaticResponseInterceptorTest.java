package me.cxdev.commerce.proxy.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.core.MediaType;

@ExtendWith(MockitoExtension.class)
class StaticResponseInterceptorTest {
	@Mock
	private HttpServerExchange exchangeMock;

	@Mock
	private Sender senderMock;

	private HeaderMap responseHeaders;

	@BeforeEach
	void setUp() {
		responseHeaders = new HeaderMap();

		Mockito.lenient().when(exchangeMock.getResponseHeaders()).thenReturn(responseHeaders);
		Mockito.lenient().when(exchangeMock.getResponseSender()).thenReturn(senderMock);
	}

	@Test
	void testApply_SetsStatusCodeContentTypeAndPayload() throws Exception {
		int statusCode = 200;
		String contentType = "application/json";
		String payload = "{\"status\": \"mocked\", \"data\": []}";

		ProxyExchangeInterceptor interceptor = Interceptors.staticResponse(statusCode, contentType, payload);

		interceptor.apply(exchangeMock);
		verify(exchangeMock).setStatusCode(statusCode);
		assertEquals(contentType, responseHeaders.getFirst(Headers.CONTENT_TYPE),
				"Content-Type header should match the configured value");
		verify(senderMock).send(payload);
	}

	@Test
	void testApply_WithNullPayload_HandlesGracefully() throws Exception {
		ProxyExchangeInterceptor interceptor = Interceptors.staticResponse(404, "text/plain", null);

		interceptor.apply(exchangeMock);

		verify(exchangeMock).setStatusCode(404);
		assertEquals("text/plain", responseHeaders.getFirst(Headers.CONTENT_TYPE));
		verify(senderMock).send("");
	}

	@Test
	void testApply_WithEmptyContentType_IgnoresHeader() throws Exception {
		ProxyExchangeInterceptor interceptor = Interceptors.staticResponse(204, "", "");

		interceptor.apply(exchangeMock);

		verify(exchangeMock).setStatusCode(204);
		assertEquals("text/plain", responseHeaders.getFirst(Headers.CONTENT_TYPE));
		verify(senderMock).send("");
	}

	@Test
	void testApply_WithJsonResponse_HandlesGracefully() throws Exception {
		ProxyExchangeInterceptor interceptor = Interceptors.jsonResponse("{}");

		interceptor.apply(exchangeMock);

		verify(exchangeMock).setStatusCode(200);
		assertEquals(MediaType.APPLICATION_JSON, responseHeaders.getFirst(Headers.CONTENT_TYPE));
		verify(senderMock).send("{}");
	}

	@Test
	void testApply_WithHtmlResponse_HandlesGracefully() throws Exception {
		ProxyExchangeInterceptor interceptor = Interceptors.htmlResponse("<HTML><BODY>TEST</BODY></HTML>");

		interceptor.apply(exchangeMock);

		verify(exchangeMock).setStatusCode(200);
		assertEquals(MediaType.TEXT_HTML, responseHeaders.getFirst(Headers.CONTENT_TYPE));
		verify(senderMock).send("<HTML><BODY>TEST</BODY></HTML>");
	}
}
