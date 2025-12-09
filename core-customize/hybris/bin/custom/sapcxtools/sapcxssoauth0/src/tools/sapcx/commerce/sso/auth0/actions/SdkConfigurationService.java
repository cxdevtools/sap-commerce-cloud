package tools.sapcx.commerce.sso.auth0.actions;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdkConfigurationService {
	private static final Logger LOG = LoggerFactory.getLogger(SdkConfigurationService.class);
	private static final String AUTH0_MANAGEMENT_API_DOMAIN = "sapcxsinglesignon.auth0.management.api.domain";
	private static final String AUTH0_MANAGEMENT_API_AUDIENCE = "sapcxsinglesignon.auth0.management.api.audience";
	private static final String AUTH0_AUTH_API_CLIENTID = "sapcxsinglesignon.auth0.auth.api.clientid";
	private static final String AUTH0_MANAGEMENT_API_CLIENTID = "sapcxsinglesignon.auth0.management.api.clientid";
	private static final String AUTH0_MANAGEMENT_API_CLIENTSECRET = "sapcxsinglesignon.auth0.management.api.clientsecret";
	private static final String AUTH0_CUSTOMER_CONNECTION = "sapcxsinglesignon.auth0.customer.connection";
	private static final String AUTH0_CUSTOMER_ID_FIELD = "sapcxsinglesignon.auth0.customer.idfield";
	private static final String AUTH0_REQUIRE_EMAIL_VERIFICATION = "sapcxsinglesignon.auth0.customer.requireemailverification";
	private static final String AUTH0_REQUIRE_PASSWORD_VERIFICATION = "sapcxsinglesignon.auth0.customer.requirepasswordverification";
	private static final String AUTH0_USE_BLOCKEDSTATUS = "sapcxsinglesignon.auth0.customer.useblockedstatus";

	private ConfigurationService configurationService;
	private Converter<CustomerModel, User> customerConverter;

	public SdkConfigurationService(ConfigurationService configurationService, Converter<CustomerModel, User> customerConverter) {
		this.configurationService = configurationService;
		this.customerConverter = customerConverter;
	}

	public ManagementAPI getManagementAPI() throws Auth0Exception {
		LOG.debug("Create new Auth0 ManagementAPI.", getAudience());
		return ManagementAPI.newBuilder(getDomain(), getManagementAccessToken()).build();
	}

	private String getManagementAccessToken() throws Auth0Exception {
		LOG.debug("Fetch access token for management API for audience: {}", getAudience());
		AuthAPI authAPI = AuthAPI.newBuilder(getDomain(), getManagementClientId(), getManagementClientSecret()).build();
		return authAPI.requestToken(getAudience()).execute().getBody().getAccessToken();
	}

	private String getAudience() {
		return configurationService.getConfiguration().getString(AUTH0_MANAGEMENT_API_AUDIENCE);
	}

	private String getDomain() {
		return configurationService.getConfiguration().getString(AUTH0_MANAGEMENT_API_DOMAIN);
	}

	public String getManagementClientId() {
		return configurationService.getConfiguration().getString(AUTH0_MANAGEMENT_API_CLIENTID);
	}

	public String getManagementClientSecret() {
		return configurationService.getConfiguration().getString(AUTH0_MANAGEMENT_API_CLIENTSECRET);
	}

	public String getCustomerConnection() {
		return configurationService.getConfiguration().getString(AUTH0_CUSTOMER_CONNECTION);
	}

	public String getCustomerIdField() {
		return configurationService.getConfiguration().getString(AUTH0_CUSTOMER_ID_FIELD);
	}

	public Converter<CustomerModel, User> getCustomerConverter() {
		return customerConverter;
	}

	public boolean requireEmailVerification() {
		return configurationService.getConfiguration().getBoolean(AUTH0_REQUIRE_EMAIL_VERIFICATION, false);
	}

	public boolean requirePasswordVerification() {
		return configurationService.getConfiguration().getBoolean(AUTH0_REQUIRE_PASSWORD_VERIFICATION, false);
	}

	public boolean useBlockedStatusForDisabledCustomers() {
		return configurationService.getConfiguration().getBoolean(AUTH0_USE_BLOCKEDSTATUS, false);
	}

	public String getAuthClientId() {
		return configurationService.getConfiguration().getString(AUTH0_AUTH_API_CLIENTID, null);
	}
}
