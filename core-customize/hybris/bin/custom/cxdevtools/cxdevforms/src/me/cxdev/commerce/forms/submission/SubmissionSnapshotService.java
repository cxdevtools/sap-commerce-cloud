package me.cxdev.commerce.forms.submission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;
import me.cxdev.commerce.forms.model.DynamicFormModel;

/** Serializes answer values separately from immutable presentation metadata. @since 5.0.2 */
public class SubmissionSnapshotService {
	private final ObjectMapper objectMapper;
	private final CommonI18NService commonI18NService;
	public SubmissionSnapshotService(final ObjectMapper objectMapper, final CommonI18NService commonI18NService) {
		this.objectMapper = objectMapper;
		this.commonI18NService = commonI18NService;
	}

	public String snapshot(final DynamicFormModel form, final List<DynamicFormFieldModel> fields) {
		final List<SubmissionSnapshot.Field> snapshot = new ArrayList<>();
		for (final DynamicFormFieldModel field : fields) {
			final Map<String, Map<String, String>> options = new LinkedHashMap<>();
			if (field.getFormFieldValues() != null) {
				for (final DynamicFormFieldValueModel option : field.getFormFieldValues()) {
					options.put(option.getId(), translations(option::getLabel));
				}
			}
			snapshot.add(new SubmissionSnapshot.Field(field.getId(), field.getFieldType().getCode(), field.getStepId(),
					translations(field::getStepTitle), translations(field::getLabel), options));
		}
		return toJson(new SubmissionSnapshot(form.getId(), translations(form::getTitle), snapshot));
	}

	public String toJson(final Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (final JsonProcessingException e) {
			throw new IllegalStateException("Cannot serialize form submission", e);
		}
	}

	public SubmissionSnapshot readSnapshot(final String json) {
		try {
			return objectMapper.readValue(json, SubmissionSnapshot.class);
		} catch (final JsonProcessingException e) {
			throw new IllegalStateException("Cannot read submission definition", e);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> readAnswers(final String json) {
		try {
			return objectMapper.readValue(json, LinkedHashMap.class);
		} catch (final JsonProcessingException e) {
			throw new IllegalStateException("Cannot read submission answers", e);
		}
	}

	private Map<String, String> translations(final Function<Locale, String> getter) {
		final Map<String, String> result = new LinkedHashMap<>();
		commonI18NService.getAllLanguages().forEach(language -> {
			final Locale locale = commonI18NService.getLocaleForLanguage(language);
			final String text = getter.apply(locale);
			if (text != null && !text.isBlank()) {
				result.put(locale.toLanguageTag(), text);
			}
		});
		return result;
	}
}
