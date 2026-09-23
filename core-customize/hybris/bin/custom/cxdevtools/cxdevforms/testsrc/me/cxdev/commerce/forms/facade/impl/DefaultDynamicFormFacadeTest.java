package me.cxdev.commerce.forms.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.cxdev.commerce.forms.data.DynamicFormData;
import me.cxdev.commerce.forms.model.DynamicFormModel;
import me.cxdev.commerce.forms.service.DynamicFormService;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultDynamicFormFacadeTest {
	@Mock
	private DynamicFormService dynamicFormService;
	@Mock
	private Converter<DynamicFormModel, DynamicFormData> dynamicFormDataConverter;
	@Mock
	private DynamicFormModel dynamicFormModel;

	private DefaultDynamicFormFacade systemUnderTest;

	@BeforeEach
	void setUp() {
		systemUnderTest = new DefaultDynamicFormFacade(dynamicFormService, dynamicFormDataConverter);
	}

	@Test
	void shouldReturnConvertedDynamicForms() {
		final DynamicFormData converted = new DynamicFormData();
		when(dynamicFormService.getAllDynamicForms()).thenReturn(List.of(dynamicFormModel));
		when(dynamicFormDataConverter.convertAll(List.of(dynamicFormModel))).thenReturn(List.of(converted));

		final List<DynamicFormData> result = systemUnderTest.getAllDynamicForms();

		assertEquals(1, result.size());
		assertSame(converted, result.get(0));
		verify(dynamicFormService).getAllDynamicForms();
	}

	@Test
	void shouldReturnEmptyDynamicFormWhenNoModelExists() {
		when(dynamicFormService.getDynamicFormForId("missing")).thenReturn(Optional.empty());

		final DynamicFormData result = systemUnderTest.getDynamicFormForId("missing");

		assertNull(result.getId());
	}

	@Test
	void shouldConvertDynamicFormWhenModelExists() {
		final DynamicFormData expected = new DynamicFormData();
		when(dynamicFormService.getDynamicFormForId("form-1")).thenReturn(Optional.of(dynamicFormModel));
		when(dynamicFormDataConverter.convert(dynamicFormModel)).thenReturn(expected);

		final DynamicFormData result = systemUnderTest.getDynamicFormForId("form-1");

		assertSame(expected, result);
	}
}
