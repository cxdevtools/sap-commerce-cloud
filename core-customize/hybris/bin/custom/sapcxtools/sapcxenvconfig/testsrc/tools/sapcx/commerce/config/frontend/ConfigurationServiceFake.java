package tools.sapcx.commerce.config.frontend;

import de.hybris.platform.servicelayer.config.ConfigurationService;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

public class ConfigurationServiceFake implements ConfigurationService {
	private BaseConfiguration configuration = new BaseConfiguration();

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	public void setProperty(String key, Object value) {
		configuration.setProperty(key, value);
	}

	public void clearProperty(String key) {
		configuration.clearProperty(key);
	}
}
