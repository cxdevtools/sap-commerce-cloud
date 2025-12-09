package tools.sapcx.commerce.sso.auth0.replication;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.auth0.json.mgmt.users.User;

import de.hybris.platform.commerceservices.strategies.CustomerNameStrategy;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class Auth0CustomerPopulator implements Populator<CustomerModel, User> {
	private CustomerNameStrategy customerNameStrategy;

	public Auth0CustomerPopulator(CustomerNameStrategy customerNameStrategy) {
		this.customerNameStrategy = customerNameStrategy;
	}

	@Override
	public void populate(CustomerModel source, User target) throws ConversionException {
		target.setEmail(source.getContactEmail());
		target.setNickname(source.getCustomerID());
		target.setName(source.getName());

		String[] nameParts = customerNameStrategy.splitName(source.getName());
		if (nameParts.length == 2) {
			if (isNotBlank(nameParts[0])) {
				target.setGivenName(nameParts[0]);
			}
			if (isNotBlank(nameParts[1])) {
				target.setFamilyName(nameParts[1]);
			}
		}
	}
}
