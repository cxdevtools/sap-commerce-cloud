package me.cxdev.commerce.forms.submission;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.jupiter.api.Test;

import me.cxdev.commerce.forms.enums.DynamicFormFieldType;
import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;
import me.cxdev.commerce.forms.model.DynamicFormModel;

@UnitTest
class DynamicFormSubmissionValidatorTests {
	private final DynamicFormSubmissionValidator validator = new DynamicFormSubmissionValidator(new RejectingSubmissionFileValidator());
	@Test
	void validatesRequiredConditionalFieldsAndRejectsUnselectedAnswers() {
		final var reason = field("reason", DynamicFormFieldType.SELECT, true);
		final var serial = field("serial", DynamicFormFieldType.TEXT, true);
		final var technical = mock(DynamicFormFieldValueModel.class);
		when(technical.getId()).thenReturn("technical");
		when(technical.getChildFields()).thenReturn(Set.of(serial));
		final var general = mock(DynamicFormFieldValueModel.class);
		when(general.getId()).thenReturn("general");
		when(reason.getFormFieldValues()).thenReturn(List.of(technical, general));
		final var form = form(reason);
		assertThat(validator.validate(form, Map.of("reason", "technical", "serial", "ABC"))).containsExactly(reason, serial);
		assertCode(form, Map.of("reason", "technical"), "required");
		assertCode(form, Map.of("reason", "general", "serial", "ABC"), "unknown");
		assertCode(form, Map.of("reason", "foreign"), "option");
	}

	@Test
	void validatesBooleansDecimalsUnicodeAndInclusiveBounds() {
		final var consent = field("consent", DynamicFormFieldType.CHECKBOX, true);
		final var number = field("number", DynamicFormFieldType.NUMBER, true);
		when(number.getMinValue()).thenReturn(0.0);
		when(number.getMaxValue()).thenReturn(2.5);
		final var name = field("name", DynamicFormFieldType.TEXT, true);
		when(name.getMaxLength()).thenReturn(1);
		final var form = form(consent, number, name);
		assertThat(validator.validate(form, Map.of("consent", true, "number", "0", "name", "😀"))).hasSize(3);
		assertThat(validator.validate(form, Map.of("consent", true, "number", "2.5", "name", "A"))).hasSize(3);
		assertCode(form, Map.of("consent", false, "number", "2.5000000000000001", "name", "AB"), "required", "maxValue", "maxLength");
		assertCode(form, Map.of("consent", "true", "number", 3, "name", "A"), "type");
	}

	@Test
	void validatesDatesWeeksAndEmailWithoutLeakingValues() {
		final var form = form(field("date", DynamicFormFieldType.DATE, false), field("week", DynamicFormFieldType.WEEK, false), field("email", DynamicFormFieldType.EMAIL, false));
		assertThat(validator.validate(form, Map.of("date", "2024-02-29", "week", "2020-W53", "email", "ada@example.org"))).hasSize(3);
		assertCode(form, Map.of("date", "2023-02-29", "week", "2021-W53", "email", "SECRET"), "date", "week", "email");
		try {
			validator.validate(form, Map.of("email", "SECRET"));
			fail("Expected validation");
		} catch (SubmissionValidationException ex) {
			assertThat(ex.getErrors().getFieldErrors()).allSatisfy(e -> assertThat(e.getRejectedValue()).isNull());
		}
	}

	@Test
	void rejectsInactiveFieldsDuplicateOptionsFilesAndPasswords() {
		final var inactive = field("inactive", DynamicFormFieldType.TEXT, false);
		when(inactive.isActive()).thenReturn(false);
		final var choices = field("choices", DynamicFormFieldType.CHECKBOXES, false);
		final var option = mock(DynamicFormFieldValueModel.class);
		when(option.getId()).thenReturn("a");
		when(choices.getFormFieldValues()).thenReturn(List.of(option));
		final var form = form(inactive, choices, field("file", DynamicFormFieldType.FILE, false), field("password", DynamicFormFieldType.PASSWORD, false));
		assertCode(form, Map.of("inactive", "x", "choices", List.of("a", "a"), "file", List.of("foreign-upload"), "password", "secret"), "unknown", "option", "file", "password");
	}

	@Test
	void rejectsCyclesAndAllowsSharedChildOnce() {
		final var root = field("root", DynamicFormFieldType.SELECT, false);
		final var option = mock(DynamicFormFieldValueModel.class);
		when(option.getId()).thenReturn("a");
		when(option.getChildFields()).thenReturn(Set.of(root));
		when(root.getFormFieldValues()).thenReturn(List.of(option));
		assertCode(form(root), Map.of("root", "a"), "configuration");
		final var child = field("child", DynamicFormFieldType.TEXT, false);
		when(option.getChildFields()).thenReturn(Set.of(child));
		assertThat(validator.validate(form(root, child), Map.of("root", "a"))).containsExactly(root, child);
	}

	@Test
	void optionalEmptyAndZeroHaveDistinctSemantics() {
		assertThat(validator.validate(form(field("n", DynamicFormFieldType.NUMBER, true)), Map.of("n", "0"))).hasSize(1);
		assertCode(form(field("n", DynamicFormFieldType.NUMBER, true)), Map.of("n", " "), "required");
		assertCode(form(), null, "answers");
	}

	private void assertCode(final DynamicFormModel form, final Map<String, Object> answers, final String... codes) {
		try {
			validator.validate(form, answers);
			fail("Expected validation errors");
		} catch (SubmissionValidationException ex) {
			assertThat(ex.getErrors().getAllErrors()).extracting(e -> e.getCode())
					.contains(Arrays.stream(codes).map(c -> "cxdevforms.submission." + c).toArray(String[]::new));
		}
	}

	private DynamicFormModel form(final DynamicFormFieldModel... fields) {
		final var form = mock(DynamicFormModel.class);
		when(form.getFormFields()).thenReturn(List.of(fields));
		return form;
	}

	private DynamicFormFieldModel field(final String id, final DynamicFormFieldType type, final boolean required) {
		final var field = mock(DynamicFormFieldModel.class);
		when(field.getId()).thenReturn(id);
		when(field.getFieldType()).thenReturn(type);
		when(field.getMinLength()).thenReturn(null);
		when(field.getMaxLength()).thenReturn(null);
		when(field.getMinValue()).thenReturn(null);
		when(field.getMaxValue()).thenReturn(null);
		when(field.isActive()).thenReturn(true);
		when(field.isRequired()).thenReturn(required);
		return field;
	}
}
