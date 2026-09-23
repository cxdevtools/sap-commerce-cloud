package me.cxdev.commerce.forms.submission;

/** Request metadata; identity, site and time are resolved on the server. @since 5.0.2 */
public record SubmissionMetadata(String userAgent, String remoteAddress) {
}
