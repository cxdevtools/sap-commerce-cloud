package me.cxdev.commerce.toolkit.email;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;

import jakarta.mail.internet.InternetAddress;

/**
 * The {@link HtmlEmailGenerator} interface introduces a simplified way of generating HTML emails from processes and
 * workflows, without the need of having a CMS template and page defined for the email to be sent, as required with
 * the SAP standard.
 * <p>
 * This process shall not replace the way emails are generated within the system if the emails contain editorial
 * content, but rather for emails, that do not require editorial content and make it easier to generate them.
 */
@FunctionalInterface
public interface HtmlEmailGenerator {
	HtmlEmail createHtmlEmail() throws EmailException;

	default String processTemplate(String template, Map<String, Object> contextParameters, Locale locale) throws EmailException {
		return template;
	}

	default HtmlEmailBuilder newHtmlEmail() {
		return HtmlEmailBuilder.withHtmlEmailGenerator(this);
	}

	/**
	 * Deprecated: please use {@link #newHtmlEmail()} with builder pattern instead!
	 */
	@Deprecated(since = "2.4.3", forRemoval = true)
	default HtmlEmail createHtmlEmail(String subject, String body, Collection<InternetAddress> to) throws EmailException {
		return createHtmlEmail(subject, body, to, null);
	}

	/**
	 * Deprecated: please use {@link #newHtmlEmail()} with builder pattern instead!
	 */
	@Deprecated(since = "2.4.3", forRemoval = true)
	default HtmlEmail createHtmlEmail(String subject, String body, Collection<InternetAddress> to, Collection<InternetAddress> cc) throws EmailException {
		return createHtmlEmail(subject, body, to, cc, null);
	}

	/**
	 * Deprecated: please use {@link #newHtmlEmail()} with builder pattern instead!
	 */
	@Deprecated(since = "2.4.3", forRemoval = true)
	default HtmlEmail createHtmlEmail(String subject, String body, Collection<InternetAddress> to, Collection<InternetAddress> cc, Collection<InternetAddress> bcc)
			throws EmailException {
		return newHtmlEmail()
				.subject(subject)
				.body(body)
				.custom(builder -> emptyIfNull(to).forEach(builder::to))
				.custom(builder -> emptyIfNull(cc).forEach(builder::cc))
				.custom(builder -> emptyIfNull(bcc).forEach(builder::bcc))
				.build();
	}

	/**
	 * Deprecated: please use {@link #newHtmlEmail()} with builder pattern instead!
	 */
	@Deprecated(since = "2.4.3", forRemoval = true)
	default HtmlEmail createHtmlEmailFromTemplate(String subject, Collection<InternetAddress> to, String template,
			Map<String, Object> contextParameters, Locale locale) throws EmailException {
		return createHtmlEmailFromTemplate(subject, to, null, template, contextParameters, locale);
	}

	/**
	 * Deprecated: please use {@link #newHtmlEmail()} with builder pattern instead!
	 */
	@Deprecated(since = "2.4.3", forRemoval = true)
	default HtmlEmail createHtmlEmailFromTemplate(String subject, Collection<InternetAddress> to, Collection<InternetAddress> cc, String template,
			Map<String, Object> contextParameters, Locale locale) throws EmailException {
		return createHtmlEmailFromTemplate(subject, to, cc, null, template, contextParameters, locale);
	}

	/**
	 * Deprecated: please use {@link #newHtmlEmail()} with builder pattern instead!
	 */
	@Deprecated(since = "2.4.3", forRemoval = true)
	default HtmlEmail createHtmlEmailFromTemplate(String subject, Collection<InternetAddress> to, Collection<InternetAddress> cc, Collection<InternetAddress> bcc, String template,
			Map<String, Object> templateParameters, Locale locale) throws EmailException {
		return newHtmlEmail()
				.subject(subject)
				.template(template, locale)
				.templateParameter(templateParameters)
				.custom(builder -> emptyIfNull(to).forEach(builder::to))
				.custom(builder -> emptyIfNull(cc).forEach(builder::cc))
				.custom(builder -> emptyIfNull(bcc).forEach(builder::bcc))
				.build();
	}
}
