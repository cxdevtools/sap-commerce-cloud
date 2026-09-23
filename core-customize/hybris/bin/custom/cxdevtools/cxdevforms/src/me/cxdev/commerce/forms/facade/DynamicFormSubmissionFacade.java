package me.cxdev.commerce.forms.facade;

import me.cxdev.commerce.forms.submission.DynamicFormSubmissionRequest;
import me.cxdev.commerce.forms.submission.SubmissionMetadata;
import me.cxdev.commerce.forms.submission.SubmissionReceipt;

/** Validates form answers before delegating to the service layer. @since 5.0.2 */
public interface DynamicFormSubmissionFacade {
	SubmissionReceipt submit(String formId, DynamicFormSubmissionRequest request, SubmissionMetadata metadata);
}
