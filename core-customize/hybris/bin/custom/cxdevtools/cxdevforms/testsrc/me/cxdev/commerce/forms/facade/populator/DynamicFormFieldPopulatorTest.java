package me.cxdev.commerce.forms.facade.populator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;

import me.cxdev.commerce.forms.facade.populator.DynamicFormFieldPopulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import me.cxdev.commerce.forms.enums.DynamicFormFieldType;
import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;
import me.cxdev.commerce.forms.model.DynamicFormStepModel;
import me.cxdev.commerce.forms.data.DynamicFormFieldData;
import me.cxdev.commerce.forms.data.DynamicFormFieldValueData;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DynamicFormFieldPopulatorTest {
	@Mock
	private ObjectFactory<Converter<DynamicFormFieldValueModel, DynamicFormFieldValueData>> converterFactory;
	@Mock
	private Converter<DynamicFormFieldValueModel, DynamicFormFieldValueData> valueConverter;
	@Mock
	private DynamicFormFieldModel source;
	@Mock
	private DynamicFormFieldValueModel valueModel;
	@Mock
	private DynamicFormFieldType fieldType;
	@Mock
	private DynamicFormStepModel step;

	private DynamicFormFieldPopulator systemUnderTest;

	@BeforeEach
	void setUp() {
		systemUnderTest = new DynamicFormFieldPopulator(converterFactory);
	}

	@Test
	void shouldPopulateFieldAndConvertValues() {
		final DynamicFormFieldValueData convertedValue = new DynamicFormFieldValueData();
		final DynamicFormFieldData target = new DynamicFormFieldData();

		when(source.getId()).thenReturn("machine");
		when(source.getLabel()).thenReturn("Machine");
		when(source.getDescription()).thenReturn("Description");
		when(source.isHidden()).thenReturn(false);
		when(source.isRequired()).thenReturn(Boolean.TRUE);
		when(source.getFieldType()).thenReturn(fieldType);
		when(fieldType.getCode()).thenReturn("TEXT");
		when(source.getDefaultValue()).thenReturn("default");
		when(source.getPlaceholder()).thenReturn("placeholder");
		when(source.getStep()).thenReturn(step);
		when(step.getId()).thenReturn("step-uuid");
		when(step.getLabel()).thenReturn("Contact details");
		when(source.getMinValue()).thenReturn(1.0);
		when(source.getMaxValue()).thenReturn(10.0);
		when(source.getMinLength()).thenReturn(2);
		when(source.getMaxLength()).thenReturn(20);
		when(source.getFormFieldValues()).thenReturn(List.of(valueModel));
		when(converterFactory.getObject()).thenReturn(valueConverter);
		when(valueConverter.convertAll(List.of(valueModel))).thenReturn(List.of(convertedValue));

		systemUnderTest.populate(source, target);

		assertEquals("machine", target.getId());
		assertEquals("TEXT", target.getFieldType());
		assertEquals(Boolean.TRUE, target.getRequired());
		assertEquals("step-uuid", target.getStepId());
		assertEquals("Contact details", target.getStepTitle());
		assertEquals(1, target.getFormFieldValues().size());
		assertSame(convertedValue, target.getFormFieldValues().get(0));
	}

	@Test
	void shouldKeepFormFieldValuesNullWhenSourceHasNone() {
		final DynamicFormFieldData target = new DynamicFormFieldData();

		systemUnderTest.populate(source, target);

		assertNull(target.getFormFieldValues());
	}
}
