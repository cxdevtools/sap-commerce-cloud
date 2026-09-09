package me.cxdev.commerce.forms.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hybris.platform.core.Registry;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

import org.springframework.transaction.support.TransactionTemplate;

import me.cxdev.commerce.forms.event.DynamicFormSubmissionCreatedEvent;
import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormModel;
import me.cxdev.commerce.forms.model.DynamicFormSubmissionModel;
import me.cxdev.commerce.forms.service.DynamicFormSubmissionService;
import me.cxdev.commerce.forms.submission.SubmissionMetadata;
import me.cxdev.commerce.forms.submission.SubmissionSnapshotService;

/** Stores a submission and schedules a commit-aware cluster event. @since 5.0.2 */
public class DefaultDynamicFormSubmissionService implements DynamicFormSubmissionService {
	private final ModelService modelService;
	private final UserService userService;
	private final BaseSiteService baseSiteService;
	private final CommonI18NService commonI18NService;
	private final EventService eventService;
	private final TransactionTemplate transactionTemplate;
	private final SubmissionSnapshotService snapshotService;
	public DefaultDynamicFormSubmissionService(final ModelService modelService, final UserService userService,
			final BaseSiteService baseSiteService, final CommonI18NService commonI18NService, final EventService eventService,
			final TransactionTemplate transactionTemplate, final SubmissionSnapshotService snapshotService) {
		this.modelService = modelService;
		this.userService = userService;
		this.baseSiteService = baseSiteService;
		this.commonI18NService = commonI18NService;
		this.eventService = eventService;
		this.transactionTemplate = transactionTemplate;
		this.snapshotService = snapshotService;
	}

	@Override
	public DynamicFormSubmissionModel create(final DynamicFormModel form, final Map<String, Object> answers,
			final List<DynamicFormFieldModel> fields, final SubmissionMetadata metadata) {
		final String json = snapshotService.toJson(answers);
		final String snapshot = snapshotService.snapshot(form, fields);
		return transactionTemplate.execute(status -> {
			final DynamicFormSubmissionModel submission = modelService.create(DynamicFormSubmissionModel.class);
			submission.setId(UUID.randomUUID().toString());
			submission.setForm(form);
			submission.setAnswersJson(json);
			submission.setDefinitionSnapshotJson(snapshot);
			submission.setSubmittedAt(new Date());
			submission.setUser(userService.getCurrentUser());
			submission.setBaseSite(baseSiteService.getCurrentBaseSite());
			submission.setLanguage(commonI18NService.getLocaleForLanguage(commonI18NService.getCurrentLanguage()).toLanguageTag());
			submission.setUserAgent(limit(metadata.userAgent(), 2048));
			submission.setRemoteAddress(limit(metadata.remoteAddress(), 64));
			modelService.save(submission);
			eventService.publishEvent(new DynamicFormSubmissionCreatedEvent(submission.getPk(), submission.getId(), form.getId(), getNodeId()));
			return submission;
		});
	}

	protected int getNodeId() {
		return Registry.getClusterID();
	}

	private String limit(final String value, final int length) {
		return value == null ? null : value.substring(0, Math.min(length, value.length()));
	}
}
