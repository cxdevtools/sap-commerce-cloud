package tools.sapcx.commerce.sso.replication;

import java.util.function.Predicate;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerReplicationInterceptor implements ValidateInterceptor<CustomerModel>, RemoveInterceptor<CustomerModel> {
	private static final Logger LOG = LoggerFactory.getLogger(CustomerReplicationInterceptor.class);

	private CustomerReplicationStrategy customerReplicationStrategy;
	private Predicate<CustomerModel> customerReplicationFilter;
	private boolean enabled;

	public CustomerReplicationInterceptor(
			CustomerReplicationStrategy customerReplicationStrategy,
			Predicate<CustomerModel> customerReplicationFilter,
			boolean enabled) {
		this.customerReplicationStrategy = customerReplicationStrategy;
		this.customerReplicationFilter = customerReplicationFilter;
		this.enabled = enabled;
	}

	@Override
	public void onValidate(CustomerModel customer, InterceptorContext interceptorContext) {
		if (enabled && customer != null) {
			try {
				if (customerReplicationFilter.test(customer)) {
					customerReplicationStrategy.replicate(customer);
				}
			} catch (RuntimeException e) {
				LOG.warn(String.format("Could not replicate customer with ID '%s'. Data may no be in sync and needs to be corrected manually!", customer.getUid()), e);
			}
		}
	}

	@Override
	public void onRemove(CustomerModel customer, InterceptorContext interceptorContext) throws InterceptorException {
		if (enabled && customer != null) {
			try {
				if (customerReplicationFilter.test(customer)) {
					customerReplicationStrategy.remove(customer.getUid());
				}
			} catch (RuntimeException e) {
				LOG.warn(String.format("Could not remove customer with ID '%s'! Account needs to be removed manually!", customer.getUid()), e);
			}
		}
	}
}
