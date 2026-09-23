package me.cxdev.commerce.forms.facade.populator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;

import me.cxdev.commerce.forms.data.DynamicFormFieldData;
import me.cxdev.commerce.forms.data.DynamicFormFieldValueData;
import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DynamicFormFieldValuePopulatorTest {
	@Mock
	private ObjectFactory<Converter<DynamicFormFieldModel, DynamicFormFieldData>> converterFactory;
	@Mock
	private Converter<DynamicFormFieldModel, DynamicFormFieldData> fieldConverter;
	@Mock
	private DynamicFormFieldValueModel source;
	@Mock
	private DynamicFormFieldModel childField;

	private DynamicFormFieldValuePopulator systemUnderTest;

	@BeforeEach
	void setUp() {
		systemUnderTest = new DynamicFormFieldValuePopulator(converterFactory);
	}

	@Test
	void shouldPopulateValueAndConvertChildFields() {
		final DynamicFormFieldData childFieldData = new DynamicFormFieldData();
		final DynamicFormFieldValueData target = new DynamicFormFieldValueData();

		when(source.getId()).thenReturn("1");
		when(source.getLabel()).thenReturn("Label");
		when(source.getChildFields()).thenReturn(List.of(childField));
		when(converterFactory.getObject()).thenReturn(fieldConverter);
		when(fieldConverter.convertAll(List.of(childField))).thenReturn(List.of(childFieldData));

		systemUnderTest.populate(source, target);

		assertEquals("1", target.getId());
		assertEquals("Label", target.getLabel());
		assertEquals(1, target.getChildFields().size());
		assertSame(childFieldData, target.getChildFields().get(0));
	}

	@Test
	void shouldKeepChildFieldsNullWhenSourceHasNone() {
		final DynamicFormFieldValueData target = new DynamicFormFieldValueData();

		systemUnderTest.populate(source, target);

		assertNull(target.getChildFields());
	}
}
