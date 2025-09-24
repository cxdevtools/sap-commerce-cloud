package tools.sapcx.commerce.sso.filter;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.filter.OncePerRequestFilter;

import tools.sapcx.commerce.sso.replication.CustomerReplicationStrategy;
import tools.sapcx.commerce.sso.user.UpdateUserFromTokenStrategy;

/**
 * This filter performs verification of an external access token.
 *
 * If enabled, it will extract the access token from the request, and asks a
 * concrete implementation to fetch the user details.
 *
 * If valid, the user details will be used to create a local OAuth2
 * Authentication for the user and stored in the token store.
 *
 * Later in the filter chain, the Spring security chain will verify the access
 * token against the token store and use this token to authenticate the user.
 */
public class JwtAccessTokenVerificationFilter extends OncePerRequestFilter {
	private static final Logger LOG = LoggerFactory.getLogger(JwtAccessTokenVerificationFilter.class);
	public static final String AUTHORIZED_PARTY_CLAIM = "azp";
	public static final String SCOPE_CLAIM = "scope";

	private OAuth2RequestFactory oAuth2RequestFactory;
	private ClientDetailsService clientDetailsService;
	private UserDetailsService userDetailsService;
	private UpdateUserFromTokenStrategy updateUserFromTokenStrategy;
	private CustomerReplicationStrategy customerReplicationStrategy;
	private TokenStore tokenStore;
	private String occClientId;
	private boolean enabled;
	private String jwksUrl;
	private String issuer;
	private String audience;
	private String scope;
	private List<String> requiredClaims;
	private String clientId;
	private String customerIdField;
	private TokenExtractor tokenExtractor;
	private JwtDecoder jwtDecoder = null;

	public JwtAccessTokenVerificationFilter(
			OAuth2RequestFactory oAuth2RequestFactory,
			ClientDetailsService clientDetailsService,
			UserDetailsService userDetailsService,
			UpdateUserFromTokenStrategy updateUserFromTokenStrategy,
			CustomerReplicationStrategy customerReplicationStrategy,
			TokenStore tokenStore,
			String occClientId,
			boolean enabled,
			String jwksUrl,
			String issuer,
			String audience,
			String scope,
			String requiredClaims,
			String clientId,
			String customerIdField) {
		this.oAuth2RequestFactory = oAuth2RequestFactory;
		this.clientDetailsService = clientDetailsService;
		this.userDetailsService = userDetailsService;
		this.updateUserFromTokenStrategy = updateUserFromTokenStrategy;
		this.customerReplicationStrategy = customerReplicationStrategy;
		this.tokenStore = tokenStore;
		this.occClientId = occClientId;
		this.enabled = enabled;
		this.jwksUrl = jwksUrl;
		this.issuer = issuer;
		this.audience = audience;
		this.scope = scope;
		this.requiredClaims = new ArrayList<>();
		if (isNotBlank(requiredClaims)) {
			Stream.of(StringUtils.split(requiredClaims, ','))
					.map(StringUtils::trimToEmpty)
					.filter(StringUtils::isNotBlank)
					.forEach(this.requiredClaims::add);
		}
		this.clientId = clientId;
		this.customerIdField = customerIdField;
	}

	@Override
	public void afterPropertiesSet() throws ServletException {
		super.afterPropertiesSet();
		this.tokenExtractor = new BearerTokenExtractor();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		Authentication accessToken = tokenExtractor.extract(request);
		if (enabled && accessToken != null) {
			String accessTokenValue = accessToken.getPrincipal().toString().intern();
			LOG.debug("Access token extracted from request: {}", accessTokenValue);

			OAuth2AccessToken oAuth2AccessToken = fetchFromTokenStore(accessTokenValue, false);
			LOG.debug("OAuth2 AccessToken from token store (1st attempt, without lock): {} (expired? => {})", oAuth2AccessToken,
					oAuth2AccessToken != null ? oAuth2AccessToken.isExpired() : false);

			if (oAuth2AccessToken == null || oAuth2AccessToken.isExpired()) {
				synchronized (accessTokenValue) {
					oAuth2AccessToken = fetchFromTokenStore(accessTokenValue, true);
					LOG.debug("OAuth2 AccessToken from token store (2nd attempt, with lock): {} (expired? => {})", oAuth2AccessToken,
							oAuth2AccessToken != null ? oAuth2AccessToken.isExpired() : false);
				}
			}
		}

		filterChain.doFilter(request, response);
	}

	private OAuth2AccessToken fetchFromTokenStore(String accessTokenValue, boolean createIfMissing) {
		OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(accessTokenValue);
		if (oAuth2AccessToken != null && !oAuth2AccessToken.isExpired()) {
			return oAuth2AccessToken;
		} else if (oAuth2AccessToken != null && oAuth2AccessToken.isExpired()) {
			tokenStore.removeAccessToken(oAuth2AccessToken);
			oAuth2AccessToken = null;
		}

		if (createIfMissing) {
			try {
				Jwt decodedToken = decodeAccessToken(accessTokenValue);
				String userId = decodedToken.getClaimAsString(customerIdField);
				if (userId != null) {
					LOG.debug("Mapped user ID using field '{}': '{}'", customerIdField, userId);
					updateUserFromTokenStrategy.updateUserFromToken(userId, decodedToken);
					oAuth2AccessToken = storeAuthenticationForUser(userId, occClientId, decodedToken);
				} else {
					LOG.warn("No user ID found in access token for field: '{}' and the configured authorized party. Make sure your IDP configuration is correct!", customerIdField);
				}
			} catch (Exception e) {
				LOG.debug(String.format("Invalid access token '%s'!", accessTokenValue), e);
			}
		}

		return oAuth2AccessToken;
	}

	protected Jwt decodeAccessToken(String accessTokenValue) throws JwtException {
		if (jwtDecoder == null) {
			initJwtDecoder();
		}

		try {
			return jwtDecoder.decode(accessTokenValue);
		} catch (JwtException e) {
			LOG.debug("Retry with reinitialized decoder");
			initJwtDecoder();
			return jwtDecoder.decode(accessTokenValue);
		}
	}

	private OAuth2AccessToken storeAuthenticationForUser(String userId, String oAuth2ClientId, Jwt decodedToken) {
		assert isNotBlank(oAuth2ClientId);
		assert isNotBlank(userId);
		assert Objects.nonNull(decodedToken);

		// OAuth2 Request
		ClientDetails clientDetails = clientDetailsService.loadClientByClientId(oAuth2ClientId);
		TokenRequest tokenRequest = oAuth2RequestFactory.createTokenRequest(Collections.emptyMap(), clientDetails);
		OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);

		try {
			// Username Password Auth Token
			UsernamePasswordAuthenticationToken userToken = createUsernamePasswordAuthenticationToken(userId);

			// Create access token and authentication for user
			DefaultOAuth2AccessToken oAuth2AccessToken = new DefaultOAuth2AccessToken(decodedToken.getTokenValue());
			oAuth2AccessToken.setExpiration(Date.from(decodedToken.getExpiresAt()));
			OAuth2Authentication authentication = new OAuth2Authentication(oAuth2Request, userToken);

			// Remove existing access token for same authentication
			OAuth2AccessToken existingToken = tokenStore.getAccessToken(authentication);
			if (existingToken != null) {
				LOG.debug("Found another access token '{}' for the authentication, removing it from the token store.", existingToken.getValue());
				tokenStore.removeAccessToken(existingToken);
			}

			// Create the new access token for the user
			tokenStore.storeAccessToken(oAuth2AccessToken, authentication);
			LOG.debug("New access token has been created successfully!");

			return oAuth2AccessToken;
		} catch (BadCredentialsException e) {
			customerReplicationStrategy.remove(userId);
			throw e;
		}
	}

	private UsernamePasswordAuthenticationToken createUsernamePasswordAuthenticationToken(String userId) {
		try {
			UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
			Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
			return new UsernamePasswordAuthenticationToken(userId, null, authorities);
		} catch (UsernameNotFoundException e) {
			LOG.warn("Login attempt for unknown user '{}'!", userId);
			throw new BadCredentialsException("Invalid credentials!");
		}
	}

	private synchronized void initJwtDecoder() {
		if (isNotBlank(jwksUrl)) {
			LOG.info("Initializing JWT decoder with JwkSetUri '{}'", jwksUrl);
			this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
		} else {
			LOG.info("Initializing JWT decoder with OIDC issuer location '{}'", issuer);
			this.jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer);
		}

		if (this.jwtDecoder instanceof NimbusJwtDecoder) {
			configureValidatorsForJwtDecoder((NimbusJwtDecoder) this.jwtDecoder);
		}
	}

	private void configureValidatorsForJwtDecoder(NimbusJwtDecoder decoder) {
		List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();

		validators.add(new JwtTimestampValidator());

		if (isNotBlank(this.issuer)) {
			validators.add(new JwtIssuerValidator(this.issuer));
		}

		if (isNotBlank(this.audience)) {
			validators.add(new AudienceValidator(this.audience));
		}

		if (isNotBlank(this.clientId)) {
			validators.add(new JwtClaimValidator<>(AUTHORIZED_PARTY_CLAIM, this.clientId::equalsIgnoreCase));
		}

		if (isNotBlank(this.scope)) {
			validators.add(new JwtClaimValidator<>(SCOPE_CLAIM, this.scope::equalsIgnoreCase));
		}

		for (String requiredClaim : this.requiredClaims) {
			validators.add(new JwtClaimValidator<>(requiredClaim, Objects::nonNull));
		}

		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
	}
}
