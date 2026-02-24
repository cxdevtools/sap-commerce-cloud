package me.cxdev.commerce.toolkit.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.junit.Before;
import org.junit.Test;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;
import me.cxdev.commerce.toolkit.testing.testdoubles.email.HtmlEmailGeneratorFake;

public class HtmlEmailGeneratorDefaultMethodsTests {
	private HtmlEmailGenerator htmlEmailGenerator;

	@Before
	public void setUp() throws Exception {
		htmlEmailGenerator = new HtmlEmailGeneratorFake();
	}

	@Test
	public void processTemplateFallbackToTemplateName() throws Exception {
		String content = htmlEmailGenerator.processTemplate("templateName", Map.of(), Locale.GERMANY);
		assertThat(content).isEqualTo("templateName");
	}

	@Test
	public void defaultImplementationWithSubjectBodyAndToDelegatesToOverloadedMethod() throws Exception {
		HtmlEmail email = htmlEmailGenerator.createHtmlEmail("subject", "body", Arrays.asList(InternetAddress.parse("to@local-dev")));

		assertThat(email.getSubject()).isEqualTo("subject");
		assertThat(email.getToAddresses()).extracting("address").containsExactly("to@local-dev");
		assertThat(email.getCcAddresses()).isNullOrEmpty();
		assertThat(email.getBccAddresses()).isNullOrEmpty();
		assertBodyContent(email, "body");
	}

	@Test
	public void defaultImplementationWithSubjectBodyAndToAndCcDelegatesToOverloadedMethod() throws Exception {
		List<InternetAddress> to = Arrays.asList(InternetAddress.parse("to@local-dev"));
		List<InternetAddress> cc = Arrays.asList(InternetAddress.parse("cc@local-dev"));
		HtmlEmail email = htmlEmailGenerator.createHtmlEmail("subject", "body", to, cc);

		assertThat(email.getSubject()).isEqualTo("subject");
		assertThat(email.getToAddresses()).extracting("address").containsExactly("to@local-dev");
		assertThat(email.getCcAddresses()).extracting("address").containsExactly("cc@local-dev");
		assertThat(email.getBccAddresses()).isNullOrEmpty();
		assertBodyContent(email, "body");
	}

	@Test
	public void defaultImplementationWithSubjectTemplateAndToDelegatesToOverloadedMethod() throws Exception {
		List<InternetAddress> to = Arrays.asList(InternetAddress.parse("to@local-dev"));
		HtmlEmail email = htmlEmailGenerator.createHtmlEmailFromTemplate("subject", to, "template", Map.of("param", "value"), Locale.GERMANY);

		assertThat(email.getSubject()).isEqualTo("subject");
		assertThat(email.getToAddresses()).extracting("address").containsExactly("to@local-dev");
		assertThat(email.getCcAddresses()).isNullOrEmpty();
		assertThat(email.getBccAddresses()).isNullOrEmpty();
		assertTemplateBody(email, "template", Locale.GERMANY, Map.of("param", "value"));
	}

	@Test
	public void defaultImplementationWithSubjectTemplateAndToAndCcDelegatesToOverloadedMethod() throws Exception {
		List<InternetAddress> to = Arrays.asList(InternetAddress.parse("to@local-dev"));
		List<InternetAddress> cc = Arrays.asList(InternetAddress.parse("cc@local-dev"));
		HtmlEmail email = htmlEmailGenerator.createHtmlEmailFromTemplate("subject", to, cc, "template", Map.of("param", "value"), Locale.GERMANY);

		assertThat(email.getSubject()).isEqualTo("subject");
		assertThat(email.getToAddresses()).extracting("address").containsExactly("to@local-dev");
		assertThat(email.getCcAddresses()).extracting("address").containsExactly("cc@local-dev");
		assertThat(email.getBccAddresses()).isNullOrEmpty();
		assertTemplateBody(email, "template", Locale.GERMANY, Map.of("param", "value"));
	}

	private void assertBodyContent(HtmlEmail email, String body) throws EmailException, MessagingException, IOException {
		email.buildMimeMessage();
		MimeMultipart part = (MimeMultipart) email.getMimeMessage().getContent();
		assertThat(part.getBodyPart(0).getContent()).isEqualTo(body);
	}

	private void assertTemplateBody(HtmlEmail email, String template, Locale locale, Map<String, Object> templateParameters)
			throws EmailException, MessagingException, IOException {
		email.buildMimeMessage();
		MimeMultipart part = (MimeMultipart) email.getMimeMessage().getContent();

		StringBuilder builder = new StringBuilder();
		builder.append("Template: ").append(template).append("\n\n");
		builder.append("Locale: ").append(locale).append("\n\n");
		builder.append(templateParameters);

		assertThat(part.getBodyPart(0).getContent()).isEqualTo(builder.toString());
	}
}
