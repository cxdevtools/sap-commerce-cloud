package me.cxdev.commerce.proxy.handler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.Methods;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
class StaticContentHandlerTest {
	private StaticContentHandler handler;

	@Mock
	private ResourceLoader resourceLoaderMock;

	@Mock
	private HttpServerExchange exchangeMock;

	@Mock
	private Resource resourceMock;

	private HeaderMap responseHeaders;

	@BeforeEach
	void setUp() {
		// We use a clean normalized base location for testing
		handler = new StaticContentHandler("classpath:ui/public");
		handler.setResourceLoader(resourceLoaderMock);

		responseHeaders = new HeaderMap();
	}

	// --- matches() Tests ---

	@Test
	void testMatches_WithValidGetRequest_ReturnsTrue() {
		when(exchangeMock.getRequestMethod()).thenReturn(Methods.GET);
		when(exchangeMock.getRequestPath()).thenReturn("/style.css");

		when(resourceLoaderMock.getResource("classpath:ui/public/style.css")).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(true);
		when(resourceMock.isReadable()).thenReturn(true);

		assertTrue(handler.matches(exchangeMock), "Should match a valid GET request for a readable file");
	}

	@Test
	void testMatches_WithNonGetMethod_ReturnsFalse() {
		when(exchangeMock.getRequestMethod()).thenReturn(Methods.POST);

		assertFalse(handler.matches(exchangeMock), "Should strictly ignore non-GET methods like POST");
	}

	@Test
	void testMatches_WithRootPath_ReturnsFalse() {
		when(exchangeMock.getRequestMethod()).thenReturn(Methods.GET);
		when(exchangeMock.getRequestPath()).thenReturn("/");

		assertFalse(handler.matches(exchangeMock), "Should ignore the root path '/' to allow index rendering handlers to take over");
	}

	@Test
	void testMatches_WithUnreadableOrMissingResource_ReturnsFalse() {
		when(exchangeMock.getRequestMethod()).thenReturn(Methods.GET);
		when(exchangeMock.getRequestPath()).thenReturn("/missing.png");

		when(resourceLoaderMock.getResource("classpath:ui/public/missing.png")).thenReturn(resourceMock);

		// Simulate missing file
		when(resourceMock.exists()).thenReturn(false);
		assertFalse(handler.matches(exchangeMock), "Should not match if the resource does not exist");

		// Simulate existing but unreadable file (e.g., a directory)
		when(resourceMock.exists()).thenReturn(true);
		when(resourceMock.isReadable()).thenReturn(false);
		assertFalse(handler.matches(exchangeMock), "Should not match if the resource is a directory or unreadable");
	}

	// --- handleRequest() Tests ---

	@Test
	void testHandleRequest_InIoThread_DispatchesAndReturns() {
		when(exchangeMock.isInIoThread()).thenReturn(true);

		handler.handleRequest(exchangeMock);

		// Assert that the handler dispatches itself to a worker thread and immediately returns
		// without trying to read paths or resources (which would block the IO thread).
		verify(exchangeMock).dispatch(handler);
		verify(exchangeMock, never()).getRequestPath();
	}

	@Test
	void testHandleRequest_ResourceDisappeared_Returns404() {
		when(exchangeMock.isInIoThread()).thenReturn(false);
		when(exchangeMock.getRequestPath()).thenReturn("/style.css");

		when(resourceLoaderMock.getResource(anyString())).thenReturn(resourceMock);

		// Edge case: File existed during matches(), but was deleted right before handleRequest()
		when(resourceMock.exists()).thenReturn(false);

		handler.handleRequest(exchangeMock);

		verify(exchangeMock).setStatusCode(404);
		verify(exchangeMock, never()).startBlocking();
	}

	@Test
	void testHandleRequest_ServesPlainFile() throws Exception {
		executeSuccessfulFileDeliveryTest("/file", "text/plain", "Lorem ipsum");
	}

	@Test
	void testHandleRequest_ServesCssFile() throws Exception {
		executeSuccessfulFileDeliveryTest("/styles/main.css", "text/css", "body { color: red; }");
	}

	@Test
	void testHandleRequest_ServesJsFile() throws Exception {
		executeSuccessfulFileDeliveryTest("/scripts/app.js", "application/javascript", "console.log('Hello');");
	}

	@Test
	void testHandleRequest_ServesUnknownExtension_DefaultsToOctetStream() throws Exception {
		executeSuccessfulFileDeliveryTest("/downloads/data.unknown", "application/octet-stream", "raw binary data");
	}

	@Test
	void testHandleRequest_OnIoException_Returns500() throws Exception {
		when(exchangeMock.isInIoThread()).thenReturn(false);
		when(exchangeMock.getRequestPath()).thenReturn("/broken.css");

		when(resourceLoaderMock.getResource(anyString())).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(true);
		when(resourceMock.isReadable()).thenReturn(true);

		Mockito.lenient().when(exchangeMock.getResponseHeaders()).thenReturn(responseHeaders);

		// Simulate an IOException while opening the file stream
		when(resourceMock.getInputStream()).thenThrow(new IOException("File locked by OS"));

		// Ensure the handler knows the response hasn't been committed yet
		when(exchangeMock.isResponseStarted()).thenReturn(false);

		handler.handleRequest(exchangeMock);

		// The handler must catch the exception and return a 500 Internal Server Error
		verify(exchangeMock).setStatusCode(500);
	}

	/**
	 * Helper method to simulate a successful file download of a specific type.
	 */
	private void executeSuccessfulFileDeliveryTest(String requestPath, String expectedMimeType, String fileContent) throws Exception {
		// 1. Setup Request Phase
		when(exchangeMock.isInIoThread()).thenReturn(false);
		when(exchangeMock.getRequestPath()).thenReturn(requestPath);

		// 2. Setup Resource Loading
		when(resourceLoaderMock.getResource("classpath:ui/public" + requestPath)).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(true);
		when(resourceMock.isReadable()).thenReturn(true);

		// 3. Setup Response Streams & Headers
		Mockito.lenient().when(exchangeMock.getResponseHeaders()).thenReturn(responseHeaders);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		when(exchangeMock.getOutputStream()).thenReturn(outputStream);

		byte[] contentBytes = fileContent.getBytes();
		when(resourceMock.getInputStream()).thenReturn(new ByteArrayInputStream(contentBytes));

		// 4. Execution
		handler.handleRequest(exchangeMock);

		// 5. Assertions
		verify(exchangeMock).setStatusCode(200);
		verify(exchangeMock).startBlocking(); // Vital for Undertow output streams

		assertEquals(expectedMimeType, responseHeaders.getFirst(Headers.CONTENT_TYPE),
				"MIME type must correctly map the file extension");
		assertArrayEquals(contentBytes, outputStream.toByteArray(),
				"The file content must be perfectly copied to the Undertow output stream");
	}
}
