package me.cxdev.commerce.forms.submission;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import me.cxdev.commerce.forms.model.DynamicFormModel;
import me.cxdev.commerce.forms.model.DynamicFormSubmissionModel;

@UnitTest
class SubmissionPresentationTests {
	@Test
	void retainsTranslatedOptionsGroupsAndEscapesFallbackEmail() {
		final var messages = new ResourceBundleMessageSource();
		messages.setBasename("localization/cxdevforms-submissions");
		messages.setDefaultEncoding("UTF-8");
		final var snapshots = new SubmissionSnapshotService(new ObjectMapper(), mock(CommonI18NService.class));
		final var snapshot = new SubmissionSnapshot("contact", Map.of("en", "Contact", "de", "Kontakt"), List.of(
				new SubmissionSnapshot.Field("reason", "SELECT", "request", Map.of("de", "Anliegen"), Map.of("de", "Grund"),
						Map.of("tech", Map.of("de", "Technik", "en", "Technical"))),
				new SubmissionSnapshot.Field("text", "TEXT", "request", Map.of(), Map.of("de", "Nachricht"), Map.of()),
				new SubmissionSnapshot.Field("consent", "CHECKBOX", "consent", Map.of("de", "Einwilligung"), Map.of(), Map.of())));
		final var form = mock(DynamicFormModel.class);
		when(form.getId()).thenReturn("contact");
		final var submission = mock(DynamicFormSubmissionModel.class);
		when(submission.getId()).thenReturn("uuid");
		when(submission.getForm()).thenThrow(new IllegalStateException("Definition no longer available"));
		when(submission.getLanguage()).thenReturn("en");
		when(submission.getDefinitionSnapshotJson()).thenReturn(snapshots.toJson(snapshot));
		when(submission.getAnswersJson()).thenReturn(snapshots.toJson(Map.of("reason", "tech", "text", "<script>alert(1)</script>", "consent", false)));
		final var view = new SubmissionPresentationService(snapshots, messages).present(submission, Locale.GERMAN);
		assertThat(view.title()).isEqualTo("Kontakt");
		assertThat(view.sections()).hasSize(2);
		assertThat(view.sections().get(0).title()).isEqualTo("Anliegen");
		assertThat(view.sections().get(0).answers().get(0).value()).isEqualTo("Technik");
		assertThat(view.sections().get(1).answers().get(0).value()).isEqualTo("Nein");
		final var resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("email-templates/");
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding("UTF-8");
		final var engine = new SpringTemplateEngine();
		engine.setTemplateResolver(resolver);
		final String html = engine.process("html/cxdevforms-submission", new Context(Locale.GERMAN, Map.of("view", view, "submission", submission)));
		assertThat(html).contains("Technik", "Einwilligung", "&lt;script&gt;").doesNotContain("<script>");
		verify(form, never()).getFormFields();
	}
}
