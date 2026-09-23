package me.cxdev.commerce.forms.facade.populator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import me.cxdev.commerce.forms.data.DynamicFormData;
import me.cxdev.commerce.forms.data.DynamicFormFieldData;
import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormModel;

/**
 * Populates {@link DynamicFormData} from {@link DynamicFormModel}.
 */
public class DynamicFormPopulator implements Populator<DynamicFormModel, DynamicFormData> {

	private final Converter<DynamicFormFieldModel, DynamicFormFieldData> dynamicFormFieldConverter;

	public DynamicFormPopulator(final Converter<DynamicFormFieldModel, DynamicFormFieldData> dynamicFormFieldConverter) {
		this.dynamicFormFieldConverter = dynamicFormFieldConverter;
	}

	/**
	 * Copies all form metadata and only active form fields.
	 *
	 * @param source source model
	 * @param target target data object
	 */
	@Override
	public void populate(final DynamicFormModel source, final DynamicFormData target) {
		target.setId(source.getId());
		target.setTitle(source.getTitle());
		if (source.getType() != null) {
			target.setType(source.getType().getCode());
		}
		target.setDescription(source.getDescription());
		target.setDynamicRecipient(BooleanUtils.isTrue(source.getDynamicRecipient()));
		if (CollectionUtils.isNotEmpty(source.getRecipients())) {
			target.setRecipients(new ArrayList<>(source.getRecipients()));
		}
		if (CollectionUtils.isNotEmpty(source.getFormFields())) {
			final List<DynamicFormFieldModel> activeFieldModels = source.getFormFields().stream()
					.filter(DynamicFormFieldModel::isActive).collect(Collectors.toList());
			target.setFormFields(dynamicFormFieldConverter.convertAll(activeFieldModels));
		}
	}
}
