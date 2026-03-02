package me.cxdev.commerce.proxy.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;

import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartupPageHandlerTest {

	private StartupPageHandler handler;

	@Mock
	private HttpServerExchange exchangeMock;

	@Mock
	private Sender senderMock;

	@Mock
	private Tenant masterTenantMock;

	@Mock
	private Tenant someOtherTenantMock;

	private HeaderMap requestHeaders;
	private HeaderMap responseHeaders;

	@BeforeEach
	void setUp() {
		handler = new StartupPageHandler();

		requestHeaders = new HeaderMap();
		responseHeaders = new HeaderMap();
	}

	// --- Lifecycle & TenantListener Tests ---

	@Test
	void testAfterPropertiesSet_RegistersListener() {
		// Setup: Mock the static Registry class
		try (MockedStatic<Registry> registryStatic = Mockito.mockStatic(Registry.class)) {
			handler.afterPropertiesSet();

			// Assert: The handler must register itself
			registryStatic.verify(() -> Registry.registerTenantListener(handler), times(1));
		}
	}

	// --- Request Handling & i18n Tests ---

	@Test
	void testHandleRequest_WithEnglishLocale_Returns503AndHtml() {
		requestHeaders.put(Headers.ACCEPT_LANGUAGE, "en-US,en;q=0.9");
		setupExchangeMocks();

		handler.handleRequest(exchangeMock);

		// Assert 1: HTTP Status 503
		verify(exchangeMock).setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);

		// Assert 2: Content-Type is text/html
		assertEquals("text/html; charset=UTF-8", responseHeaders.getFirst(Headers.CONTENT_TYPE));

		// Assert 3: The HTML payload is sent
		ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
		verify(senderMock).send(htmlCaptor.capture());

		String html = htmlCaptor.getValue();
		assertTrue(html.contains("<!DOCTYPE html>"), "Must be a valid HTML document");
		assertTrue(html.contains("<meta http-equiv=\"refresh\" content=\"5\">"), "Must auto-refresh");

		// As long as we don't strictly load a real ResourceBundle in this test classpath,
		// we assert that it gracefully falls back to the hardcoded default English texts.
		assertTrue(html.contains("Starting up..."), "Should contain the English default title");
	}

	@Test
	void testHandleRequest_WithGermanLocale_DoesNotCrash() {
		// Ensure that providing a German locale doesn't crash the ResourceBundle lookup
		requestHeaders.put(Headers.ACCEPT_LANGUAGE, "de-DE,de;q=0.9");
		setupExchangeMocks();

		handler.handleRequest(exchangeMock);

		ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
		verify(senderMock).send(htmlCaptor.capture());

		// If the German bundle is present in the test context, it will use it.
		// Otherwise, it gracefully uses the fallback. The main goal here is to ensure no exceptions escape.
		assertTrue(htmlCaptor.getValue().contains("<html lang=\"en\">"));
	}

	@Test
	void testHandleRequest_WithMissingAcceptLanguage_DefaultsToEnglish() {
		// No Accept-Language header set
		setupExchangeMocks();

		handler.handleRequest(exchangeMock);

		ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
		verify(senderMock).send(htmlCaptor.capture());

		assertTrue(htmlCaptor.getValue().contains("Starting up..."), "Should gracefully default to English");
	}

	private void setupExchangeMocks() {
		Mockito.lenient().when(exchangeMock.getRequestHeaders()).thenReturn(requestHeaders);
		Mockito.lenient().when(exchangeMock.getResponseHeaders()).thenReturn(responseHeaders);
		Mockito.lenient().when(exchangeMock.getResponseSender()).thenReturn(senderMock);
	}
}
