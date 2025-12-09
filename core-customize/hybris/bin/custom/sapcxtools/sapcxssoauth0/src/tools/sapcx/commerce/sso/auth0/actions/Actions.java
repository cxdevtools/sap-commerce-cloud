package tools.sapcx.commerce.sso.auth0.actions;

import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;

import de.hybris.platform.core.model.user.CustomerModel;

public interface Actions {
	static Role getRole(String roleName) throws Auth0Exception {
		return FetchRoleAction.getRole(roleName);
	}

	static void assignRole(Role role, User user) throws Auth0Exception {
		AssignRoleAction.assignRole(role, user);
	}

	static void removeRole(Role role, User user) throws Auth0Exception {
		RemoveRoleAction.removeRole(role, user);
	}

	static User getUser(String email) throws Auth0Exception {
		return FetchUserAction.getUser(email);
	}

	static User createUser(CustomerModel customer) throws Auth0Exception {
		return CreateUserAction.createUser(customer);
	}

	static User updateUser(User user, CustomerModel customer) throws Auth0Exception {
		return UpdateUserAction.updateUser(user, customer);
	}

	static void removeUser(User user, String customerId) throws Auth0Exception {
		RemoveUserAction.removeUser(user, customerId);
	}

	static String getPasswordResetUrl(User user) throws Auth0Exception {
		return getPasswordResetUrl(user, false);
	}

	static String getPasswordResetUrl(User user, boolean markEmailAsVerified) throws Auth0Exception {
		return PasswordResetUrlAction.getPasswordResetUrl(user, markEmailAsVerified);
	}
}
