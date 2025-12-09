package tools.sapcx.commerce.sso.auth0.actions;

import java.util.Map;

import com.auth0.client.mgmt.filter.RolesFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.roles.Role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FetchRoleAction implements SdkAction<Role> {
	private static final Logger LOG = LoggerFactory.getLogger(FetchRoleAction.class);

	static Role getRole(String roleName) throws Auth0Exception {
		return new FetchRoleAction().execute(Map.of("role", roleName));
	}

	private FetchRoleAction() {
		// Avoid instantiation
	}

	@Override
	public Role execute(Map<String, Object> parameter) throws Auth0Exception {
		String roleName = getWithType(parameter, "role", String.class);
		Role role = null;
		try {
			RolesFilter roleByName = new RolesFilter().withName(roleName);
			role = fetchFirst(managementAPI().roles().list(roleByName));
			return role;
		} catch (Auth0Exception exception) {
			LOG.debug(String.format("Search for role with name '%s' could not be executed!", roleName), exception);
			throw exception;
		} finally {
			LOG.debug("Lookup for existing role with name '{}' resulted in: '{}'", roleName, role != null ? role.getId() : "-not found-");
		}
	}
}
