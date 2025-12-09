package tools.sapcx.commerce.sso.replication;

import javax.annotation.Nonnull;

import de.hybris.platform.core.model.user.CustomerModel;

public interface CustomerReplicationStrategy {
	void replicate(@Nonnull CustomerModel customer);

	void remove(@Nonnull String customerId);
}
