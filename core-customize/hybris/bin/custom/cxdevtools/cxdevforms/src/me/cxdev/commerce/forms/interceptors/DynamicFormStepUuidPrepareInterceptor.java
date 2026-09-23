package me.cxdev.commerce.forms.interceptors;

import java.util.UUID;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import me.cxdev.commerce.forms.model.DynamicFormStepModel;

/** Assigns an immutable technical identifier when a form step is created without one. */
public class DynamicFormStepUuidPrepareInterceptor implements PrepareInterceptor<DynamicFormStepModel> {

	@Override
	public void onPrepare(final DynamicFormStepModel step, final InterceptorContext context) {
		if (step.getId() == null || step.getId().isBlank()) {
			step.setId(UUID.randomUUID().toString());
		}
	}
}
