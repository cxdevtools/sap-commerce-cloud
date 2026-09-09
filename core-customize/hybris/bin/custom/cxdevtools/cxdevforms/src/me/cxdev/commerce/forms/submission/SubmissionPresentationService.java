package me.cxdev.commerce.forms.submission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;

import me.cxdev.commerce.forms.model.DynamicFormSubmissionModel;

/** Shared, localized read model for Backoffice and notification templates. @since 5.0.2 */
public class SubmissionPresentationService {
	private final SubmissionSnapshotService snapshotService;
	private final MessageSource messageSource;
	public SubmissionPresentationService(final SubmissionSnapshotService snapshotService, final MessageSource messageSource) {
		this.snapshotService = snapshotService;
		this.messageSource = messageSource;
	}

	public View present(final DynamicFormSubmissionModel submission, final Locale locale) {
		final SubmissionSnapshot snapshot = snapshotService.readSnapshot(submission.getDefinitionSnapshotJson());
		final Map<String, Object> answers = snapshotService.readAnswers(submission.getAnswersJson());
		final Map<String, List<Answer>> groups = new LinkedHashMap<>();
		final Map<String, String> titles = new LinkedHashMap<>();
		for (final SubmissionSnapshot.Field field : snapshot.fields()) {
			final String step = field.stepId() == null ? "" : field.stepId();
			titles.putIfAbsent(step, label(field.stepTitle(), locale, submission.getLanguage(), step.isEmpty()
					? messageSource.getMessage("cxdevforms.submission.details", null, locale)
					: step));
			groups.computeIfAbsent(step, key -> new ArrayList<>()).add(new Answer(field.id(),
					label(field.label(), locale, submission.getLanguage(), field.id()),
					"PASSWORD".equals(field.type()) ? "••••••" : display(answers.get(field.id()), field, locale, submission.getLanguage())));
		}
		final List<Section> sections = groups.entrySet().stream().map(e -> new Section(titles.get(e.getKey()), e.getValue())).toList();
		return new View(label(snapshot.title(), locale, submission.getLanguage(), snapshot.formId()), sections);
	}

	private String display(final Object value, final SubmissionSnapshot.Field field, final Locale locale, final String language) {
		if (value == null || value instanceof String s && s.isBlank() || value instanceof List<?> l && l.isEmpty()) {
			return messageSource.getMessage("cxdevforms.submission.unanswered", null, locale);
		}
		if (value instanceof Boolean flag) {
			return messageSource.getMessage("cxdevforms.submission." + (flag ? "yes" : "no"), null, locale);
		}
		if (value instanceof List<?> values) {
			return values.stream().map(v -> display(v, field, locale, language)).collect(Collectors.joining(", "));
		}
		final String text = String.valueOf(value);
		return label(field.options().get(text), locale, language, text);
	}

	private String label(final Map<String, String> translations, final Locale locale, final String language, final String fallback) {
		if (translations == null) {
			return fallback;
		}
		for (final String key : List.of(locale.toLanguageTag(), locale.getLanguage(), language, "en")) {
			final String text = translations.get(key);
			if (text != null && !text.isBlank()) {
				return text;
			}
		}
		return fallback;
	}
	public record View(String title, List<Section> sections) {
	}
	public record Section(String title, List<Answer> answers) {
	}
	public record Answer(String fieldId, String label, String value) {
	}
}
