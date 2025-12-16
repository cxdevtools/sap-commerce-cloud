package tools.sapcx.commerce.toolkit.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hybris.bootstrap.annotations.UnitTest;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IContext;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;

@UnitTest
public class ThymeleafHtmlEmailGeneratorTests {
	private ITemplateEngine templateEngine;
	private ThymeleafHtmlEmailGeneratorWithPreconfiguredEmail generator;

	@Before
	public void setUp() throws Exception {
		templateEngine = mock(ITemplateEngine.class);
		generator = new ThymeleafHtmlEmailGeneratorWithPreconfiguredEmail(templateEngine);
	}

	@Test
	public void verifyCharsetIsSetToUTF8() throws EmailException, MessagingException {
		HtmlEmail email = generator.newHtmlEmail()
				.to("to@local.dev")
				.subject("€")
				.body("body")
				.build();

		email.buildMimeMessage();
		assertThat(email.getMimeMessage().getSubject()).isEqualTo("€");
	}

	@Test(expected = EmailException.class)
	public void throwsExceptionIfToAddressIsEmpty() throws EmailException {
		generator.createHtmlEmail("subject", "body", List.of(), null, null);
	}

	@Test
	public void verifySetOfPropertiesWithoutCCandBCC() throws EmailException, MessagingException, IOException {
		Collection<InternetAddress> to = Arrays.asList(InternetAddress.parse("to@local.dev"));
		HtmlEmail email = generator.createHtmlEmail("subject", "body", to, null, null);

		assertThat(email.getSubject()).isEqualTo("subject");
		assertThat(email.getToAddresses()).containsOnlyElementsOf(to);
		assertBodyContent(email, "body");
	}

	@Test
	public void verifySetOfPropertiesWithCCandBCC() throws EmailException, MessagingException, IOException {
		Collection<InternetAddress> to = Arrays.asList(InternetAddress.parse("to-1@local.dev, to-2@local.dev"));
		Collection<InternetAddress> cc = Arrays.asList(InternetAddress.parse("cc-1@local.dev, cc-2@local.dev, cc-3@local.dev"));
		Collection<InternetAddress> bcc = Arrays.asList(InternetAddress.parse("bcc@local.dev"));
		HtmlEmail email = generator.createHtmlEmail("subject", "body", to, cc, bcc);

		assertThat(email.getSubject()).isEqualTo("subject");
		assertThat(email.getToAddresses()).containsOnlyElementsOf(to);
		assertThat(email.getCcAddresses()).containsOnlyElementsOf(cc);
		assertThat(email.getBccAddresses()).containsOnlyElementsOf(bcc);
		assertBodyContent(email, "body");
	}

	@Test
	public void verifyTemplateEngineIsExecutedForTemplate() throws EmailException, MessagingException, IOException {
		Collection<InternetAddress> to = Arrays.asList(InternetAddress.parse("to-1@local.dev, to-2@local.dev"));
		Collection<InternetAddress> cc = Arrays.asList(InternetAddress.parse("cc-1@local.dev, cc-2@local.dev, cc-3@local.dev"));
		Collection<InternetAddress> bcc = Arrays.asList(InternetAddress.parse("bcc@local.dev"));

		ArgumentCaptor<IContext> contextArgumentCaptor = ArgumentCaptor.forClass(IContext.class);
		when(templateEngine.process(eq("template"), contextArgumentCaptor.capture())).thenReturn("body-from-template");

		Map<String, Object> variables = Map.of("varA", "valueA", "varB", "valueB", "varC", "valueC");
		HtmlEmail email = generator.createHtmlEmailFromTemplate("subject", to, cc, bcc, "template", variables, Locale.GERMANY);

		assertThat(email.getSubject()).isEqualTo("subject");
		assertThat(email.getToAddresses()).containsOnlyElementsOf(to);
		assertThat(email.getCcAddresses()).containsOnlyElementsOf(cc);
		assertThat(email.getBccAddresses()).containsOnlyElementsOf(bcc);
		assertBodyContent(email, "body-from-template");

		IContext context = contextArgumentCaptor.getValue();
		assertThat(context.getLocale()).isEqualTo(Locale.GERMANY);
		assertThat(context.getVariable("subject")).isEqualTo("subject");
		assertThat(context.getVariable("recipients")).asList()
				.extracting("email")
				.containsExactly("to-1@local.dev", "to-2@local.dev");
		assertThat(context.getVariable("varA")).isEqualTo("valueA");
		assertThat(context.getVariable("varB")).isEqualTo("valueB");
		assertThat(context.getVariable("varC")).isEqualTo("valueC");
	}

	@Test
	public void verifyTemplateSubjectAndRecipientsMayNotBeOverriddenThroughVariables() throws EmailException, AddressException {
		Collection<InternetAddress> to = Arrays.asList(InternetAddress.parse("to-1@local.dev, to-2@local.dev"));

		ArgumentCaptor<IContext> contextArgumentCaptor = ArgumentCaptor.forClass(IContext.class);
		when(templateEngine.process(eq("template"), contextArgumentCaptor.capture())).thenReturn("body-from-template");

		Map<String, Object> variables = Map.of("subject", "new-subject", "recipients", "modified-recipients");
		generator.createHtmlEmailFromTemplate("subject", to, null, null, "template", variables, Locale.GERMANY);

		IContext context = contextArgumentCaptor.getValue();
		assertThat(context.getVariable("subject")).isEqualTo("subject");
		assertThat(context.getVariable("recipients")).asList()
				.extracting("email")
				.containsExactly("to-1@local.dev", "to-2@local.dev");
	}

	private void assertBodyContent(HtmlEmail email, String body) throws EmailException, MessagingException, IOException {
		email.buildMimeMessage();
		MimeMultipart part = (MimeMultipart) email.getMimeMessage().getContent();
		assertThat(part.getBodyPart(0).getContent()).isEqualTo(body);
	}

	private class ThymeleafHtmlEmailGeneratorWithPreconfiguredEmail extends ThymeleafHtmlEmailGenerator {
		public ThymeleafHtmlEmailGeneratorWithPreconfiguredEmail(ITemplateEngine templateEngine) {
			super(templateEngine);
		}

		@Override
		protected HtmlEmail getPreConfiguredEmail() throws EmailException {
			HtmlEmail email = new HtmlEmail();
			email.setHostName("localhost");
			email.setFrom("from@local-dev");
			email.addReplyTo("reply-to@local.dev");
			return email;
		}
	}
}
