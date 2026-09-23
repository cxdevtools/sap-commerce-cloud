package me.cxdev.commerce.forms.interceptors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;

@UnitTest
class DynamicFormFieldUuidPrepareInterceptorTest {

	private final InterceptorContext context = mock(InterceptorContext.class);

	@Test
	void shouldAssignUuidToNewField() {
		final DynamicFormFieldModel field = mock(DynamicFormFieldModel.class);
		when(field.getId()).thenReturn(null);

		new DynamicFormFieldUuidPrepareInterceptor().onPrepare(field, context);

		final ArgumentCaptor<String> id = ArgumentCaptor.forClass(String.class);
		verify(field).setId(id.capture());
		assertDoesNotThrow(() -> UUID.fromString(id.getValue()));
	}

	@Test
	void shouldAssignUuidToNewValue() {
		final DynamicFormFieldValueModel value = mock(DynamicFormFieldValueModel.class);
		when(value.getId()).thenReturn(" ");

		new DynamicFormFieldValuePrepareInterceptor().onPrepare(value, context);

		final ArgumentCaptor<String> id = ArgumentCaptor.forClass(String.class);
		verify(value).setId(id.capture());
		assertDoesNotThrow(() -> UUID.fromString(id.getValue()));
	}
}
