package me.cxdev.commerce.forms.interceptors;

import java.util.UUID;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;

/**
 * Assigns an immutable technical identifier when a value is created without one.
 */
public class DynamicFormFieldValuePrepareInterceptor implements PrepareInterceptor<DynamicFormFieldValueModel> {

	@Override
	public void onPrepare(final DynamicFormFieldValueModel value, final InterceptorContext context) {
		if (isBlank(value.getId())) {
			value.setId(UUID.randomUUID().toString());
		}
	}

	private boolean isBlank(final String value) {
		return value == null || value.trim().isEmpty();
	}
}
