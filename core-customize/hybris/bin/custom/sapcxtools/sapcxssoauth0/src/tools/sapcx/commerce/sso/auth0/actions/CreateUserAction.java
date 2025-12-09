package tools.sapcx.commerce.sso.auth0.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CreateUserAction implements SdkAction<User> {
	private static final Logger LOG = LoggerFactory.getLogger(CreateUserAction.class);

	static User createUser(CustomerModel customer) throws Auth0Exception {
		return new CreateUserAction().execute(Map.of("customer", customer));
	}

	private CreateUserAction() {
		// Avoid instantiation
	}

	@Override
	public User execute(Map<String, Object> parameter) throws Auth0Exception {
		CustomerModel customer = getWithType(parameter, "customer", CustomerModel.class);
		String customerId = customer.getUid();

		User user = null;
		try {
			Converter<CustomerModel, User> customerConverter = getCustomerConverter();
			User userInfo = customerConverter.convert(customer);

			if (requireEmailVerification()) {
				userInfo.setEmailVerified(false);
				userInfo.setVerifyEmail(true);
			}

			if (useBlockedStatusForDisabledCustomers()) {
				userInfo.setBlocked(BooleanUtils.isNotFalse(customer.isLoginDisabled()));
			}

			// Add one time information for creation process
			userInfo.setConnection(getCustomerConnection());
			userInfo.setPassword(getRandomPassword());
			if (requirePasswordVerification()) {
				userInfo.setVerifyPassword(true);
			}

			user = fetch(managementAPI().users().create(userInfo));
			return user;
		} catch (Auth0Exception exception) {
			LOG.debug(String.format("Create user with ID '%s' failed!", customerId), exception);
			throw exception;
		} finally {
			LOG.debug("Create user with ID '{}' resulted in: '{}'.", customerId, user != null ? user.getId() : "-error-");
		}
	}

	private char[] getRandomPassword() {
		List<String> passwordCharacters = new ArrayList<>(32);

		String alphaNumericChars = RandomStringUtils.randomAlphanumeric(26);
		for (int i = 0; i < alphaNumericChars.length(); i++) {
			passwordCharacters.add(String.valueOf(alphaNumericChars.charAt(i)));
		}

		String specialChars = RandomStringUtils.random(6, '!', '@', '#', '$', '%', '^', '&', '*');
		for (int i = 0; i < specialChars.length(); i++) {
			passwordCharacters.add(String.valueOf(specialChars.charAt(i)));
		}

		Collections.shuffle(passwordCharacters);
		return String.join("", passwordCharacters).toCharArray();
	}
}
