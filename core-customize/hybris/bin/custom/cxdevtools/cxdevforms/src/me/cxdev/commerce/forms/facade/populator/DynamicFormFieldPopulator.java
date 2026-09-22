package me.cxdev.commerce.forms.facade.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.ObjectFactory;

import me.cxdev.commerce.forms.data.DynamicFormFieldData;
import me.cxdev.commerce.forms.data.DynamicFormFieldValueData;
import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;
import me.cxdev.commerce.forms.model.DynamicFormStepModel;

/**
 * Populates {@link DynamicFormFieldData} from {@link DynamicFormFieldModel}.
 */
public class DynamicFormFieldPopulator implements Populator<DynamicFormFieldModel, DynamicFormFieldData> {
	private final ObjectFactory<Converter<DynamicFormFieldValueModel, DynamicFormFieldValueData>> dynamicFormFieldValueConverterFactory;

	public DynamicFormFieldPopulator(
			final ObjectFactory<Converter<DynamicFormFieldValueModel, DynamicFormFieldValueData>> dynamicFormFieldValueConverterFactory) {
		this.dynamicFormFieldValueConverterFactory = dynamicFormFieldValueConverterFactory;
	}

	/**
	 * Copies all relevant field attributes including selectable values.
	 *
	 * @param source source model
	 * @param target target data object
	 * @throws ConversionException if conversion fails
	 */
	@Override
	public void populate(final DynamicFormFieldModel source, final DynamicFormFieldData target)
			throws ConversionException {
		target.setId(source.getId());
		target.setLabel(source.getLabel());
		target.setDescription(source.getDescription());
		target.setHidden(source.isHidden());
		target.setRequired(BooleanUtils.isTrue(source.isRequired()));
		if (source.getFieldType() != null) {
			target.setFieldType(source.getFieldType().getCode());
		}
		target.setDefaultValue(source.getDefaultValue());
		target.setPlaceholder(source.getPlaceholder());
		final DynamicFormStepModel step = source.getStep();
		target.setStepId(step == null ? null : step.getId());
		target.setStepTitle(step == null ? null : step.getLabel());
		target.setMinValue(source.getMinValue());
		target.setMaxValue(source.getMaxValue());
		target.setMinLength(source.getMinLength());
		target.setMaxLength(source.getMaxLength());
		if (CollectionUtils.isNotEmpty(source.getFormFieldValues())) {
			target.setFormFieldValues(
					dynamicFormFieldValueConverterFactory.getObject().convertAll(source.getFormFieldValues()));
		}
	}
}
