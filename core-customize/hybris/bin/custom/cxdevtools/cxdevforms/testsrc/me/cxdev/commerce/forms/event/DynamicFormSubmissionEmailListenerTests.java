package me.cxdev.commerce.forms.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.PK;
import de.hybris.platform.servicelayer.model.ModelService;

import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import me.cxdev.commerce.forms.model.DynamicFormModel;
import me.cxdev.commerce.forms.model.DynamicFormSubmissionModel;
import me.cxdev.commerce.forms.submission.SubmissionPresentationService;
import me.cxdev.commerce.forms.submission.SubmissionSnapshotService;
import me.cxdev.commerce.toolkit.email.HtmlEmailGenerator;
import me.cxdev.commerce.toolkit.email.HtmlEmailService;

@UnitTest
class DynamicFormSubmissionEmailListenerTests {
	@Test
	void sendsOnlyOnOriginUsesConfiguredTemplateAndFallsBackWhenBlank() throws Exception {
		final var models = mock(ModelService.class);
		final var generator = mock(HtmlEmailGenerator.class, CALLS_REAL_METHODS);
		final var mail = mock(HtmlEmailService.class);
		final var snapshots = mock(SubmissionSnapshotService.class);
		final var presentation = mock(SubmissionPresentationService.class);
		final var messages = new StaticMessageSource();
		messages.addMessage("cxdevforms.submission.subject", Locale.GERMAN, "Neue Anfrage: {0}");
		final var listener = new DynamicFormSubmissionEmailListener(models, generator, mail, snapshots, presentation, messages) {
			@Override
			protected int getNodeId() {
				return 7;
			}
		};
		final var event = new DynamicFormSubmissionCreatedEvent(PK.fromLong(1), "uuid", "contact", 7);
		listener.onEvent(new DynamicFormSubmissionCreatedEvent(PK.fromLong(1), "uuid", "contact", 8));
		verifyNoInteractions(models);
		final var submission = mock(DynamicFormSubmissionModel.class);
		final var form = mock(DynamicFormModel.class);
		when(models.get(event.getSubmissionPk())).thenReturn(submission);
		when(submission.getForm()).thenReturn(form);
		when(submission.getLanguage()).thenReturn("de");
		when(submission.getAnswersJson()).thenReturn("{}");
		when(form.getRecipients()).thenReturn(List.of("service@example.org"));
		when(form.getSubmissionEmailTemplate(Locale.GERMAN)).thenReturn("<p>Custom</p>");
		when(presentation.present(submission, Locale.GERMAN)).thenReturn(new SubmissionPresentationService.View("Kontakt", List.of()));
		when(snapshots.readAnswers("{}")).thenReturn(Map.of());
		when(generator.createHtmlEmail()).thenAnswer(call -> new HtmlEmail());
		listener.onEvent(event);
		verify(generator).processTemplate(eq("<p>Custom</p>"), argThat(context -> context.containsKey("answersJson") && context.containsKey("answers")), eq(Locale.GERMAN));
		verify(mail).sendEmail(any());
		when(form.getSubmissionEmailTemplate(Locale.GERMAN)).thenReturn(" ");
		listener.onEvent(event);
		verify(generator).processTemplate(eq("html/cxdevforms-submission"), any(), eq(Locale.GERMAN));
		when(generator.createHtmlEmail()).thenThrow(new org.apache.commons.mail2.core.EmailException("private template"));
		assertThatCode(() -> listener.onEvent(event)).doesNotThrowAnyException();
	}
}
