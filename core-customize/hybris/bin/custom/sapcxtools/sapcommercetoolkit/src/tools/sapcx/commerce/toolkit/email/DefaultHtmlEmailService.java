package tools.sapcx.commerce.toolkit.email;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;

public class DefaultHtmlEmailService implements HtmlEmailService {
	/**
	 * Sends the email by calling the underlying email objects native code.
	 *
	 * @param email the email to be sent
	 * @return the message id of the underlying MimeMessage
	 * @throws EmailException the sending failed
	 */
	@Override
	public String sendEmailInternal(HtmlEmail email) throws EmailException {
		return email.send();
	}
}
