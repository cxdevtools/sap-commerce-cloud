package tools.sapcx.commerce.reporting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static tools.sapcx.commerce.toolkit.testing.itemmodel.InMemoryModelFactory.createTestableItemModel;

import de.hybris.bootstrap.annotations.UnitTest;

import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;

import tools.sapcx.commerce.reporting.model.ConfigurationPropertyAccessorModel;
import tools.sapcx.commerce.toolkit.testing.testdoubles.config.ConfigurationServiceFake;

@UnitTest
public class ValueFieldOfConfigurationPropertyAccessorAttributeHandlerTests {
	private ConfigurationServiceFake configurationService;
	private ValueFieldOfConfigurationPropertyAccessorAttributeHandler handler;

	@Before
	public void setUp() throws Exception {
		configurationService = new ConfigurationServiceFake();
		configurationService.setProperty("sample.key1", "custom value 1");
		configurationService.setProperty("sample.key2", "custom value 2");

		handler = new ValueFieldOfConfigurationPropertyAccessorAttributeHandler(configurationService);
	}

	@Test
	public void whenNoConfigurationKeyIsProvided_verifyThatNoValueIsReturned() {
		ConfigurationPropertyAccessorModel configurationItem = createTestableItemModel(ConfigurationPropertyAccessorModel.class);

		String value = handler.get(configurationItem);

		assertThat(value).isNull();
	}

	@Test
	public void whenUnknownConfigurationKeyIsProvided_verifyThatEmptyStringIsReturned() {
		ConfigurationPropertyAccessorModel configurationItem = createTestableItemModel(ConfigurationPropertyAccessorModel.class);
		configurationItem.setKey("sample.unknown_key");

		String value = handler.get(configurationItem);

		assertThat(value).isEmpty();
	}

	@Test
	public void whenKnownConfigurationKeyIsProvided_verifyThatConfigurationValueIsRead() {
		ConfigurationPropertyAccessorModel configurationItem1 = createTestableItemModel(ConfigurationPropertyAccessorModel.class);
		configurationItem1.setKey("sample.key1");
		ConfigurationPropertyAccessorModel configurationItem2 = createTestableItemModel(ConfigurationPropertyAccessorModel.class);
		configurationItem2.setKey("sample.key2");

		String value1 = handler.get(configurationItem1);
		String value2 = handler.get(configurationItem2);

		assertThat(value1).isEqualTo("custom value 1");
		assertThat(value2).isEqualTo("custom value 2");
	}

	@Test
	public void whenNoConfigurationKeyIsProvided_verifyThatSetterIgnoresValue() {
		ConfigurationPropertyAccessorModel configurationItem = createTestableItemModel(ConfigurationPropertyAccessorModel.class);

		handler.set(configurationItem, "override");

		Configuration configuration = configurationService.getConfiguration();
		assertThat(configuration.getKeys()).toIterable().hasSize(2);
	}

	@Test
	public void whenUnknownConfigurationKeyIsProvided_verifyThatConfigurationIsAdded() {
		ConfigurationPropertyAccessorModel configurationItem = createTestableItemModel(ConfigurationPropertyAccessorModel.class);
		configurationItem.setKey("sample.unknown_key");

		handler.set(configurationItem, "override");

		Configuration configuration = configurationService.getConfiguration();
		assertThat(configuration.containsKey("sample.unknown_key")).isTrue();
		assertThat(configuration.getString("sample.unknown_key")).isEqualTo("override");
	}

	@Test
	public void whenKnownConfigurationKeyIsProvided_verifyThatConfigurationIsUpdated() {
		ConfigurationPropertyAccessorModel configurationItem1 = createTestableItemModel(ConfigurationPropertyAccessorModel.class);
		configurationItem1.setKey("sample.key1");

		handler.set(configurationItem1, "override");

		Configuration configuration = configurationService.getConfiguration();
		assertThat(configuration.containsKey("sample.key1")).isTrue();
		assertThat(configuration.getString("sample.key1")).isEqualTo("override");
	}
}
