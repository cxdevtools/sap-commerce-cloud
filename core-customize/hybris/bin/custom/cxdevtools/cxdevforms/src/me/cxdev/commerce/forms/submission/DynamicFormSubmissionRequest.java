package me.cxdev.commerce.forms.submission;

import java.util.Map;

/** ID-keyed, typed answers; the form ID is supplied in the URL. @since 5.0.2 */
public class DynamicFormSubmissionRequest {
	private Map<String, Object> answers;
	public Map<String, Object> getAnswers() {
		return answers;
	}

	public void setAnswers(final Map<String, Object> answers) {
		this.answers = answers;
	}
}
