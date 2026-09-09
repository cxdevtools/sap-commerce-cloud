package me.cxdev.commerce.forms.facade.impl;

import java.util.List;

import de.hybris.platform.servicelayer.dto.converter.Converter;

import me.cxdev.commerce.forms.service.DynamicFormService;
import me.cxdev.commerce.forms.model.DynamicFormModel;
import me.cxdev.commerce.forms.data.DynamicFormData;
import me.cxdev.commerce.forms.facade.DynamicFormFacade;

/**
 * Default facade implementation for dynamic forms.
 */
public class DefaultDynamicFormFacade implements DynamicFormFacade {
	private final DynamicFormService dynamicFormService;
	private final Converter<DynamicFormModel, DynamicFormData> dynamicFormDataConverter;

	public DefaultDynamicFormFacade(
			final DynamicFormService dynamicFormService,
			final Converter<DynamicFormModel, DynamicFormData> dynamicFormDataConverter) {
		this.dynamicFormService = dynamicFormService;
		this.dynamicFormDataConverter = dynamicFormDataConverter;
	}

	@Override
	public List<DynamicFormData> getAllDynamicForms() {
		return dynamicFormDataConverter.convertAll(dynamicFormService.getAllDynamicForms());
	}

	@Override
	public DynamicFormData getDynamicFormForId(final String id) {
		return dynamicFormService.getDynamicFormForId(id)
				.map(dynamicFormDataConverter::convert)
				.orElseGet(DynamicFormData::new);
	}
}
