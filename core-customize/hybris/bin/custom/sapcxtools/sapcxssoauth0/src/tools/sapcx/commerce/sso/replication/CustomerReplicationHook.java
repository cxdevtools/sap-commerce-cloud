package tools.sapcx.commerce.sso.replication;

import com.auth0.json.mgmt.users.User;

import de.hybris.platform.core.model.user.CustomerModel;

public interface CustomerReplicationHook {
	default void customerSuccessfullyCreated(CustomerModel customer, User createdUser) {
	}

	default void customerSuccessfullyUpdated(CustomerModel customer, User updatedUser) {
	}

	default void customerSuccessfullyRemoved(String customer) {
	}
}
