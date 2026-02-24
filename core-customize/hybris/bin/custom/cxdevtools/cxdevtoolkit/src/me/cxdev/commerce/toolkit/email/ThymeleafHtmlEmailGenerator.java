package me.cxdev.commerce.toolkit.email;

import java.util.Locale;
import java.util.Map;

import de.hybris.platform.util.mail.MailUtils;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.assertj.core.util.VisibleForTesting;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateEngineException;

/**
 * This {@link HtmlEmailGenerator} uses Thymeleaf as template engine and supports multiple template formats:
 *
 * <ul>
 *     <li>HTML: provided as html file in classpath:/email-templates/html/[name].html</li>
 *     <li>TEXT: provided as text file in classpath:/email-templates/text/[name].txt</li>
 *     <li>String: provided as plain {@link String} to the template engine itself</li>
 * </ul>
 * <p>
 * The templates can be placed in any extension, they just have to follow the naming conventions.
 */
public class ThymeleafHtmlEmailGenerator implements HtmlEmailGenerator {
	private ITemplateEngine templateEngine;

	public ThymeleafHtmlEmailGenerator(ITemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	@Override
	public HtmlEmail createHtmlEmail() throws EmailException {
		// MailUtils always returns an HtmlEmail
		HtmlEmail email = getPreConfiguredEmail();

		// Use UTF-8 as default charset
		email.setCharset("UTF-8");

		return email;
	}

	@VisibleForTesting
	protected HtmlEmail getPreConfiguredEmail() throws EmailException {
		return (HtmlEmail) MailUtils.getPreConfiguredEmail();
	}

	@Override
	public String processTemplate(String template, Map<String, Object> contextParameters, Locale locale) throws EmailException {
		try {
			return templateEngine.process(template, new Context(locale, contextParameters));
		} catch (TemplateEngineException e) {
			throw new EmailException(String.format("Email cannot be created. Could not process template %s", template), e);
		}
	}
}
