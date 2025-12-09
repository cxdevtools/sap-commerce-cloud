package tools.sapcx.commerce.sso.auth0.actions;

import java.util.Map;

import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FetchUserAction implements SdkAction<User> {
	private static final Logger LOG = LoggerFactory.getLogger(FetchUserAction.class);

	static User getUser(String id) throws Auth0Exception {
		return new FetchUserAction().execute(Map.of("id", id));
	}

	private FetchUserAction() {
		// Avoid instantiation
	}

	@Override
	public User execute(Map<String, Object> parameter) throws Auth0Exception {
		String userId = getWithType(parameter, "id", String.class);
		User user = null;
		try {
			UserFilter userById = new UserFilter().withQuery(userId);
			user = fetchFirst(managementAPI().users().list(userById));
			return user;
		} catch (Auth0Exception exception) {
			LOG.debug(String.format("Search for user with ID '%s' could not be executed!", userId), exception);
			throw exception;
		} finally {
			LOG.debug("Lookup for existing user with ID '{}' resulted in: '{}'", userId, user != null ? user.getId() : "-not found-");
		}
	}
}
