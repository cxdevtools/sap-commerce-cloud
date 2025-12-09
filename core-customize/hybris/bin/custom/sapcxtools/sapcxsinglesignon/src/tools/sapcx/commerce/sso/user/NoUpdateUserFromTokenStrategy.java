package tools.sapcx.commerce.sso.user;

import org.springframework.security.oauth2.jwt.Jwt;

public class NoUpdateUserFromTokenStrategy implements UpdateUserFromTokenStrategy {
	@Override
	public void updateUserFromToken(String userId, Jwt token) {
	}
}
