package me.cxdev.commerce.forms.submission;

import me.cxdev.commerce.forms.model.DynamicFormFieldModel;

/** Rejects unverified file references. @since 5.0.2 */
public class RejectingSubmissionFileValidator implements SubmissionFileValidator {
	@Override
	public boolean isValid(final DynamicFormFieldModel field, final String reference) {
		return false;
	}
}
