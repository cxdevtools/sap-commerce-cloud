package tools.sapcx.commerce.reporting.domain;

import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.attribute.AbstractDynamicAttributeHandler;

import org.apache.commons.lang3.StringUtils;

import tools.sapcx.commerce.reporting.model.ConfigurationPropertyAccessorModel;

public class ValueFieldOfConfigurationPropertyAccessorAttributeHandler extends AbstractDynamicAttributeHandler<String, ConfigurationPropertyAccessorModel> {
	private ConfigurationService configurationService;

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

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
