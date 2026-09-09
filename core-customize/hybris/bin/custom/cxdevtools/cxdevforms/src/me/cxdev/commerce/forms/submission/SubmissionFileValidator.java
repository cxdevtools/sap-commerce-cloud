package me.cxdev.commerce.forms.submission;

import me.cxdev.commerce.forms.model.DynamicFormFieldModel;

/**
 * Project hook for ownership, type, size and malware checks of upload references.
 * The default rejects uploads because this extension has no upload endpoint.
 * @since 5.0.2
 */
public interface SubmissionFileValidator {
	boolean isValid(DynamicFormFieldModel field, String reference);
}
