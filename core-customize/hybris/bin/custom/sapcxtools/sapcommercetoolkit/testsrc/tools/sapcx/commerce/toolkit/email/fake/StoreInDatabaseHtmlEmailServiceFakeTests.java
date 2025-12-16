package tools.sapcx.commerce.toolkit.email.fake;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import tools.sapcx.commerce.toolkit.model.LocallyStoredEmailModel;
import tools.sapcx.commerce.toolkit.testing.itemmodel.InMemoryModelFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@UnitTest
public class StoreInDatabaseHtmlEmailServiceFakeTests {
	private ModelService modelService;
	private MediaService mediaService;

	private LocallyStoredEmailModel generatedEmail;

	private StoreLocallyHtmlEmailServiceFake service;

	@Before
	public void setUp() throws Exception {
		modelService = mock(ModelService.class);
		mediaService = mock(MediaService.class);

		generatedEmail = InMemoryModelFactory.createTestableItemModel(LocallyStoredEmailModel.class);
		when(modelService.create(LocallyStoredEmailModel.class)).thenReturn(generatedEmail);

		service = StoreLocallyHtmlEmailServiceFake.storeInDatabase("emails", "{subject}.{extension}", "eml", modelService, mediaService);
	}

	@Test
	public void verifyHtmlEmailIsStoredInDatabase() throws EmailException, MessagingException, IOException {
		HtmlEmail htmlEmail = new HtmlEmail();
		htmlEmail.setCharset("UTF-8");
		htmlEmail.setSubject("Subject");
		htmlEmail.setFrom("from-email@local.dev");
		htmlEmail.setTo(List.of(service.getInternetAddress("to-email@local.dev", "to email")));
		htmlEmail.setHtmlMsg("<html><body>Content</body></html>");

		String messageId = service.sendEmail(htmlEmail);
		assertThat(messageId).isEqualTo(htmlEmail.getMimeMessage().getMessageID());

		assertThat(generatedEmail.getSender()).isEqualTo("from-email@local.dev");
		assertThat(generatedEmail.getRecipients()).isEqualTo("to email <to-email@local.dev>");
		assertThat(generatedEmail.getDescription()).isEqualTo("Subject");
		assertThat(generatedEmail.getMime()).isEqualTo("application/octet-stream");
		verify(modelService).save(generatedEmail);

		ArgumentCaptor<LocallyStoredEmailModel> emailCaptor = ArgumentCaptor.forClass(LocallyStoredEmailModel.class);
		ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
		verify(mediaService).setDataForMedia(emailCaptor.capture(), contentCaptor.capture());

		assertThat(emailCaptor.getValue()).isEqualTo(generatedEmail);

		String content = new String(contentCaptor.getValue());
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
