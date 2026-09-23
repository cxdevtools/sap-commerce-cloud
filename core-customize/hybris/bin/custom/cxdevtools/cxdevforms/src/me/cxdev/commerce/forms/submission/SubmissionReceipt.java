package me.cxdev.commerce.forms.submission;

import java.util.Date;

/** Public creation receipt containing no answer or identity data. @since 5.0.2 */
public record SubmissionReceipt(String submissionId, String formId, Date submittedAt) {
}
