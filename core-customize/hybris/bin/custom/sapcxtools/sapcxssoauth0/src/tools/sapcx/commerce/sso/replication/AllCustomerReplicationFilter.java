package tools.sapcx.commerce.sso.replication;

import java.util.function.Predicate;

import de.hybris.platform.core.model.user.CustomerModel;

public class AllCustomerReplicationFilter implements Predicate<CustomerModel> {
	@Override
	public boolean test(CustomerModel customerModel) {
		return true;
	}
}
