package me.cxdev.commerce.proxy.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Service responsible for loading JWT templates, generating signed tokens, and caching them.
 * <p>
 * It uses the local domain's private key (extracted from the PKCS12 keystore) to sign the
 * tokens using the RS256 algorithm. Static claims are loaded from JSON templates in the
 * classpath, while dynamic claims (like iat, exp) are calculated on the fly.
 * </p>
 */
public class JwtTokenService implements InitializingBean {
	private static final Logger LOG = LoggerFactory.getLogger(JwtTokenService.class);
	private static final long TOKEN_VALIDITY_MS = 3600 * 1000L; // 1 hour validity

	private String keystorePath;
	private String keystorePassword;
	private String keystoreAlias;

	private PrivateKey privateKey;
	private final Map<String, CachedToken> tokenCache = new ConcurrentHashMap<>();

	@Override
	public void afterPropertiesSet() throws Exception {
		loadPrivateKey();
	}

	/**
	 * Extracts the private key from the configured PKCS12 keystore.
	 */
	private void loadPrivateKey() throws Exception {
		if (StringUtils.isBlank(keystorePath)) {
			LOG.warn("Keystore path not configured. JWT signing will not work.");
			return;
		}

		File keystoreFile = new File(keystorePath.trim());
		if (!keystoreFile.exists()) {
			LOG.error("Keystore not found at {}. JWT signing disabled.", keystoreFile.getAbsolutePath());
			return;
		}

		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		char[] password = keystorePassword != null ? keystorePassword.toCharArray() : new char[0];

		try (InputStream is = new FileInputStream(keystoreFile)) {
			keyStore.load(is, password);
		}

		KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
				keystoreAlias, new KeyStore.PasswordProtection(password));

		if (privateKeyEntry != null) {
			this.privateKey = privateKeyEntry.getPrivateKey();
			LOG.info("Successfully loaded private key for alias '{}' for JWT signing.", keystoreAlias);
		} else {
			LOG.error("Could not find private key for alias '{}'.", keystoreAlias);
		}
	}

	/**
	 * Retrieves a signed JWT for the given user ID.
	 * Uses a cached token if it is still valid; otherwise, generates a new one.
	 *
	 * @param userType The type of the user (e.g., "employee").
	 * @param userId The ID of the user (e.g., "admin").
	 * @return The signed JWT as a Base64 encoded string, or null if generation fails.
	 */
	public String getOrGenerateToken(String userType, String userId) {
		if (privateKey == null) {
			return null;
		}

		CachedToken cached = tokenCache.get(userId);
		if (cached != null && cached.isValid()) {
			return cached.getToken();
		}

		String newToken = generateSignedToken(userType, userId);
		if (newToken != null) {
			// Cache token to expire slightly before the actual JWT expiry to avoid edge cases
			tokenCache.put(userId, new CachedToken(newToken, System.currentTimeMillis() + TOKEN_VALIDITY_MS - 60000));
		}
		return newToken;
	}

	/**
	 * Reads the JSON template, appends dynamic claims, and signs the JWT.
	 */
	private String generateSignedToken(String userType, String userId) {
		String templatePath = "cxdevproxy/jwt/" + userType + "/" + userId + ".json";
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(templatePath)) {
			if (is == null) {
				LOG.warn("No JWT template found for user at classpath: {}", templatePath);
				return null;
			}

			String jsonContent = IOUtils.toString(is, StandardCharsets.UTF_8);

			// Parse static claims from the JSON file
			JWTClaimsSet templateClaims = JWTClaimsSet.parse(jsonContent);

			// Calculate dynamic validity
			Date now = new Date();
			Date expiry = new Date(now.getTime() + TOKEN_VALIDITY_MS);

			// Merge claims
			JWTClaimsSet finalClaims = new JWTClaimsSet.Builder(templateClaims)
					.issueTime(now)
					.expirationTime(expiry)
					.issuer("cxdevproxy")
					.build();

			// Sign the JWT
			JWSSigner signer = new RSASSASigner(this.privateKey);
			JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keystoreAlias).build();

			SignedJWT signedJWT = new SignedJWT(header, finalClaims);
			signedJWT.sign(signer);

			LOG.debug("Successfully generated new signed JWT for user '{}'", userId);
			return signedJWT.serialize();

		} catch (Exception e) {
			LOG.error("Failed to generate JWT for user '{}'", userId, e);
			return null;
		}
	}

	public void setKeystorePath(String keystorePath) {
		this.keystorePath = keystorePath;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	public void setKeystoreAlias(String keystoreAlias) {
		this.keystoreAlias = keystoreAlias;
	}

	/**
	 * Internal wrapper to hold a cached token and its local expiration time.
	 */
	private static class CachedToken {
		private final String token;
		private final long expiresAt;

		public CachedToken(String token, long expiresAt) {
			this.token = token;
			this.expiresAt = expiresAt;
		}

		public String getToken() {
			return token;
		}

		public boolean isValid() {
			return System.currentTimeMillis() < expiresAt;
		}
	}
}
