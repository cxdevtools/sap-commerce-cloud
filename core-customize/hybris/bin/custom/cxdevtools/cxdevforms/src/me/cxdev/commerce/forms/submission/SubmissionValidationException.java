package me.cxdev.commerce.forms.submission;

import org.springframework.validation.Errors;

/** Carries standard Spring validation errors without submitted values. @since 5.0.2 */
public class SubmissionValidationException extends RuntimeException {
	private final Errors errors;
	public SubmissionValidationException(final Errors errors) {
		super("Invalid form submission");
		this.errors = errors;
	}

	public Errors getErrors() {
		return errors;
	}
}
