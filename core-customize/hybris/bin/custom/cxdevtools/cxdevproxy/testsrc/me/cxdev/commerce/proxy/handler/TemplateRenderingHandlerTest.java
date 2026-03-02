package me.cxdev.commerce.proxy.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import de.hybris.platform.servicelayer.config.ConfigurationService;

import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.Methods;

import org.apache.commons.configuration2.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
class TemplateRenderingHandlerTest {

	private TemplateRenderingHandler handler;

	@Mock
	private ConfigurationService configurationServiceMock;

	@Mock
	private Configuration configurationMock;

	@Mock
	private MessageSource messageSourceMock;

	@Mock
	private ResourceLoader resourceLoaderMock;

	@Mock
	private HttpServerExchange exchangeMock;

	@Mock
	private Resource resourceMock;

	@Mock
	private Sender senderMock;

	private HeaderMap requestHeaders;
	private HeaderMap responseHeaders;

	@BeforeEach
	void setUp() {
		handler = new TemplateRenderingHandler("classpath:ui/templates", configurationServiceMock, messageSourceMock);
		handler.setResourceLoader(resourceLoaderMock);

		requestHeaders = new HeaderMap();
		responseHeaders = new HeaderMap();

		// Lenient mocks for standard exchange behavior
		Mockito.lenient().when(exchangeMock.getRequestHeaders()).thenReturn(requestHeaders);
		Mockito.lenient().when(exchangeMock.getResponseHeaders()).thenReturn(responseHeaders);
		Mockito.lenient().when(exchangeMock.getResponseSender()).thenReturn(senderMock);
		Mockito.lenient().when(configurationServiceMock.getConfiguration()).thenReturn(configurationMock);
	}

	// --- matches() Tests ---

	@Test
	void testMatches_WithValidHtmlGetRequest_ReturnsTrue() {
		when(exchangeMock.getRequestMethod()).thenReturn(Methods.GET);
		when(exchangeMock.getRequestPath()).thenReturn("/login.html");

		when(resourceLoaderMock.getResource("classpath:ui/templates/login.html")).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(true);
		when(resourceMock.isReadable()).thenReturn(true);

		assertTrue(handler.matches(exchangeMock), "Should match GET requests for existing .html files");
	}

	@Test
	void testMatches_WithNonHtmlExtension_ReturnsFalse() {
		when(exchangeMock.getRequestMethod()).thenReturn(Methods.GET);
		when(exchangeMock.getRequestPath()).thenReturn("/style.css");

		assertFalse(handler.matches(exchangeMock), "Should ignore non-html files (handled by StaticContentHandler)");
	}

	@Test
	void testMatches_WithNonGetMethod_ReturnsFalse() {
		when(exchangeMock.getRequestMethod()).thenReturn(Methods.POST);

		assertFalse(handler.matches(exchangeMock), "Should strictly ignore POST/PUT requests");
	}

	// --- handleRequest() Tests ---

	@Test
	void testHandleRequest_InIoThread_DispatchesAndReturns() {
		when(exchangeMock.isInIoThread()).thenReturn(true);

		handler.handleRequest(exchangeMock);

		// Assert that the handler dispatches itself to a worker thread to prevent blocking
		verify(exchangeMock).dispatch(handler);
		verify(exchangeMock, never()).getRequestPath();
	}

	@Test
	void testHandleRequest_ResourceDisappeared_Returns404() {
		when(exchangeMock.isInIoThread()).thenReturn(false);
		when(exchangeMock.getRequestPath()).thenReturn("/vanished.html");
		when(resourceLoaderMock.getResource(anyString())).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(false);

		handler.handleRequest(exchangeMock);

		verify(exchangeMock).setStatusCode(404);
		verify(senderMock).send("404 - Template not found");
	}

	@Test
	void testHandleRequest_RendersPropertiesAndI18nMessages() throws Exception {
		// Setup: Request
		when(exchangeMock.isInIoThread()).thenReturn(false);
		when(exchangeMock.getRequestPath()).thenReturn("/index.html");
		requestHeaders.put(Headers.ACCEPT_LANGUAGE, "de-DE,de;q=0.9,en;q=0.8");

		// Setup: Mock Resource and raw HTML content
		String rawHtml = "<div>%{my.property}</div> " +
				"<span>%{missing.prop:DefaultFallback}</span> " +
				"<h1>#{page.title}</h1> " +
				"<p>#{missing.msg:Default Msg}</p>";

		when(resourceLoaderMock.getResource(anyString())).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(true);
		when(resourceMock.getInputStream()).thenReturn(new ByteArrayInputStream(rawHtml.getBytes(StandardCharsets.UTF_8)));

		// Setup: Mock Configuration properties
		when(configurationMock.getString("my.property", null)).thenReturn("ResolvedPropValue");
		when(configurationMock.getString("missing.prop", "DefaultFallback")).thenReturn("DefaultFallback"); // Simulating
																											// missing
																											// prop

		// Setup: Mock i18n messages (expecting German locale based on header)
		Locale expectedLocale = Locale.forLanguageTag("de-DE");
		when(messageSourceMock.getMessage(eq("page.title"), isNull(), eq("page.title"), eq(expectedLocale)))
				.thenReturn("CX Dev Proxy - Mock Login");
		when(messageSourceMock.getMessage(eq("missing.msg"), isNull(), eq("Default Msg"), eq(expectedLocale)))
				.thenReturn("Default Msg"); // Simulating fallback

		// Execution
		handler.handleRequest(exchangeMock);

		// Assertions
		verify(exchangeMock).setStatusCode(200);

		ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
		verify(senderMock).send(htmlCaptor.capture());

		String renderedHtml = htmlCaptor.getValue();

		// Assert Property Resolution
		assertTrue(renderedHtml.contains("<div>ResolvedPropValue</div>"), "Should resolve known properties");
		assertTrue(renderedHtml.contains("<span>DefaultFallback</span>"), "Should use property default values if provided");

		// Assert i18n Resolution
		assertTrue(renderedHtml.contains("<h1>CX Dev Proxy - Mock Login</h1>"), "Should resolve known i18n messages in correct locale");
		assertTrue(renderedHtml.contains("<p>Default Msg</p>"), "Should use i18n default values if provided");
	}

	@Test
	void testHandleRequest_OnIoException_Returns500() throws Exception {
		when(exchangeMock.isInIoThread()).thenReturn(false);
		when(exchangeMock.getRequestPath()).thenReturn("/error.html");

		when(resourceLoaderMock.getResource(anyString())).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(true);
		when(resourceMock.getInputStream()).thenThrow(new IOException("Disk read error"));

		handler.handleRequest(exchangeMock);

		verify(exchangeMock).setStatusCode(500);
		verify(senderMock).send("500 - Internal Server Error rendering template");
	}

	@Test
	void testDetermineLocale_InvalidHeader_DefaultsToEnglish() throws Exception {
		// Setup request with completely broken language header
		when(exchangeMock.isInIoThread()).thenReturn(false);
		when(exchangeMock.getRequestPath()).thenReturn("/test.html");
		requestHeaders.put(Headers.ACCEPT_LANGUAGE, null);

		when(resourceLoaderMock.getResource(anyString())).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(true);
		when(resourceMock.getInputStream()).thenReturn(new ByteArrayInputStream("#{test.msg}".getBytes(StandardCharsets.UTF_8)));

		handler.handleRequest(exchangeMock);

		// Verify that the MessageSource is called with Locale.ENGLISH as the ultimate fallback
		verify(messageSourceMock).getMessage(anyString(), isNull(), anyString(), eq(Locale.ENGLISH));
	}
}
