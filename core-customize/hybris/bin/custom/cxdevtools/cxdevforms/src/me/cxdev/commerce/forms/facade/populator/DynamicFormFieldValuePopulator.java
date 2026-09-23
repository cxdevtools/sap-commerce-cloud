package me.cxdev.commerce.forms.facade.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.ObjectFactory;

import me.cxdev.commerce.forms.data.DynamicFormFieldData;
import me.cxdev.commerce.forms.data.DynamicFormFieldValueData;
import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;

/**
 * Populates {@link DynamicFormFieldValueData} from {@link DynamicFormFieldValueModel}.
 */
public class DynamicFormFieldValuePopulator
		implements Populator<DynamicFormFieldValueModel, DynamicFormFieldValueData> {

	private final ObjectFactory<Converter<DynamicFormFieldModel, DynamicFormFieldData>> dynamicFormFieldConverterFactory;

	public DynamicFormFieldValuePopulator(
			final ObjectFactory<Converter<DynamicFormFieldModel, DynamicFormFieldData>> dynamicFormFieldConverterFactory) {
		this.dynamicFormFieldConverterFactory = dynamicFormFieldConverterFactory;
	}

	/**
	 * Copies value metadata including conditional child fields.
	 *
	 * @param source source model
	 * @param target target data object
	 * @throws ConversionException if conversion fails
	 */
	@Override
	public void populate(final DynamicFormFieldValueModel source,
			final DynamicFormFieldValueData target) throws ConversionException {
		target.setId(source.getId());
		target.setLabel(source.getLabel());
		if (CollectionUtils.isNotEmpty(source.getChildFields())) {
			target.setChildFields(dynamicFormFieldConverterFactory.getObject().convertAll(source.getChildFields()));
		}
	}
}
