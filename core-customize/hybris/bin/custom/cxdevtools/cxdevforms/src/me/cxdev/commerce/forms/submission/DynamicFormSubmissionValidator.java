package me.cxdev.commerce.forms.submission;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;
import me.cxdev.commerce.forms.model.DynamicFormModel;

/** Server-authoritative validation of effective fields and typed answers. @since 5.0.2 */
public class DynamicFormSubmissionValidator {
	private static final Pattern DECIMAL = Pattern.compile("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?");
	private final SubmissionFileValidator fileValidator;
	public DynamicFormSubmissionValidator(final SubmissionFileValidator fileValidator) {
		this.fileValidator = fileValidator;
	}

	/** Returns the ordered effective fields or throws an exception containing Spring Errors. */
	public List<DynamicFormFieldModel> validate(final DynamicFormModel form, final Map<String, Object> answers) {
		final Errors errors = new MapBindingResult(new HashMap<>(), "submission");
		if (answers == null || answers.size() > 1000) {
			errors.reject("cxdevforms.submission.answers");
			throw new SubmissionValidationException(errors);
		}
		final Map<String, DynamicFormFieldModel> effective = new LinkedHashMap<>();
		for (final DynamicFormFieldModel field : safe(form.getFormFields())) {
			visit(field, answers, effective, new HashSet<>(), errors, 0);
		}
		for (final String id : answers.keySet()) {
			if (!effective.containsKey(id)) {
				reject(errors, id, "unknown");
			}
		}
		if (errors.hasErrors()) {
			throw new SubmissionValidationException(errors);
		}
		return List.copyOf(effective.values());
	}

	private void visit(final DynamicFormFieldModel field, final Map<String, Object> answers,
			final Map<String, DynamicFormFieldModel> effective, final Set<String> path, final Errors errors, final int depth) {
		if (!field.isActive()) {
			return;
		}
		final String id = field.getId();
		if (id == null || depth > 64 || path.contains(id) || effective.size() >= 1000) {
			errors.reject("cxdevforms.submission.configuration");
			return;
		}
		if (effective.putIfAbsent(id, field) != null) {
			return;
		}
		final Object value = answers.get(id);
		validateValue(field, value, errors);
		path.add(id);
		for (final DynamicFormFieldValueModel option : safe(field.getFormFieldValues())) {
			if (Objects.equals(option.getId(), value) || value instanceof List<?> list && list.contains(option.getId())) {
				for (final DynamicFormFieldModel child : safe(option.getChildFields()).stream()
						.sorted(Comparator.comparing(DynamicFormFieldModel::getId, Comparator.nullsLast(String::compareTo))).toList()) {
					visit(child, answers, effective, path, errors, depth + 1);
				}
			}
		}
		path.remove(id);
	}

	private void validateValue(final DynamicFormFieldModel field, final Object value, final Errors errors) {
		final String id = field.getId();
		final String type = field.getFieldType() == null ? "TEXT" : field.getFieldType().getCode();
		final boolean empty = value == null || value instanceof String s && s.isBlank() || value instanceof List<?> l && l.isEmpty();
		if (field.isRequired() && (empty || "CHECKBOX".equals(type) && !Boolean.TRUE.equals(value))) {
			reject(errors, id, "required");
		}
		if (value == null) {
			return;
		}
		if ("PASSWORD".equals(type)) {
			reject(errors, id, "password");
			return;
		}
		if ("CHECKBOX".equals(type)) {
			if (!(value instanceof Boolean)) {
				reject(errors, id, "type");
			}
			return;
		}
		if ("CHECKBOXES".equals(type) || "FILE".equals(type)) {
			if (!(value instanceof List<?> list) || list.size() > 1000 || list.stream().anyMatch(v -> !(v instanceof String))) {
				reject(errors, id, "type");
				return;
			}
			if (new HashSet<>(list).size() != list.size()) {
				reject(errors, id, "option");
			}
			for (final Object entry : list) {
				if ("FILE".equals(type)) {
					if (((String) entry).length() > 1024 || !fileValidator.isValid(field, (String) entry)) {
						reject(errors, id, "file");
					}
				} else if (!hasOption(field, (String) entry)) {
					reject(errors, id, "option");
				}
			}
			return;
		}
		if (!(value instanceof String text)) {
			reject(errors, id, "type");
			return;
		}
		final int length = text.codePointCount(0, text.length());
		if (length > 65536 || field.getMaxLength() != null && length > field.getMaxLength()) {
			reject(errors, id, "maxLength");
		}
		if (empty) {
			return;
		}
		if (field.getMinLength() != null && length < field.getMinLength()) {
			reject(errors, id, "minLength");
		}
		switch (type) {
			case "NUMBER" -> validateNumber(field, text, errors);
			case "EMAIL" -> {
				try {
					final InternetAddress address = new InternetAddress(text, true);
					address.validate();
					if (!text.equals(address.getAddress()) || !text.contains("@")) {
						reject(errors, id, "email");
					}
				} catch (final AddressException e) {
					reject(errors, id, "email");
				}
			}
			case "COLOR" -> {
				if (!text.matches("#[0-9a-fA-F]{6}")) {
					reject(errors, id, "color");
				}
			}
			case "DATE" -> {
				try {
					if (!text.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
						throw new IllegalArgumentException();
					}
					LocalDate.parse(text);
				} catch (final RuntimeException e) {
					reject(errors, id, "date");
				}
			}
			case "WEEK" -> {
				try {
					if (!text.matches("[0-9]{4}-W[0-9]{2}")) {
						throw new IllegalArgumentException();
					}
					final int year = Integer.parseInt(text.substring(0, 4));
					final int week = Integer.parseInt(text.substring(6));
					if (week < 1 || week > LocalDate.of(year, 12, 28).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)) {
						throw new IllegalArgumentException();
					}
				} catch (final RuntimeException e) {
					reject(errors, id, "week");
				}
			}
			case "SELECT", "RADIO" -> {
				if (!hasOption(field, text)) {
					reject(errors, id, "option");
				}
			}
			case "TEXT", "TEXTAREA", "HIDDEN" -> {
			}
			default -> reject(errors, id, "type");
		}
	}

	private void validateNumber(final DynamicFormFieldModel field, final String text, final Errors errors) {
		if (text.length() > 256 || !DECIMAL.matcher(text).matches()) {
			reject(errors, field.getId(), "number");
			return;
		}
		final BigDecimal number = new BigDecimal(text);
		if (field.getMinValue() != null && number.compareTo(BigDecimal.valueOf(field.getMinValue())) < 0) {
			reject(errors, field.getId(), "minValue");
		}
		if (field.getMaxValue() != null && number.compareTo(BigDecimal.valueOf(field.getMaxValue())) > 0) {
			reject(errors, field.getId(), "maxValue");
		}
	}

	private boolean hasOption(final DynamicFormFieldModel field, final String id) {
		return safe(field.getFormFieldValues()).stream().anyMatch(option -> id.equals(option.getId()));
	}

	private void reject(final Errors errors, final String id, final String code) {
		((MapBindingResult) errors).addError(new FieldError("submission", "answers[" + id + "]", null, false,
				new String[] { "cxdevforms.submission." + code }, null, null));
	}

	private static <T> Collection<T> safe(final Collection<T> values) {
		return values == null ? List.of() : values;
	}
}
