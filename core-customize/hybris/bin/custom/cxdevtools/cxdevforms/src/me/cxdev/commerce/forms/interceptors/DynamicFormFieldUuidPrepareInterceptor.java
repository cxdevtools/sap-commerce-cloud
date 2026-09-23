package me.cxdev.commerce.forms.interceptors;

import java.util.UUID;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import me.cxdev.commerce.forms.model.DynamicFormFieldModel;

/** Assigns an immutable technical identifier when a field is created without one. */
public class DynamicFormFieldUuidPrepareInterceptor implements PrepareInterceptor<DynamicFormFieldModel> {

	@Override
	public void onPrepare(final DynamicFormFieldModel field, final InterceptorContext context) {
		if (isBlank(field.getId())) {
			field.setId(UUID.randomUUID().toString());
		}
	}

	private boolean isBlank(final String value) {
		return value == null || value.trim().isEmpty();
	}
}
