package me.cxdev.commerce.forms.interceptors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;

import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;
import me.cxdev.commerce.forms.model.DynamicFormModel;
import me.cxdev.commerce.forms.model.DynamicFormStepModel;

@UnitTest
class DynamicFormFieldStepValidateInterceptorTest {
    private final DynamicFormFieldStepValidateInterceptor interceptor = new DynamicFormFieldStepValidateInterceptor();

    @Test
    void shouldAcceptStepFromTheFieldsForm() {
        final DynamicFormModel form = mock(DynamicFormModel.class);
        final DynamicFormFieldModel field = mock(DynamicFormFieldModel.class);
        final DynamicFormStepModel step = mock(DynamicFormStepModel.class);
        when(field.getForm()).thenReturn(form);
        when(field.getStep()).thenReturn(step);
        when(step.getForm()).thenReturn(form);

        assertDoesNotThrow(() -> interceptor.onValidate(field, null));
    }

    @Test
    void shouldRejectStepFromAnotherForm() {
        final DynamicFormFieldModel field = mock(DynamicFormFieldModel.class);
        final DynamicFormStepModel step = mock(DynamicFormStepModel.class);
        when(field.getForm()).thenReturn(mock(DynamicFormModel.class));
        when(field.getStep()).thenReturn(step);
        when(step.getForm()).thenReturn(mock(DynamicFormModel.class));

        assertThrows(InterceptorException.class, () -> interceptor.onValidate(field, null));
    }

    @Test
    void shouldRejectParentValueFromAnotherForm() {
        final DynamicFormFieldModel field = mock(DynamicFormFieldModel.class);
        final DynamicFormFieldValueModel parentValue = mock(DynamicFormFieldValueModel.class);
        final DynamicFormFieldModel parentField = mock(DynamicFormFieldModel.class);
        when(field.getForm()).thenReturn(mock(DynamicFormModel.class));
        when(field.getParentFieldValue()).thenReturn(parentValue);
        when(parentValue.getField()).thenReturn(parentField);
        when(parentField.getForm()).thenReturn(mock(DynamicFormModel.class));

        assertThrows(InterceptorException.class, () -> interceptor.onValidate(field, null));
    }
}
