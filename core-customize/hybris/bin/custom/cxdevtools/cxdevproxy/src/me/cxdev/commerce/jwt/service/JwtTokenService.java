package me.cxdev.commerce.jwt.service;

/**
 * Core service responsible for managing and provisioning JSON Web Tokens (JWT)
 * for local development and proxy routing.
 * <p>
 * Implementations of this interface should handle the generation of signed tokens
 * (typically using the platform's native keys to ensure trust) based on predefined
 * user templates.
 * </p>
 */
public interface JwtTokenService {
	/**
	 * Retrieves a valid, signed JWT for the specified user.
	 * <p>
	 * This method should ideally utilize a caching mechanism to prevent unnecessary
	 * token regeneration. It returns a cached token if one exists and is still valid;
	 * otherwise, it triggers the generation of a new token.
	 * </p>
	 *
	 * @param userType The classification of the user (e.g., "customer", "employee", "admin").
	 * This is typically used to locate the correct token template.
	 * @param userId   The unique identifier of the user (e.g., "hans.meier@example.com").
	 * @return A Base64-encoded, signed JWT string, or {@code null} if the token
	 * could not be generated (e.g., due to missing keys or templates).
	 */
	String getOrGenerateToken(String userType, String userId);

	/**
	 * Forces the generation of a newly signed JWT for the specified user,
	 * bypassing any internal caches.
	 * <p>
	 * This method reads the associated template, computes dynamic claims
	 * (such as issue time and expiration), and signs the payload.
	 * </p>
	 *
	 * @param userType The classification of the user (e.g., "customer", "employee").
	 * @param userId   The unique identifier of the user.
	 * @return A newly generated, Base64-encoded, signed JWT string, or {@code null}
	 * if the generation fails.
	 */
	String generateSignedToken(String userType, String userId);
}
