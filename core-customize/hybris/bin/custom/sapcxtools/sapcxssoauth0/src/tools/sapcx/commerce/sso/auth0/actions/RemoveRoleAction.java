package tools.sapcx.commerce.sso.auth0.actions;

import java.util.List;
import java.util.Map;

import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RemoveRoleAction implements SdkAction<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(RemoveRoleAction.class);

	static void removeRole(Role role, User user) throws Auth0Exception {
		new RemoveRoleAction().execute(Map.of("role", role, "user", user));
	}

	private RemoveRoleAction() {
		// Avoid instantiation
	}

	@Override
	public Void execute(Map<String, Object> parameter) throws Auth0Exception {
		Role role = getWithType(parameter, "role", Role.class);
		User user = getWithType(parameter, "user", User.class);

		try {
			submit(managementAPI().users().removeRoles(user.getId(), List.of(role.getId())));
			LOG.debug("Remove role with name '{}' from user with email '{}'.", role.getName(), user.getEmail());
			return null;
		} catch (Auth0Exception exception) {
			LOG.warn(String.format("Could not remove role '%s' from user with email '%s'. ", role.getName(), user.getEmail()), exception);
			throw exception;
		}
	}

}
