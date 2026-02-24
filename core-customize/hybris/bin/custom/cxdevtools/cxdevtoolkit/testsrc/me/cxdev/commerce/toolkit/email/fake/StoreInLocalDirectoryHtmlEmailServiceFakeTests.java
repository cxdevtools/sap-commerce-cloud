package me.cxdev.commerce.toolkit.email.fake;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.hybris.bootstrap.annotations.UnitTest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@UnitTest
public class StoreInLocalDirectoryHtmlEmailServiceFakeTests {
	private StoreLocallyHtmlEmailServiceFake service;
	private Path tempDirectoryPath;

	@Before
	public void setUp() throws Exception {
		tempDirectoryPath = FileUtils.getTempDirectory().toPath().resolve("emails");
		service = StoreLocallyHtmlEmailServiceFake.storeInFilesystem(tempDirectoryPath.toString(), "{subject}.{extension}", "eml");
	}

	@After
	public void afterClass() throws Exception {
		FileUtils.cleanDirectory(tempDirectoryPath.toFile());
		FileUtils.deleteDirectory(tempDirectoryPath.toFile());
	}

	@Test
	public void verifyHtmlEmailIsStoredInConfiguredDirectory() throws EmailException, MessagingException, IOException {
		HtmlEmail htmlEmail = new HtmlEmail();
		htmlEmail.setCharset("UTF-8");
		htmlEmail.setSubject("Subject");
		htmlEmail.setFrom("from-email@local.dev");
		htmlEmail.setTo(List.of(service.getInternetAddress("to-email@local.dev", "to email")));
		htmlEmail.setHtmlMsg("<html><body>Content</body></html>");

		String messageId = service.sendEmail(htmlEmail);
		assertThat(messageId).isEqualTo(htmlEmail.getMimeMessage().getMessageID());

		File generatedEmail = tempDirectoryPath.resolve("Subject.eml").toFile();
		assertThat(generatedEmail.exists()).isTrue();
		assertThat(generatedEmail.canRead()).isTrue();

		String content = Files.readString(generatedEmail.toPath());
		assertThat(content)
				.containsIgnoringCase("From: from-email@local.dev")
				.containsIgnoringCase("To: to email <to-email@local.dev>")
				.containsIgnoringCase("Message-ID: " + messageId)
				.containsIgnoringCase("Subject: Subject")
				.containsIgnoringCase("<html><body>Content</body></html>");
	}

	@Test
	public void ifSendMailFailsNullIsReturned() throws EmailException {
		HtmlEmail htmlEmail = new TestHtmlEmailThrowingException();
		htmlEmail.setCharset("UTF-8");
		htmlEmail.setSubject("Subject");
		htmlEmail.setFrom("from-email@local.dev");
		htmlEmail.setTo(List.of(service.getInternetAddress("to-email@local.dev", "to email")));
		htmlEmail.setHtmlMsg("<html><body>Content</body></html>");

		String messageId = service.sendEmail(htmlEmail);
		assertThat(messageId).isNull();
	}

	private static class TestHtmlEmailThrowingException extends HtmlEmail {
		@Override
		public MimeMessage getMimeMessage() {
			return new MimeMessage((Session) null) {
				@Override
				public void writeTo(OutputStream os) throws IOException, MessagingException {
					throw new MessagingException("Forced Exception");
				}
			};
		}
	}
}
