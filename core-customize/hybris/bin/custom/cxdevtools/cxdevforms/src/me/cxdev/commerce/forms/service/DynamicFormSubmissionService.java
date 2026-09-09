package me.cxdev.commerce.forms.service;

import java.util.List;
import java.util.Map;

import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormModel;
import me.cxdev.commerce.forms.model.DynamicFormSubmissionModel;
import me.cxdev.commerce.forms.submission.SubmissionMetadata;

/** Persistence boundary for validated submissions. @since 5.0.2 */
public interface DynamicFormSubmissionService {
	/** Saves validated answers and their effective field snapshot in one transaction. */
	DynamicFormSubmissionModel create(DynamicFormModel form, Map<String, Object> answers,
			List<DynamicFormFieldModel> fields, SubmissionMetadata metadata);
}
