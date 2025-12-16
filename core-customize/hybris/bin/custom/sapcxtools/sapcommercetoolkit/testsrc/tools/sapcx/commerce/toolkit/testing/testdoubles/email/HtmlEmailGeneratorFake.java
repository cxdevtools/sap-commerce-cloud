package tools.sapcx.commerce.toolkit.testing.testdoubles.email;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;

import tools.sapcx.commerce.toolkit.email.HtmlEmailGenerator;

import jakarta.mail.internet.InternetAddress;

public class HtmlEmailGeneratorFake implements HtmlEmailGenerator {
	@Override
	public HtmlEmail createHtmlEmail() throws EmailException {
		HtmlEmail htmlEmail = new HtmlEmail();
		htmlEmail.setHostName("localhost");
		htmlEmail.setCharset("UTF-8");
		htmlEmail.setFrom("from@local.dev");
		return htmlEmail;
	}

	@Override
	public HtmlEmail createHtmlEmailFromTemplate(String subject, Collection<InternetAddress> to, Collection<InternetAddress> cc, Collection<InternetAddress> bcc, String template,
			Map<String, Object> contextParameters, Locale locale) throws EmailException {
		StringBuilder builder = new StringBuilder();
		builder.append("Template: ").append(template).append("\n\n");
		builder.append("Locale: ").append(locale).append("\n\n");
		builder.append(contextParameters);
		return createHtmlEmail(subject, builder.toString(), to, cc, bcc);
	}
}
