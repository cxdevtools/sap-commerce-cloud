package me.cxdev.commerce.forms.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import me.cxdev.commerce.forms.event.DynamicFormSubmissionCreatedEvent;
import me.cxdev.commerce.forms.model.DynamicFormModel;
import me.cxdev.commerce.forms.model.DynamicFormSubmissionModel;
import me.cxdev.commerce.forms.submission.SubmissionMetadata;
import me.cxdev.commerce.forms.submission.SubmissionSnapshotService;

@UnitTest
class DefaultDynamicFormSubmissionServiceTests {
	@Test
	void capturesServerContextAndOnlyPublishesAfterSave() {
		final var models = mock(ModelService.class);
		final var users = mock(UserService.class);
		final var sites = mock(BaseSiteService.class);
		final var i18n = mock(CommonI18NService.class);
		final var events = mock(EventService.class);
		final var snapshots = mock(SubmissionSnapshotService.class);
		final var tx = mock(TransactionTemplate.class);
		when(tx.execute(any())).thenAnswer(call -> ((TransactionCallback<?>) call.getArgument(0)).doInTransaction(null));
		final var service = new DefaultDynamicFormSubmissionService(models, users, sites, i18n, events, tx, snapshots) {
			@Override
			protected int getNodeId() {
				return 7;
			}
		};
		final var form = mock(DynamicFormModel.class);
		when(form.getId()).thenReturn("contact");
		final var saved = mock(DynamicFormSubmissionModel.class);
		when(saved.getId()).thenReturn("uuid");
		when(models.create(DynamicFormSubmissionModel.class)).thenReturn(saved);
		final var user = mock(UserModel.class);
		final var site = mock(BaseSiteModel.class);
		when(users.getCurrentUser()).thenReturn(user);
		when(sites.getCurrentBaseSite()).thenReturn(site);
		when(i18n.getLocaleForLanguage(any())).thenReturn(Locale.GERMAN);
		when(snapshots.toJson(any())).thenReturn("{}");
		when(snapshots.snapshot(any(), any())).thenReturn("snapshot");
		assertThat(service.create(form, Map.of(), List.of(), new SubmissionMetadata("x".repeat(3000), "127.0.0.1"))).isSameAs(saved);
		verify(saved).setUser(user);
		verify(saved).setBaseSite(site);
		verify(saved).setLanguage("de");
		verify(saved).setUserAgent("x".repeat(2048));
		verify(saved).setDefinitionSnapshotJson("snapshot");
		final var event = ArgumentCaptor.forClass(DynamicFormSubmissionCreatedEvent.class);
		final var order = inOrder(models, events);
		order.verify(models).save(saved);
		order.verify(events).publishEvent(event.capture());
		assertThat(event.getValue().publishOnCommitOnly()).isTrue();
		assertThat(event.getValue().publish(7, 8)).isTrue();
		assertThat(event.getValue().getOriginNodeId()).isEqualTo(7);
		assertThat(event.getValue().getFormId()).isEqualTo("contact");
		clearInvocations(events);
		doThrow(new IllegalStateException("DB failed")).when(models).save(saved);
		assertThatThrownBy(() -> service.create(form, Map.of(), List.of(), new SubmissionMetadata(null, null))).isInstanceOf(IllegalStateException.class);
		verifyNoInteractions(events);
	}
}
