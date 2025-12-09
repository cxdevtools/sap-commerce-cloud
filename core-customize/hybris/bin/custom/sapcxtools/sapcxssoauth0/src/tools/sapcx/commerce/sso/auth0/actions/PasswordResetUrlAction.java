package tools.sapcx.commerce.sso.auth0.actions;

import java.util.Map;

import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.tickets.PasswordChangeTicket;
import com.auth0.json.mgmt.users.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PasswordResetUrlAction implements SdkAction<String> {
	private static final Logger LOG = LoggerFactory.getLogger(PasswordResetUrlAction.class);

	static String getPasswordResetUrl(User user, boolean markEmailAsVerified) throws Auth0Exception {
		return new PasswordResetUrlAction().execute(Map.of("user", user, "markEmailAsVerified", markEmailAsVerified));
	}

	private PasswordResetUrlAction() {
		// Avoid instantiation
	}

	@Override
	public String execute(Map<String, Object> parameter) throws Auth0Exception {
		User user = getWithType(parameter, "user", User.class);
		Boolean markEmailAsVerified = getWithType(parameter, "markEmailAsVerified", Boolean.class);
		try {
			PasswordChangeTicket ticket = new PasswordChangeTicket(user.getId());
			ticket.setMarkEmailAsVerified(markEmailAsVerified);

			PasswordChangeTicket passwordChangeTicket = fetch(managementAPI().tickets().requestPasswordChange(ticket));
			return passwordChangeTicket.getTicket();
		} catch (Auth0Exception exception) {
			LOG.debug(String.format("Get password reset token for existing user with ID '{}' failed!", user.getEmail()), exception);
			throw exception;
		}
	}
}
