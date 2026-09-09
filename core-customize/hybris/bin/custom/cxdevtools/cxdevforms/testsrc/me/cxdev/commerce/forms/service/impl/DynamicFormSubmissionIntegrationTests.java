package me.cxdev.commerce.forms.service.impl;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.SiteChannel;
import de.hybris.platform.servicelayer.ServicelayerTest;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.site.BaseSiteService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.Resource;
import me.cxdev.commerce.forms.enums.DynamicFormFieldType;
import me.cxdev.commerce.forms.enums.DynamicFormType;
import me.cxdev.commerce.forms.event.DynamicFormSubmissionCreatedEvent;
import me.cxdev.commerce.forms.facade.DynamicFormSubmissionFacade;
import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormModel;
import me.cxdev.commerce.forms.model.DynamicFormSubmissionModel;
import me.cxdev.commerce.forms.submission.DynamicFormSubmissionRequest;
import me.cxdev.commerce.forms.submission.SubmissionMetadata;

/** Real database and commit/rollback checks; requires an initialized test tenant. */
@IntegrationTest
public class DynamicFormSubmissionIntegrationTests extends ServicelayerTest {
	@Resource
	private ModelService modelService;
	@Resource
	private FlexibleSearchService flexibleSearchService;
	@Resource
	private CommonI18NService commonI18NService;
	@Resource
	private BaseSiteService baseSiteService;
	@Resource
	private EventService eventService;
	@Resource(name = "cxDynamicFormSubmissionFacade")
	private DynamicFormSubmissionFacade facade;
	@Resource(name = "cxDynamicFormSubmissionTransactionTemplate")
	private TransactionTemplate transactionTemplate;
	private DynamicFormModel form;
	private DynamicFormFieldModel field;
	private BaseSiteModel site;
	private final BlockingQueue<DynamicFormSubmissionCreatedEvent> received = new LinkedBlockingQueue<>();
	private final ApplicationListener<DynamicFormSubmissionCreatedEvent> listener = new ApplicationListener<>() {
		@Override
		public void onApplicationEvent(final DynamicFormSubmissionCreatedEvent event) {
			received.add(event);
		}
	};

	@Before
	public void prepareForm() {
		commonI18NService.setCurrentLanguage(commonI18NService.getLanguage("en"));
		site = modelService.create(BaseSiteModel.class);
		site.setUid("submission-test-" + UUID.randomUUID());
		site.setChannel(SiteChannel.B2C);
		modelService.save(site);
		baseSiteService.setCurrentBaseSite(site, false);
		form = modelService.create(DynamicFormModel.class);
		form.setId("submission-test-" + UUID.randomUUID());
		form.setType(DynamicFormType.valueOf("SUBMISSION_TEST"));
		form.setTitle("Integration form");
		modelService.save(form);
		field = modelService.create(DynamicFormFieldModel.class);
		field.setId("message-" + UUID.randomUUID());
		field.setFieldType(DynamicFormFieldType.TEXT);
		field.setLabel("Message");
		field.setRequired(true);
		field.setActive(true);
		form.setFormFields(List.of(field));
		modelService.saveAll(form, field);
		eventService.registerEventListener(listener);
	}

	@After
	public void cleanUp() {
		eventService.unregisterEventListener(listener);
		if (form != null) {
			modelService.removeAll(submissions());
			if (field != null) {
				modelService.remove(field);
			}
			modelService.remove(form);
		}
		if (site != null) {
			modelService.remove(site);
		}
	}

	@Test
	public void savesToDatabaseAndPublishesCommittedReference() throws Exception {
		final var request = new DynamicFormSubmissionRequest();
		request.setAnswers(Map.of(field.getId(), "Persist this answer"));
		final var receipt = facade.submit(form.getId(), request, new SubmissionMetadata("IntegrationBrowser", "127.0.0.1"));
		final var event = received.poll(5, TimeUnit.SECONDS);
		assertThat(event).isNotNull();
		assertThat(event.getSubmissionId()).isEqualTo(receipt.submissionId());
		modelService.detachAll();
		final DynamicFormSubmissionModel reloaded = modelService.get(event.getSubmissionPk());
		assertThat(reloaded.getAnswersJson()).contains(field.getId(), "Persist this answer");
		assertThat(reloaded.getDefinitionSnapshotJson()).contains("Integration form");
		assertThat(reloaded.getUser()).isNotNull();
		assertThat(reloaded.getSubmittedAt()).isNotNull();
		assertThat(reloaded.getBaseSite().getUid()).isEqualTo(site.getUid());
		assertThat(reloaded.getUserAgent()).isEqualTo("IntegrationBrowser");
	}

	@Test
	public void rollbackLeavesNeitherSubmissionNorPublishedEvent() throws Exception {
		final var request = new DynamicFormSubmissionRequest();
		request.setAnswers(Map.of(field.getId(), "Persist this answer"));
		transactionTemplate.execute(status -> {
			facade.submit(form.getId(), request, new SubmissionMetadata(null, null));
			status.setRollbackOnly();
			return null;
		});
		assertThat(submissions()).isEmpty();
		assertThat(received.poll(500, TimeUnit.MILLISECONDS)).isNull();
	}

	private List<DynamicFormSubmissionModel> submissions() {
		return flexibleSearchService.<DynamicFormSubmissionModel>search(new FlexibleSearchQuery(
				"SELECT {pk} FROM {DynamicFormSubmission} WHERE {form}=?form", Map.of("form", form))).getResult();
	}
}
