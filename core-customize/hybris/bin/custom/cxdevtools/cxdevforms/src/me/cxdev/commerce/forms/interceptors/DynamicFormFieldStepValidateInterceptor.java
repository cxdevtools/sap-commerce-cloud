package me.cxdev.commerce.forms.interceptors;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Objects;

import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;
import me.cxdev.commerce.forms.model.DynamicFormStepModel;

/** Ensures that a field can only reference a step owned by its own form. */
public class DynamicFormFieldStepValidateInterceptor implements ValidateInterceptor<DynamicFormFieldModel> {

    @Override
    public void onValidate(final DynamicFormFieldModel field, final InterceptorContext context)
            throws InterceptorException {
        final DynamicFormStepModel step = field.getStep();
        if (step != null && field.getForm() != null && !Objects.equals(field.getForm(), step.getForm())) {
            throw new InterceptorException("The selected form step must belong to the field's form.");
        }
        final DynamicFormFieldValueModel parentValue = field.getParentFieldValue();
        if (parentValue != null && parentValue.getField() != null && field.getForm() != null
                && !Objects.equals(field.getForm(), parentValue.getField().getForm())) {
            throw new InterceptorException("The parent form-field value must belong to the field's form.");
        }
    }
}
