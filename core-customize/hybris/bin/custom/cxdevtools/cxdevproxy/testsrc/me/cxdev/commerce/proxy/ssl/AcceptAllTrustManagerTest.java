package me.cxdev.commerce.proxy.ssl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.Socket;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AcceptAllTrustManagerTest {
	private AcceptAllTrustManager trustManager;

	@Mock
	private X509Certificate mockCertificate;

	@Mock
	private Socket mockSocket;

	@Mock
	private SSLEngine mockEngine;

	@BeforeEach
	void setUp() {
		trustManager = new AcceptAllTrustManager();
	}

	// --- Standard X509TrustManager Methods ---

	@Test
	void testCheckClientTrusted_NeverThrowsException() {
		X509Certificate[] validChain = new X509Certificate[] { mockCertificate };

		assertDoesNotThrow(() -> trustManager.checkClientTrusted(validChain, "RSA"));
		assertDoesNotThrow(() -> trustManager.checkClientTrusted(null, null));
	}

	@Test
	void testCheckServerTrusted_NeverThrowsException() {
		X509Certificate[] validChain = new X509Certificate[] { mockCertificate };

		assertDoesNotThrow(() -> trustManager.checkServerTrusted(validChain, "RSA"));
		assertDoesNotThrow(() -> trustManager.checkServerTrusted(null, null));
	}

	// --- Extended X509ExtendedTrustManager Methods (Socket) ---

	@Test
	void testCheckClientTrustedWithSocket_NeverThrowsException() {
		X509Certificate[] validChain = new X509Certificate[] { mockCertificate };

		assertDoesNotThrow(() -> trustManager.checkClientTrusted(validChain, "RSA", mockSocket),
				"Should silently accept valid chains with a Socket");
		assertDoesNotThrow(() -> trustManager.checkClientTrusted(null, null, (Socket) null),
				"Should silently accept null values with a null Socket");
	}

	@Test
	void testCheckServerTrustedWithSocket_NeverThrowsException() {
		X509Certificate[] validChain = new X509Certificate[] { mockCertificate };

		assertDoesNotThrow(() -> trustManager.checkServerTrusted(validChain, "RSA", mockSocket),
				"Should silently accept valid chains with a Socket");
		assertDoesNotThrow(() -> trustManager.checkServerTrusted(null, null, (Socket) null),
				"Should silently accept null values with a null Socket");
	}

	// --- Extended X509ExtendedTrustManager Methods (SSLEngine) ---

	@Test
	void testCheckClientTrustedWithEngine_NeverThrowsException() {
		X509Certificate[] validChain = new X509Certificate[] { mockCertificate };

		assertDoesNotThrow(() -> trustManager.checkClientTrusted(validChain, "RSA", mockEngine),
				"Should silently accept valid chains with an SSLEngine");
		assertDoesNotThrow(() -> trustManager.checkClientTrusted(null, null, (SSLEngine) null),
				"Should silently accept null values with a null SSLEngine");
	}

	@Test
	void testCheckServerTrustedWithEngine_NeverThrowsException() {
		X509Certificate[] validChain = new X509Certificate[] { mockCertificate };

		assertDoesNotThrow(() -> trustManager.checkServerTrusted(validChain, "RSA", mockEngine),
				"Should silently accept valid chains with an SSLEngine");
		assertDoesNotThrow(() -> trustManager.checkServerTrusted(null, null, (SSLEngine) null),
				"Should silently accept null values with a null SSLEngine");
	}

	// --- Array Return Method ---

	@Test
	void testGetAcceptedIssuers_ReturnsEmptyArray() {
		X509Certificate[] issuers = trustManager.getAcceptedIssuers();

		assertNotNull(issuers, "Accepted issuers should not be null to prevent NullPointerExceptions");
		assertEquals(0, issuers.length, "Accepted issuers array should be empty");
	}
}
