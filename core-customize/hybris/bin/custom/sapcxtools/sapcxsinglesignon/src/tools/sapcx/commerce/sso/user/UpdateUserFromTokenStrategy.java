package tools.sapcx.commerce.sso.user;

import org.springframework.security.oauth2.jwt.Jwt;

public interface UpdateUserFromTokenStrategy {
	void updateUserFromToken(String userId, Jwt token);
}
