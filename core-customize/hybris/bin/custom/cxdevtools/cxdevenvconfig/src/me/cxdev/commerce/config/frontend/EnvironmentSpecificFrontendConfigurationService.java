package me.cxdev.commerce.config.frontend;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.hybris.platform.servicelayer.config.ConfigurationService;

import pl.jalokim.propertiestojson.util.PropertiesToJsonConverterBuilder;

public class EnvironmentSpecificFrontendConfigurationService implements FrontendConfigurationService {
	static final String ENVIRONMENT_ID_CONFIGURATION_KEY = "cxdevenvconfig.environment.id";
	static final String ENVIRONMENT_NAME_CONFIGURATION_KEY = "cxdevenvconfig.environment.name";
	static final String FRONTEND_CONFIGURATION_PREFIX = "cxdevenvconfig.frontend";

	private ConfigurationService configurationService;
	private int generatedHashCode = -1;
	private String generatedProperties = null;

	public EnvironmentSpecificFrontendConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	@Override
	public String getEnvironmentId() {
		return configurationService.getConfiguration().getString(ENVIRONMENT_ID_CONFIGURATION_KEY);
	}

	@Override
	public String getEnvironmentName() {
		return configurationService.getConfiguration().getString(ENVIRONMENT_NAME_CONFIGURATION_KEY);
	}

	@Override
	public String getFrontendConfiguration() {
		List<String> keys = loadFrontendKeys();
		int hashCode = keys.hashCode();
		if (generatedProperties == null || generatedHashCode != hashCode) {
			generatedHashCode = hashCode;
			generatedProperties = PropertiesToJsonConverterBuilder.builder()
					.charset(Charset.forName("UTF-8"))
					.defaultAndCustomObjectToJsonTypeConverters()
					.defaultAndCustomTextToObjectResolvers()
					.skipNulls()
					.build()
					.convertToJson(loadProperties(keys));
		}

		return generatedProperties;
	}

	private List<String> loadFrontendKeys() {
		List<String> keys = new ArrayList<>();
		configurationService.getConfiguration()
				.getKeys(FRONTEND_CONFIGURATION_PREFIX)
				.forEachRemaining(keys::add);
		return keys;
	}

	private Properties loadProperties(List<String> keys) {
		Properties properties = new Properties();
		keys.forEach(key -> {
			String frontendKey = key.substring(FRONTEND_CONFIGURATION_PREFIX.length() + 1);
			String frontendValue = configurationService.getConfiguration().getString(key, "");
			properties.setProperty(frontendKey, frontendValue);
		});
		return properties;
	}
}
