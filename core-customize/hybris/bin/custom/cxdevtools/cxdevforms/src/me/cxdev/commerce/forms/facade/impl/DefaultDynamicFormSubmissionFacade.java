package me.cxdev.commerce.forms.facade.impl;

import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import me.cxdev.commerce.forms.facade.DynamicFormSubmissionFacade;
import me.cxdev.commerce.forms.service.DynamicFormService;
import me.cxdev.commerce.forms.service.DynamicFormSubmissionService;
import me.cxdev.commerce.forms.submission.DynamicFormSubmissionRequest;
import me.cxdev.commerce.forms.submission.DynamicFormSubmissionValidator;
import me.cxdev.commerce.forms.submission.SubmissionMetadata;
import me.cxdev.commerce.forms.submission.SubmissionReceipt;

/** Implements the validation and persistence workflow. @since 5.0.2 */
public class DefaultDynamicFormSubmissionFacade implements DynamicFormSubmissionFacade {
	private final DynamicFormService formService;
	private final DynamicFormSubmissionService submissionService;
	private final DynamicFormSubmissionValidator validator;
	public DefaultDynamicFormSubmissionFacade(final DynamicFormService formService,
			final DynamicFormSubmissionService submissionService, final DynamicFormSubmissionValidator validator) {
		this.formService = formService;
		this.submissionService = submissionService;
		this.validator = validator;
	}

	@Override
	public SubmissionReceipt submit(final String formId, final DynamicFormSubmissionRequest request, final SubmissionMetadata metadata) {
		final var form = formService.getDynamicFormForId(formId).orElseThrow(() -> new UnknownIdentifierException("Form not found"));
		final var answers = request == null ? null : request.getAnswers();
		final var fields = validator.validate(form, answers);
		final var submission = submissionService.create(form, answers, fields, metadata);
		return new SubmissionReceipt(submission.getId(), form.getId(), submission.getSubmittedAt());
	}
}
