package me.cxdev.commerce.toolkit.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.media.MediaModel;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.junit.Before;
import org.junit.Test;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;
import me.cxdev.commerce.toolkit.email.attachments.HtmlEmailAttachmentBuilders;
import me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory;
import me.cxdev.commerce.toolkit.testing.testdoubles.email.HtmlEmailGeneratorFake;

@UnitTest
public class HtmlEmailBuilderTests {
	private HtmlEmailBuilder builder;

	@Before
	public void setUp() {
		builder = HtmlEmailBuilder.withHtmlEmailGenerator(new HtmlEmailGeneratorFake());
	}

	@Test(expected = EmailException.class)
	public void failsWithoutToAddress() throws EmailException {
		builder.build();
	}

	@Test(expected = EmailException.class)
	public void failsWithoutBodyOrTemplate() throws EmailException {
		builder
				.to("mail@localhost")
				.build();
	}

	@Test(expected = EmailException.class)
	public void failsWithBodyAndTemplate() throws EmailException {
		builder
				.to("mail@localhost")
				.body("body")
				.template("template", Locale.ENGLISH)
				.build();
	}

	@Test
	public void testToAddressResolution() throws Exception {
		HtmlEmail htmlEmail = builder
				.body("ignored-in-this-test-but-required")
				.to("email-without-name@localhost")
				.to("name", "email-with-name@localhost")
				.to(new InternetAddress("email@localhost", "first last"))
				.build();

		assertThat(htmlEmail.getToAddresses())
				.extracting("address", "personal")
				.containsExactly(
						tuple("email-without-name@localhost", null),
						tuple("email-with-name@localhost", "name"),
						tuple("email@localhost", "first last"));
	}

	@Test
	public void testCcAddressResolution() throws Exception {
		HtmlEmail htmlEmail = builder
				.body("ignored-in-this-test-but-required")
				.to("requires-at-least-one-to-address@localhost")
				.cc("email-without-name@localhost")
				.cc("name", "email-with-name@localhost")
				.cc(new InternetAddress("email@localhost", "first last"))
				.build();

		assertThat(htmlEmail.getCcAddresses())
				.extracting("address", "personal")
				.containsExactly(
						tuple("email-without-name@localhost", null),
						tuple("email-with-name@localhost", "name"),
						tuple("email@localhost", "first last"));
	}

	@Test
	public void testBccAddressResolution() throws Exception {
		HtmlEmail htmlEmail = builder
				.body("ignored-in-this-test-but-required")
				.to("requires-at-least-one-to-address@localhost")
				.bcc("email-without-name@localhost")
				.bcc("name", "email-with-name@localhost")
				.bcc(new InternetAddress("email@localhost", "first last"))
				.build();

		assertThat(htmlEmail.getBccAddresses())
				.extracting("address", "personal")
				.containsExactly(
						tuple("email-without-name@localhost", null),
						tuple("email-with-name@localhost", "name"),
						tuple("email@localhost", "first last"));
	}

	@Test
	public void testReplyToAddressResolution() throws Exception {
		HtmlEmail htmlEmail = builder
				.body("ignored-in-this-test-but-required")
				.to("requires-at-least-one-to-address@localhost")
				.replyTo("email-without-name@localhost")
				.replyTo("name", "email-with-name@localhost")
				.replyTo(new InternetAddress("email@localhost", "first last"))
				.build();

		assertThat(htmlEmail.getReplyToAddresses())
				.extracting("address", "personal")
				.containsExactly(
						tuple("email-without-name@localhost", null),
						tuple("email-with-name@localhost", "name"),
						tuple("email@localhost", "first last"));
	}

	@Test
	public void testSubjectAndBody() throws Exception {
		HtmlEmail htmlEmail = builder
				.subject("custom subject for email")
				.body("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent vel ligula luctus, facilisis duis.")
				.to("requires-at-least-one-to-address@localhost")
				.build();

		assertThat(htmlEmail.getSubject()).isEqualTo("custom subject for email");
		assertBodyContent(htmlEmail, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent vel ligula luctus, facilisis duis.");
	}

	@Test
	public void testSubjectAndTemplate() throws Exception {
		HtmlEmail htmlEmail = builder
				.subject("custom subject for email")
				.template("templateName", Locale.ENGLISH)
				.to("requires-at-least-one-to-address@localhost")
				.build();

		assertThat(htmlEmail.getSubject()).isEqualTo("custom subject for email");
		assertBodyContent(htmlEmail, "templateName");
	}

	@Test
	public void testAttachments() throws Exception {
		Path tempFile = Files.createTempFile("file-attachment", ".txt");

		MediaModel mediaModel = InMemoryModelFactory.createTestableItemModel(MediaModel.class);
		mediaModel.setCode("00000001");
		mediaModel.setRealFileName("media-attachment-00000001.txt");
		mediaModel.setDescription("media attachment description");

		HtmlEmail htmlEmail = builder
				.to("requires-at-least-one-to-address@localhost")
				.body("ignored-in-this-test-but-required")
				.attach(tempFile.toFile())
				.attach(HtmlEmailAttachmentBuilders.forUrl(new URL("file:" + tempFile.toAbsolutePath())).name("url-attachment.txt").description("url attachment description"))
				// .attach(mediaModel)
				.build();

		htmlEmail.buildMimeMessage();
		assertThat(htmlEmail)
				.extracting("container")
				.extracting("parts")
				.asList()
				.extractingResultOf("getDisposition")
				.containsExactly(null, "attachment", "attachment");

		assertThat(htmlEmail)
				.extracting("container")
				.extracting("parts")
				.asList()
				.extractingResultOf("getContentType")
				.containsExactly("text/plain", "text/plain", "text/plain");

		assertThat(htmlEmail)
				.extracting("container")
				.extracting("parts")
				.asList()
				.extractingResultOf("getFileName")
				.containsExactly(null, tempFile.getFileName().toString(), "url-attachment.txt");

		assertThat(htmlEmail)
				.extracting("container")
				.extracting("parts")
				.asList()
				.extractingResultOf("getDescription")
				.containsExactly(null, null, "url attachment description");
	}

	private void assertBodyContent(HtmlEmail email, String body) throws EmailException, MessagingException, IOException {
		email.buildMimeMessage();
		MimeMultipart part = (MimeMultipart) email.getMimeMessage().getContent();
		assertThat(part.getBodyPart(0).getContent()).isEqualTo(body);
	}
}
