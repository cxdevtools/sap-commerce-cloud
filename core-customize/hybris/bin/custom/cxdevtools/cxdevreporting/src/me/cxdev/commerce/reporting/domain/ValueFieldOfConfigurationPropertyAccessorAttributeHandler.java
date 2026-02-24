package me.cxdev.commerce.reporting.domain;

import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.attribute.AbstractDynamicAttributeHandler;

import org.apache.commons.lang3.StringUtils;

import me.cxdev.commerce.reporting.model.ConfigurationPropertyAccessorModel;

public class ValueFieldOfConfigurationPropertyAccessorAttributeHandler extends AbstractDynamicAttributeHandler<String, ConfigurationPropertyAccessorModel> {
	private final ConfigurationService configurationService;

	public ValueFieldOfConfigurationPropertyAccessorAttributeHandler(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	@Override
	public String get(ConfigurationPropertyAccessorModel model) {
		String key = model.getKey();
		return (key == null) ? null : configurationService.getConfiguration().getString(key, "");
	}

	@Override
	public void set(ConfigurationPropertyAccessorModel model, String value) {
		String key = model.getKey();
		if (StringUtils.isNotBlank(key)) {
			configurationService.getConfiguration().setProperty(key, value);
		}
	}
}
