package me.cxdev.commerce.toolkit.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;

import de.hybris.bootstrap.annotations.UnitTest;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.junit.Before;
import org.junit.Test;

import jakarta.mail.internet.InternetAddress;

@UnitTest
public class DefaultHtmlEmailServiceTests {
	private DefaultHtmlEmailService emailService;

	@Before
	public void setUp() throws Exception {
		emailService = new DefaultHtmlEmailService();
	}

	@Test
	public void verifySendEmailDelegatesToHtmlEmailObject() throws EmailException {
		TestHtmlEmail email = new TestHtmlEmail();
		String messageId = emailService.sendEmail(email);
		assertThat(email.numberOfInvocationsOfSend).isEqualTo(1);
		assertThat(messageId).isEqualTo(email.randomMessageId);
	}

	@Test
	public void verifyProxyMethodWrapsHtmlEmailObject() throws EmailException {
		TestHtmlEmail email = new TestHtmlEmail();

		HtmlEmail proxy = emailService.proxy(email);
		String messageId = proxy.send();

		assertThat(email.numberOfInvocationsOfSend).isEqualTo(1);
		assertThat(messageId).isEqualTo(email.randomMessageId);
	}

	@Test
	public void verifyInternetAddressCanBeCreated() throws EmailException {
		InternetAddress internetAddress = emailService.getInternetAddress("email@local.dev");

		assertThat(internetAddress.getAddress()).isEqualTo("email@local.dev");
		assertThat(internetAddress.getPersonal()).isEqualTo("email@local.dev");
	}

	@Test
	public void verifyInternetAddressWithPersonalCanBeCreated() throws EmailException {
		InternetAddress internetAddress = emailService.getInternetAddress("email@local.dev", "personal name");

		assertThat(internetAddress.getAddress()).isEqualTo("email@local.dev");
		assertThat(internetAddress.getPersonal()).isEqualTo("personal name");
	}

	@Test(expected = EmailException.class)
	public void verifyInvalidInternetAddressThrowException() throws EmailException {
		emailService.getInternetAddress("not valid internet address");
	}

	private static class TestHtmlEmail extends HtmlEmail {
		int numberOfInvocationsOfSend = 0;
		String randomMessageId = "id";

		public TestHtmlEmail() {
		}

		@Override
		public String send() throws EmailException {
			numberOfInvocationsOfSend++;
			byte[] bytes = new byte[8];
			new Random().nextBytes(bytes);
			randomMessageId = new String(bytes);
			return randomMessageId;
		}
	}
}
