package me.cxdev.commerce.forms.submission;

import java.util.List;
import java.util.Map;

/** Immutable description of submitted fields and their translations. @since 5.0.2 */
public record SubmissionSnapshot(String formId, Map<String, String> title, List<Field> fields) {
	public record Field(String id, String type, String stepId, Map<String, String> stepTitle,
			Map<String, String> label, Map<String, Map<String, String>> options) {
	}
}
