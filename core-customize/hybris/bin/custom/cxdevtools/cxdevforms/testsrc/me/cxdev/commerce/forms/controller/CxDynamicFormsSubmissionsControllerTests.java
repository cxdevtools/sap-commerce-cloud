package me.cxdev.commerce.forms.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import me.cxdev.commerce.forms.facade.DynamicFormSubmissionFacade;
import me.cxdev.commerce.forms.submission.SubmissionReceipt;
import me.cxdev.commerce.forms.submission.SubmissionValidationException;

@UnitTest
class CxDynamicFormsSubmissionsControllerTests {
	private final DynamicFormSubmissionFacade facade = mock(DynamicFormSubmissionFacade.class);
	private MockMvc mvc;
	@BeforeEach
	void setup() {
		final var controller = new CxDynamicFormsSubmissionsController();
		final var messages = new ResourceBundleMessageSource();
		messages.setBasename("localization/cxdevforms-submissions");
		messages.setDefaultEncoding("UTF-8");
		final var i18n = mock(CommonI18NService.class);
		when(i18n.getLocaleForLanguage(any())).thenReturn(Locale.GERMAN);
		ReflectionTestUtils.setField(controller, "facade", facade);
		ReflectionTestUtils.setField(controller, "messageSource", messages);
		ReflectionTestUtils.setField(controller, "commonI18NService", i18n);
		mvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	void returnsCreatedAndDelegatesTypedAnswersAndRequestMetadata() throws Exception {
		when(facade.submit(eq("contact"), any(), any())).thenReturn(new SubmissionReceipt("uuid", "contact", new Date()));
		final var response = mvc.perform(post("/electronics/forms/contact/submissions").contentType("application/json")
				.header("User-Agent", "TestBrowser").content("{\"answers\":{\"consent\":true,\"choices\":[\"a\"]}}"))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(201);
		assertThat(new ObjectMapper().readTree(response.getContentAsString()).get("submissionId").asText()).isEqualTo("uuid");
		verify(facade).submit(eq("contact"), argThat(r -> Boolean.TRUE.equals(r.getAnswers().get("consent"))
				&& List.of("a").equals(r.getAnswers().get("choices"))), argThat(m -> "TestBrowser".equals(m.userAgent())));
	}

	@Test
	void localizesSpringFieldErrorsWith422() throws Exception {
		final var errors = new MapBindingResult(new HashMap<>(), "submission");
		errors.addError(new FieldError("submission", "answers[email]", null, false, new String[] { "cxdevforms.submission.email" }, null, null));
		when(facade.submit(any(), any(), any())).thenThrow(new SubmissionValidationException(errors));
		final var response = mvc.perform(post("/electronics/forms/contact/submissions").contentType("application/json").content("{\"answers\":{}}"))
				.andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(422);
		final var error = new ObjectMapper().readTree(response.getContentAsString()).get("errors").get(0);
		assertThat(error.get("message").asText()).isEqualTo("Bitte eine gültige E-Mail-Adresse eingeben.");
		assertThat(error.get("field").asText()).isEqualTo("answers[email]");
		assertThat(error.has("rejectedValue")).isFalse();
	}

	@Test
	void returns400ForMalformedJsonAnd404ForUnknownForm() throws Exception {
		assertThat(mvc.perform(post("/s/forms/f/submissions").contentType("application/json").content("{"))
				.andReturn().getResponse().getStatus()).isEqualTo(400);
		verifyNoInteractions(facade);
		when(facade.submit(any(), any(), any())).thenThrow(new UnknownIdentifierException("not found"));
		assertThat(mvc.perform(post("/s/forms/f/submissions").contentType("application/json").content("{\"answers\":{}}"))
				.andReturn().getResponse().getStatus()).isEqualTo(404);
	}
}
