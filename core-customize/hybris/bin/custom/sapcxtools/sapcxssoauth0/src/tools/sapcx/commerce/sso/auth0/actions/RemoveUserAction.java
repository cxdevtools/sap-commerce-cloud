package tools.sapcx.commerce.sso.auth0.actions;

import java.util.Map;

import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RemoveUserAction implements SdkAction<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(RemoveUserAction.class);

	static void removeUser(User user, String customerId) throws Auth0Exception {
		new RemoveUserAction().execute(Map.of("user", user, "customerId", customerId));
	}

	private RemoveUserAction() {
		// Avoid instantiation
	}

	@Override
	public Void execute(Map<String, Object> parameter) throws Auth0Exception {
		User user = getWithType(parameter, "user", User.class);
		String customerId = getWithType(parameter, "customerId", String.class);
		try {
			fetch(managementAPI().users().delete(user.getId()));
			LOG.debug("Delete user with ID '{}' was successful.", customerId);
			return null;
		} catch (Auth0Exception exception) {
			LOG.debug(String.format("Delete user with ID '%s' failed!", customerId), exception);
			throw exception;
		}
	}
}
