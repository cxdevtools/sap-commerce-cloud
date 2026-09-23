package me.cxdev.commerce.forms.facade.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.jupiter.api.Test;

import me.cxdev.commerce.forms.model.DynamicFormModel;
import me.cxdev.commerce.forms.model.DynamicFormSubmissionModel;
import me.cxdev.commerce.forms.service.DynamicFormService;
import me.cxdev.commerce.forms.service.DynamicFormSubmissionService;
import me.cxdev.commerce.forms.submission.DynamicFormSubmissionRequest;
import me.cxdev.commerce.forms.submission.DynamicFormSubmissionValidator;
import me.cxdev.commerce.forms.submission.RejectingSubmissionFileValidator;
import me.cxdev.commerce.forms.submission.SubmissionMetadata;
import me.cxdev.commerce.forms.submission.SubmissionValidationException;

@UnitTest
class DefaultDynamicFormSubmissionFacadeTests {
	@Test
	void invalidAnswersNeverReachPersistence() {
		final var forms = mock(DynamicFormService.class);
		final var submissions = mock(DynamicFormSubmissionService.class);
		final var form = mock(DynamicFormModel.class);
		when(form.getFormFields()).thenReturn(List.of());
		when(forms.getDynamicFormForId("contact")).thenReturn(Optional.of(form));
		final var facade = new DefaultDynamicFormSubmissionFacade(forms, submissions, new DynamicFormSubmissionValidator(new RejectingSubmissionFileValidator()));
		final var request = new DynamicFormSubmissionRequest();
		request.setAnswers(Map.of("injected", "value"));
		assertThatThrownBy(() -> facade.submit("contact", request, new SubmissionMetadata(null, null))).isInstanceOf(SubmissionValidationException.class);
		verifyNoInteractions(submissions);
		request.setAnswers(Map.of());
		final var saved = mock(DynamicFormSubmissionModel.class);
		when(saved.getId()).thenReturn("uuid");
		when(submissions.create(eq(form), any(), any(), any())).thenReturn(saved);
		assertThat(facade.submit("contact", request, new SubmissionMetadata(null, null)).submissionId()).isEqualTo("uuid");
	}
}
