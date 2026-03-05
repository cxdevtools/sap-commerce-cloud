package me.cxdev.commerce.proxy.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.LinkedHashSet;
import java.util.Set;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import me.cxdev.commerce.jwt.service.CxJwtTokenService;

@ExtendWith(MockitoExtension.class)
class JwtInjectorInterceptorTest {
	@Mock
	private CxJwtTokenService jwtTokenServiceMock;

	@Mock
	private HttpServerExchange exchangeMock;

	@InjectMocks
	private JwtInjectorInterceptor interceptor;

	private HeaderMap requestHeaders;
	private HeaderMap responseHeaders;
	private Set<Cookie> requestCookies;

	@BeforeEach
	void setUp() {
		// Undertow Maps initialisieren
		requestHeaders = new HeaderMap();
		responseHeaders = new HeaderMap();
		requestCookies = new LinkedHashSet<>();

		// Lenient Stubbing, da nicht jeder Test beide Maps zwingend benötigt
		Mockito.lenient().when(exchangeMock.getRequestHeaders()).thenReturn(requestHeaders);
		Mockito.lenient().when(exchangeMock.getResponseHeaders()).thenReturn(responseHeaders);
		Mockito.lenient().when(exchangeMock.requestCookies()).thenReturn(requestCookies);
		Mockito.lenient().when(exchangeMock.getRequestCookie(anyString())).thenCallRealMethod();
	}

	@Test
	void testApply_InjectsTokenSuccessfully() throws Exception {
		// 1. Setup: Cookies sind vorhanden
		requestCookies.add(new CookieImpl("cxdevproxy_user_id", "customer@cxdev.me"));
		requestCookies.add(new CookieImpl("cxdevproxy_user_type", "customer"));

		// 2. Setup: TokenService liefert ein valides Token
		when(jwtTokenServiceMock.getOrGenerateToken("customer", "customer@cxdev.me"))
				.thenReturn("mocked.jwt.token");

		// 3. Ausführung
		interceptor.apply(exchangeMock);

		// 4. Assert: Der Header muss korrekt mit "Bearer " Präfix gesetzt sein
		assertEquals("Bearer mocked.jwt.token", requestHeaders.getFirst(Headers.AUTHORIZATION),
				"Authorization header should contain the Bearer token");
	}

	@Test
	void testApply_MissingUserIdCookie_DoesNothing() throws Exception {
		// Nur der Typ-Cookie ist da, aber keine ID
		requestCookies.add(new CookieImpl("cxdevproxy_user_type", "customer"));

		interceptor.apply(exchangeMock);

		// TokenService darf nicht aufgerufen werden
		verify(jwtTokenServiceMock, never()).getOrGenerateToken(anyString(), anyString());

		// Kein Header darf gesetzt werden
		assertFalse(requestHeaders.contains(Headers.AUTHORIZATION),
				"Authorization header should not be set if user_id is missing");
	}

	@Test
	void testApply_MissingUserTypeCookie_DoesNothing() throws Exception {
		// Nur der ID-Cookie ist da, aber kein Typ
		requestCookies.add(new CookieImpl("cxdevproxy_user_id", "customer@cxdev.me"));

		interceptor.apply(exchangeMock);

		// TokenService darf nicht aufgerufen werden
		verify(jwtTokenServiceMock, never()).getOrGenerateToken(anyString(), anyString());

		// Kein Header darf gesetzt werden
		assertFalse(requestHeaders.contains(Headers.AUTHORIZATION),
				"Authorization header should not be set if user_type is missing");
	}

	@Test
	void testApply_TokenServiceReturnsNull_DoesNothing() throws Exception {
		// Cookies sind vorhanden
		requestCookies.add(new CookieImpl("cxdevproxy_user_id", "invalid@cxdev.me"));
		requestCookies.add(new CookieImpl("cxdevproxy_user_type", "customer"));

		// TokenService schlägt fehl / findet kein Template und gibt null zurück
		when(jwtTokenServiceMock.getOrGenerateToken("customer", "invalid@cxdev.me")).thenReturn(null);

		interceptor.apply(exchangeMock);

		// Kein Header darf gesetzt werden, da kein Token generiert wurde
		assertFalse(requestHeaders.contains(Headers.AUTHORIZATION),
				"Authorization header should not be set if generated token is null");
	}
}
