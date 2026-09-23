package me.cxdev.commerce.forms.controller;

import java.util.List;
import java.util.Locale;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import me.cxdev.commerce.forms.facade.DynamicFormSubmissionFacade;
import me.cxdev.commerce.forms.submission.DynamicFormSubmissionRequest;
import me.cxdev.commerce.forms.submission.SubmissionMetadata;
import me.cxdev.commerce.forms.submission.SubmissionReceipt;
import me.cxdev.commerce.forms.submission.SubmissionValidationException;

/** OCC creation endpoint; validation is delegated to the facade. @since 5.0.2 */
@RestController
@RequestMapping("/{baseSiteId}/forms/{id}/submissions")
@ApiVersion("v2")
@Tag(name = "Dynamic Forms Submissions")
public class CxDynamicFormsSubmissionsController {
	@Resource(name = "cxDynamicFormSubmissionFacade")
	private DynamicFormSubmissionFacade facade;
	@Resource(name = "cxDynamicFormSubmissionMessageSource")
	private MessageSource messageSource;
	@Resource(name = "commonI18NService")
	private CommonI18NService commonI18NService;

	@PostMapping(consumes = "application/json", produces = "application/json")
	@Operation(operationId = "createDynamicFormSubmission", summary = "Validate and store a form submission")
	public ResponseEntity<SubmissionReceipt> submit(@PathVariable("id") final String id,
			@RequestBody final DynamicFormSubmissionRequest body, final HttpServletRequest request) {
		final var receipt = facade.submit(id, body, new SubmissionMetadata(request.getHeader("User-Agent"), request.getRemoteAddr()));
		return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
	}

	@ExceptionHandler(SubmissionValidationException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public SubmissionErrors validation(final SubmissionValidationException exception) {
		final Locale locale = locale();
		return new SubmissionErrors(exception.getErrors().getAllErrors().stream()
				.map(error -> new SubmissionError(error.getCode(), error instanceof FieldError field ? field.getField() : null,
						messageSource.getMessage(error, locale)))
				.toList());
	}

	@ExceptionHandler(UnknownIdentifierException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public SubmissionErrors notFound() {
		return error("cxdevforms.submission.notFound");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public SubmissionErrors malformed() {
		return error("cxdevforms.submission.malformed");
	}

	private SubmissionErrors error(final String code) {
		return new SubmissionErrors(List.of(new SubmissionError(code, null, messageSource.getMessage(code, null, locale()))));
	}

	private Locale locale() {
		return commonI18NService.getLocaleForLanguage(commonI18NService.getCurrentLanguage());
	}
	public record SubmissionError(String code, String field, String message) {
	}
	public record SubmissionErrors(List<SubmissionError> errors) {
	}
}
