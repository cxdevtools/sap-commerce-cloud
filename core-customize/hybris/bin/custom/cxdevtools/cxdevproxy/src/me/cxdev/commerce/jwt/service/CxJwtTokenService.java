package me.cxdev.commerce.jwt.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import me.cxdev.commerce.proxy.util.ResourcePathUtils;
import me.cxdev.commerce.proxy.util.TimeUtils;

/**
 * Service responsible for loading JWT templates, generating signed tokens, and caching them.
 * <p>
 * It strictly relies on the platform's JWKSource (from the authorizationserver) to sign tokens
 * with the exact same key the backend uses, ensuring native trust without additional configuration.
 * </p>
 */
public class CxJwtTokenService implements JwtTokenService, InitializingBean, ResourceLoaderAware {
	private static final Logger LOG = LoggerFactory.getLogger(CxJwtTokenService.class);

	private ResourceLoader resourceLoader;
	private String templatePathPrefix = "classpath:cxdevproxy/jwt";
	private long tokenValidityMs = 3600 * 1000L;
	private JWKSource<SecurityContext> jwkSource;
	private String activeKeyId;
	private PrivateKey privateKey;
	private final Map<String, CachedToken> tokenCache = new ConcurrentHashMap<>();

	@Override
	public String getOrGenerateToken(String userType, String userId) {
		if (privateKey == null) {
			return null;
		}

		String cacheKey = userType + ":" + userId;
		CachedToken cached = tokenCache.get(cacheKey);

		if (cached != null && cached.isValid()) {
			return cached.getToken();
		}

		String newToken = generateSignedToken(userType, userId);
		if (newToken != null) {
			// Cache expires 1 minute before the actual token to avoid edge cases
			long cacheExpiry = System.currentTimeMillis() + this.tokenValidityMs - 60000;
			tokenCache.put(cacheKey, new CachedToken(newToken, cacheExpiry));
		}
		return newToken;
	}

	@Override
	public String generateSignedToken(String userType, String userId) {
		String normalizedPrefix = templatePathPrefix.endsWith("/") ? templatePathPrefix : templatePathPrefix + "/";
		String templatePath = normalizedPrefix + userType + "/" + userId + ".json";

		try {
			Resource resource = resourceLoader.getResource(templatePath);
			if (!resource.exists()) {
				LOG.warn("No JWT template found for user at path: {}", templatePath);
				return null;
			}

			String jsonContent;
			try (InputStream is = resource.getInputStream()) {
				jsonContent = IOUtils.toString(is, StandardCharsets.UTF_8);
			}

			JWTClaimsSet templateClaims = JWTClaimsSet.parse(jsonContent);

			Date now = new Date();
			Date expiry = new Date(now.getTime() + this.tokenValidityMs);

			JWTClaimsSet finalClaims = new JWTClaimsSet.Builder(templateClaims)
					.notBeforeTime(now)
					.issueTime(now)
					.expirationTime(expiry)
					.issuer("cxdevproxy")
					.build();

			JWSSigner signer = new RSASSASigner(this.privateKey);
			JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
					.type(JOSEObjectType.JWT)
					.keyID(this.activeKeyId)
					.build();

			SignedJWT signedJWT = new SignedJWT(header, finalClaims);
			signedJWT.sign(signer);

			LOG.debug("Successfully generated natively-trusted signed JWT for user '{}' of type '{}'", userId, userType);
			return signedJWT.serialize();

		} catch (Exception e) {
			LOG.error("Failed to generate JWT for user '{}' of type '{}'", userId, userType, e);
			return null;
		}
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setTemplatePathPrefix(String templatePathPrefix) {
		this.templatePathPrefix = ResourcePathUtils.normalizeDirectoryPath(templatePathPrefix, "Template path in JwtTokenService");
	}

	public void setTokenValidity(String validity) {
		this.tokenValidityMs = TimeUtils.parseIntervalToMillis(validity, "Token validity for JwtTokenService");
	}

	public void setJwkSource(JWKSource<SecurityContext> jwkSource) {
		this.jwkSource = jwkSource;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (jwkSource != null) {
			loadPrivateKeyFromJwkSource();
		} else {
			LOG.warn("No JWKSource injected. JWT signing will be disabled for the proxy.");
		}
	}

	private void loadPrivateKeyFromJwkSource() {
		try {
			JWKSelector selector = new JWKSelector(new JWKMatcher.Builder().build());
			List<JWK> jwks = jwkSource.get(selector, null);

			if (jwks != null && !jwks.isEmpty()) {
				for (JWK jwk : jwks) {
					if (jwk instanceof RSAKey && jwk.isPrivate()) {
						this.privateKey = ((RSAKey) jwk).toPrivateKey();
						this.activeKeyId = jwk.getKeyID();
						LOG.info("Successfully loaded private key from injected JWKSource (kid: {}). Mock tokens will be natively trusted!", this.activeKeyId);
						return;
					}
				}
			}
			LOG.error("Injected JWKSource did not contain a valid private RSAKey. JWT signing disabled.");
		} catch (Exception e) {
			LOG.error("Failed to extract private key from JWKSource. JWT signing disabled.", e);
		}
	}

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
