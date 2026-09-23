package me.cxdev.commerce.forms.event;

import java.util.Locale;
import java.util.Map;

import de.hybris.platform.core.Registry;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

import org.apache.commons.mail2.core.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import me.cxdev.commerce.forms.model.DynamicFormSubmissionModel;
import me.cxdev.commerce.forms.submission.SubmissionPresentationService;
import me.cxdev.commerce.forms.submission.SubmissionSnapshotService;
import me.cxdev.commerce.toolkit.email.HtmlEmailGenerator;
import me.cxdev.commerce.toolkit.email.HtmlEmailService;

/**
 * Optional best-effort notification listener, instantiated only by explicit import.
 * Only the originating node sends mail to avoid one email per cluster node.
 * Durable retries and delivery deduplication belong in a project outbox listener.
 * @since 5.0.2
 */
public class DynamicFormSubmissionEmailListener extends AbstractEventListener<DynamicFormSubmissionCreatedEvent> {
	private static final Logger LOG = LoggerFactory.getLogger(DynamicFormSubmissionEmailListener.class);
	private final ModelService modelService;
	private final HtmlEmailGenerator emailGenerator;
	private final HtmlEmailService emailService;
	private final SubmissionSnapshotService snapshotService;
	private final SubmissionPresentationService presentationService;
	private final MessageSource messageSource;
	public DynamicFormSubmissionEmailListener(final ModelService modelService, final HtmlEmailGenerator emailGenerator,
			final HtmlEmailService emailService, final SubmissionSnapshotService snapshotService,
			final SubmissionPresentationService presentationService, final MessageSource messageSource) {
		this.modelService = modelService;
		this.emailGenerator = emailGenerator;
		this.emailService = emailService;
		this.snapshotService = snapshotService;
		this.presentationService = presentationService;
		this.messageSource = messageSource;
	}

	@Override
	protected void onEvent(final DynamicFormSubmissionCreatedEvent event) {
		if (event.getOriginNodeId() != getNodeId()) {
			return;
		}
		try {
			final DynamicFormSubmissionModel submission = modelService.get(event.getSubmissionPk());
			final var form = submission.getForm();
			if (form.getRecipients() == null || form.getRecipients().isEmpty()) {
				return;
			}
			final Locale locale = Locale.forLanguageTag(submission.getLanguage());
			final var view = presentationService.present(submission, locale);
			final String configured = form.getSubmissionEmailTemplate(locale);
			final String template = configured == null || configured.isBlank() ? "html/cxdevforms-submission" : configured;
			final var builder = emailGenerator.newHtmlEmail()
					.subject(messageSource.getMessage("cxdevforms.submission.subject", new Object[] { view.title() }, locale))
					.locale(locale).template(template)
					.templateParameter(Map.of("submission", submission, "view", view,
							"answers", snapshotService.readAnswers(submission.getAnswersJson()),
							"answersJson", submission.getAnswersJson()));
			form.getRecipients().forEach(builder::to);
			emailService.sendEmail(builder.build());
		} catch (final EmailException | RuntimeException e) {
			// Template exceptions may contain HTML or answers; never log their message/stack.
			LOG.warn("Could not send notification for submission {} ({})", event.getSubmissionId(), e.getClass().getSimpleName());
		}
	}

	protected int getNodeId() {
		return Registry.getClusterID();
	}
}
