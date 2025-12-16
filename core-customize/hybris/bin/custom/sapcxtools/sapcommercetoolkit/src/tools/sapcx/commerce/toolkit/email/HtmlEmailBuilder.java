package tools.sapcx.commerce.toolkit.email;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import de.hybris.platform.core.model.media.MediaModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;

import tools.sapcx.commerce.toolkit.email.attachments.EmailAfterBuildHook;
import tools.sapcx.commerce.toolkit.email.attachments.HtmlEmailAttachmentBuilder;
import tools.sapcx.commerce.toolkit.email.attachments.HtmlEmailAttachmentBuilders;

import jakarta.mail.internet.InternetAddress;

public class HtmlEmailBuilder {
	static HtmlEmailBuilder withHtmlEmailGenerator(HtmlEmailGenerator generator) {
		return new HtmlEmailBuilder(generator);
	}

	private List<HtmlEmailAddress> replyToAddresses = new ArrayList<>();
	private List<HtmlEmailAddress> toAddresses = new ArrayList<>();
	private List<HtmlEmailAddress> ccAddresses = new ArrayList<>();
	private List<HtmlEmailAddress> bccAddresses = new ArrayList<>();
	private List<EmailAfterBuildHook> afterBuildHooks = new ArrayList<>();

	private String subject;
	private String body;
	private String templateName;
	private Map<String, Object> contextParameters;
	private Locale templateLocale;

	private HtmlEmailGenerator htmlEmailGenerator;

	private HtmlEmailBuilder(HtmlEmailGenerator htmlEmailGenerator) {
		this.htmlEmailGenerator = htmlEmailGenerator;
	}

	public HtmlEmailBuilder replyTo(InternetAddress email) {
		replyToAddresses.add(HtmlEmailAddress.fromInternetAddress(email));
		return this;
	}

	public HtmlEmailBuilder replyTo(String email) {
		replyToAddresses.add(HtmlEmailAddress.address(email));
		return this;
	}

	public HtmlEmailBuilder replyTo(String name, String email) {
		replyToAddresses.add(HtmlEmailAddress.address(name, email));
		return this;
	}

	public HtmlEmailBuilder to(InternetAddress email) {
		toAddresses.add(HtmlEmailAddress.fromInternetAddress(email));
		return this;
	}

	public HtmlEmailBuilder to(String email) {
		toAddresses.add(HtmlEmailAddress.address(email));
		return this;
	}

	public HtmlEmailBuilder to(String name, String email) {
		toAddresses.add(HtmlEmailAddress.address(name, email));
		return this;
	}

	public HtmlEmailBuilder cc(InternetAddress email) {
		ccAddresses.add(HtmlEmailAddress.fromInternetAddress(email));
		return this;
	}

	public HtmlEmailBuilder cc(String email) {
		ccAddresses.add(HtmlEmailAddress.address(email));
		return this;
	}

	public HtmlEmailBuilder cc(String name, String email) {
		ccAddresses.add(HtmlEmailAddress.address(name, email));
		return this;
	}

	public HtmlEmailBuilder bcc(InternetAddress email) {
		bccAddresses.add(HtmlEmailAddress.fromInternetAddress(email));
		return this;
	}

	public HtmlEmailBuilder bcc(String email) {
		bccAddresses.add(HtmlEmailAddress.address(email));
		return this;
	}

	public HtmlEmailBuilder bcc(String name, String email) {
		bccAddresses.add(HtmlEmailAddress.address(name, email));
		return this;
	}

	public HtmlEmailBuilder subject(String subject) {
		this.subject = subject;
		return this;
	}

	public HtmlEmailBuilder body(String body) {
		this.body = body;
		return this;
	}

	public HtmlEmailBuilder template(String template, Locale locale) {
		this.templateName = template;
		this.templateLocale = locale;
		return templateParameter(Map.of());
	}

	public HtmlEmailBuilder templateParameter(String key, Object value) {
		templateParameter(Map.of(key, value));
		return this;
	}

	public HtmlEmailBuilder templateParameter(Map<String, Object> parameters) {
		if (this.contextParameters == null) {
			this.contextParameters = new HashMap<>();
		}
		this.contextParameters.putAll(parameters);
		return this;
	}

	public HtmlEmailBuilder attach(File file) {
		attach(HtmlEmailAttachmentBuilders.forFile(file));
		return this;
	}

	public HtmlEmailBuilder attach(MediaModel mediaModel) {
		attach(HtmlEmailAttachmentBuilders.forMedia(mediaModel));
		return this;
	}

	public HtmlEmailBuilder attach(HtmlEmailAttachmentBuilder builder) {
		return afterBuildHook(builder::attach);
	}

	public HtmlEmailBuilder custom(Consumer<HtmlEmailBuilder> consumer) {
		consumer.accept(this);
		return this;
	}

	public HtmlEmailBuilder afterBuildHook(EmailAfterBuildHook afterBuildHook) {
		this.afterBuildHooks.add(afterBuildHook);
		return this;
	}

	public HtmlEmail build() throws EmailException {
		validateConfiguration();

		HtmlEmail htmlEmail = htmlEmailGenerator.createHtmlEmail();
		configureAddresses(htmlEmail);
		processMessageContent(htmlEmail, htmlEmailGenerator);

		for (EmailAfterBuildHook hook : afterBuildHooks) {
			hook.afterBuild(htmlEmail);
		}

		return htmlEmail;
	}

	private void validateConfiguration() throws EmailException {
		if (toAddresses.isEmpty()) {
			throw new EmailException("Cannot create email without recipients. Please provide at least one valid email address!");
		}

		if (StringUtils.isBlank(body) && StringUtils.isBlank(templateName)) {
			throw new EmailException("Cannot create email without content. There must be a configuration for either body or template!");
		}

		if (StringUtils.isNotBlank(body) && StringUtils.isNotBlank(templateName)) {
			throw new EmailException("Cannot create email without ambiguous content. There must be a configuration for either body or template, not both!");
		}
	}

	private void configureAddresses(HtmlEmail htmlEmail) throws EmailException {
		for (HtmlEmailAddress address : replyToAddresses) {
			htmlEmail.addReplyTo(address.getEmail(), address.getName());
		}
		for (HtmlEmailAddress address : toAddresses) {
			htmlEmail.addTo(address.getEmail(), address.getName());
		}
		for (HtmlEmailAddress address : ccAddresses) {
			htmlEmail.addCc(address.getEmail(), address.getName());
		}
		for (HtmlEmailAddress address : bccAddresses) {
			htmlEmail.addBcc(address.getEmail(), address.getName());
		}
	}

	private void processMessageContent(HtmlEmail htmlEmail, HtmlEmailGenerator htmlEmailGenerator) throws EmailException {
		if (templateName != null && templateLocale != null) {
			templateParameter("subject", subject);
			templateParameter("recipients", emptyIfNull(toAddresses));
			templateParameter("ccRecipients", emptyIfNull(ccAddresses));
			body = htmlEmailGenerator.processTemplate(templateName, contextParameters, templateLocale);
		}

		htmlEmail.setSubject(subject);
		htmlEmail.setHtmlMsg(body);
	}
}
