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

import me.cxdev.commerce.forms.data.DynamicFormData;
import me.cxdev.commerce.forms.data.DynamicFormFieldData;
import me.cxdev.commerce.forms.enums.DynamicFormType;
import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormModel;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DynamicFormPopulatorTest {
	@Mock
	private Converter<DynamicFormFieldModel, DynamicFormFieldData> dynamicFormFieldConverter;
	@Mock
	private DynamicFormModel source;
	@Mock
	private DynamicFormType type;
	@Mock
	private DynamicFormFieldModel activeField;
	@Mock
	private DynamicFormFieldModel inactiveField;

	private DynamicFormPopulator systemUnderTest;

	@BeforeEach
	void setUp() {
		systemUnderTest = new DynamicFormPopulator(dynamicFormFieldConverter);
	}

	@Test
	void shouldPopulateOnlyActiveFormFields() {
		final DynamicFormFieldData convertedField = new DynamicFormFieldData();
		final DynamicFormData target = new DynamicFormData();

		when(source.getId()).thenReturn("service");
		when(source.getTitle()).thenReturn("Service");
		when(source.getType()).thenReturn(type);
		when(type.getCode()).thenReturn("TECHNICAL_SUPPORT");
		when(source.getDescription()).thenReturn("Description");
		when(source.getDynamicRecipient()).thenReturn(Boolean.TRUE);
		when(source.getRecipients()).thenReturn(List.of("team@example.com"));
		when(source.getFormFields()).thenReturn(List.of(activeField, inactiveField));
		when(activeField.isActive()).thenReturn(true);
		when(inactiveField.isActive()).thenReturn(false);
		when(dynamicFormFieldConverter.convertAll(List.of(activeField))).thenReturn(List.of(convertedField));

		systemUnderTest.populate(source, target);

		assertEquals("service", target.getId());
		assertEquals("TECHNICAL_SUPPORT", target.getType());
		assertEquals(1, target.getFormFields().size());
		assertSame(convertedField, target.getFormFields().get(0));
	}

	@Test
	void shouldHandleOptionalTypeAndLists() {
		final DynamicFormData target = new DynamicFormData();
		systemUnderTest.populate(source, target);
		assertNull(target.getType());
		assertNull(target.getRecipients());
	}
}
