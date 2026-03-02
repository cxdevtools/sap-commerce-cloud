package me.cxdev.commerce.jwt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.SignedJWT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
class CxJwtTokenServiceTest {
	private CxJwtTokenService tokenService;

	@Mock
	private ResourceLoader resourceLoaderMock;

	@Mock
	private JWKSource<SecurityContext> jwkSourceMock;

	@Mock
	private Resource resourceMock;

	private RSAKey testRsaJwk;

	@BeforeEach
	void setUp() throws Exception {
		tokenService = new CxJwtTokenService();
		tokenService.setResourceLoader(resourceLoaderMock);
		tokenService.setTemplatePathPrefix("cxdevproxy/jwt");

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		testRsaJwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
				.privateKey((RSAPrivateKey) keyPair.getPrivate())
				.keyID("test-key-123")
				.build();
	}

	// --- JWKSource & Initialization Tests ---

	@Test
	void testAfterPropertiesSet_LoadsPrivateKeySuccessfully() throws Exception {
		tokenService.setJwkSource(jwkSourceMock);
		when(jwkSourceMock.get(any(), any())).thenReturn(Collections.singletonList((JWK) testRsaJwk));

		tokenService.afterPropertiesSet();

		lenient().when(resourceLoaderMock.getResource(anyString())).thenReturn(resourceMock);
		lenient().when(resourceMock.exists()).thenReturn(false);

		assertNull(tokenService.getOrGenerateToken("customer", "john.doe@example.com"));
		verify(resourceLoaderMock).getResource(anyString());
	}

	@Test
	void testGetOrGenerateToken_WithoutPrivateKey_ReturnsNullImmediately() {
		String token = tokenService.getOrGenerateToken("customer", "jane.doe@example.com");

		assertNull(token, "Should return null if no private key is loaded");
		verify(resourceLoaderMock, times(0)).getResource(anyString());
	}

	// --- Token Generation & Caching Tests ---

	@Test
	void testGenerateSignedToken_WithValidTemplate_ReturnsValidJwt() throws Exception {
		tokenService.setJwkSource(jwkSourceMock);
		when(jwkSourceMock.get(any(), any())).thenReturn(Collections.singletonList((JWK) testRsaJwk));
		tokenService.afterPropertiesSet();

		String jsonTemplate = "{\"sub\": \"john.doe@example.com\", \"roles\": [\"b2bcustomergroup\"]}";
		when(resourceLoaderMock.getResource("classpath:cxdevproxy/jwt/customer/john.doe@example.com.json")).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(true);
		when(resourceMock.getInputStream()).thenReturn(new ByteArrayInputStream(jsonTemplate.getBytes(StandardCharsets.UTF_8)));

		String jwtString = tokenService.generateSignedToken("customer", "john.doe@example.com");

		assertNotNull(jwtString, "Generated token should not be null");

		SignedJWT parsedJwt = SignedJWT.parse(jwtString);
		assertEquals("test-key-123", parsedJwt.getHeader().getKeyID(), "Key ID must match");
		assertEquals("john.doe@example.com", parsedJwt.getJWTClaimsSet().getSubject(), "Subject must match template");
		assertEquals("cxdevproxy", parsedJwt.getJWTClaimsSet().getIssuer(), "Issuer must be set by service");
	}

	@Test
	void testGetOrGenerateToken_CachesTokenCorrectly() throws Exception {
		tokenService.setJwkSource(jwkSourceMock);
		when(jwkSourceMock.get(any(), any())).thenReturn(Collections.singletonList((JWK) testRsaJwk));
		tokenService.afterPropertiesSet();

		String jsonTemplate = "{\"sub\": \"cached.user@example.com\"}";
		when(resourceLoaderMock.getResource(anyString())).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(true);
		when(resourceMock.getInputStream()).thenReturn(new ByteArrayInputStream(jsonTemplate.getBytes(StandardCharsets.UTF_8)));

		String firstCallToken = tokenService.getOrGenerateToken("customer", "cached.user@example.com");
		assertNotNull(firstCallToken);
		verify(resourceLoaderMock, times(1)).getResource(anyString()); // Template wurde geladen

		String secondCallToken = tokenService.getOrGenerateToken("customer", "cached.user@example.com");
		assertEquals(firstCallToken, secondCallToken, "Must return the exact same token from cache");
		verify(resourceLoaderMock, times(1)).getResource(anyString()); // Template wurde NICHT noch einmal geladen
	}

	@Test
	void testGetOrGenerateToken_WithExpiredCache_GeneratesNewToken() throws Exception {
		// Trick: We manipulate the token validity to 10 seconds.
		// This makes expiry = now + 10s - 60s (safety buffer) = now - 50s.
		// Thus, the token is already "expired" the moment it enters the cache.
		tokenService.setTokenValidity("1m");

		// Setup: Initialize Key & JWKSource
		tokenService.setJwkSource(jwkSourceMock);
		when(jwkSourceMock.get(any(), any())).thenReturn(Collections.singletonList((JWK) testRsaJwk));
		tokenService.afterPropertiesSet();

		// Setup: Mock Template
		String jsonTemplate = "{\"sub\": \"expired.user@example.com\"}";
		when(resourceLoaderMock.getResource(anyString())).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(true);

		// Since the resource is read twice, we must provide a fresh InputStream each time
		when(resourceMock.getInputStream()).thenAnswer(inv -> new ByteArrayInputStream(jsonTemplate.getBytes(StandardCharsets.UTF_8)));

		// 1st call: Generates the token and puts it (already expired) into the cache
		String firstCallToken = tokenService.getOrGenerateToken("customer", "expired.user@example.com");
		assertNotNull(firstCallToken);

		// Verify that the template was loaded exactly once
		verify(resourceLoaderMock, times(1)).getResource(anyString());

		// Short pause (10ms) to ensure the "issueTime" timestamp of the new JWT is strictly different
		Thread.sleep(50);

		// 2nd call: Finds the token in cache, detects it is invalid, and regenerates it
		String secondCallToken = tokenService.getOrGenerateToken("customer", "expired.user@example.com");
		assertNotNull(secondCallToken);

		// Assert 1: The template must have been loaded a SECOND time
		verify(resourceLoaderMock, times(2)).getResource(anyString());

		// Assert 2: The two token strings must differ because a completely new signature
		// with a new timestamp was generated.
		assertNotSame(firstCallToken, secondCallToken,
				"Expired token should be discarded and a completely new one generated");
	}
}
