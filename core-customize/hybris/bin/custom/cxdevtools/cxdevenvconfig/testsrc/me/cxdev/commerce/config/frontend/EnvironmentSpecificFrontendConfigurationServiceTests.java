package me.cxdev.commerce.config.frontend;

import static me.cxdev.commerce.config.frontend.EnvironmentSpecificFrontendConfigurationService.ENVIRONMENT_ID_CONFIGURATION_KEY;
import static me.cxdev.commerce.config.frontend.EnvironmentSpecificFrontendConfigurationService.ENVIRONMENT_NAME_CONFIGURATION_KEY;
import static me.cxdev.commerce.config.frontend.EnvironmentSpecificFrontendConfigurationService.FRONTEND_CONFIGURATION_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.Test;
import org.springframework.util.StringUtils;

@UnitTest
public class EnvironmentSpecificFrontendConfigurationServiceTests {
	private ConfigurationServiceFake configurationService;
	private EnvironmentSpecificFrontendConfigurationService service;

	@Test
	public void withNoPropertiesSet_returnEmptyJsonObject() {
		setupService(Map.of("cxdevenvconfig.unknown.key", "value"));

		// @formatter:off
        String frontendConfiguration = service.getFrontendConfiguration();
        assertThat(StringUtils.trimAllWhitespace(frontendConfiguration)).isEqualToIgnoringWhitespace(
            "{" +
            "}"
        );
		// @formatter:on
	}

	@Test
	public void withOnePropertySet_returnSingleJsonObject() {
		setupService(Map.of(FRONTEND_CONFIGURATION_PREFIX + ".property", "value"));

		// @formatter:off
        String frontendConfiguration = service.getFrontendConfiguration();
        assertThat(StringUtils.trimAllWhitespace(frontendConfiguration)).isEqualToIgnoringWhitespace(
            "{" +
                "\"property\":\"value\"" +
            "}"
        );
        // @formatter:on
	}

	@Test
	public void withPropertyTreeSet_returnJsonStructure() {
		setupService(Map.of(
				FRONTEND_CONFIGURATION_PREFIX + ".level1a.level2a.key1", "value-1a-2a",
				FRONTEND_CONFIGURATION_PREFIX + ".level1a.level2b.key2", "value-1a-2b",
				FRONTEND_CONFIGURATION_PREFIX + ".level1b.level2a.key3", "value-1b-2a",
				FRONTEND_CONFIGURATION_PREFIX + ".level1b.level2b.key4", "value-1b-2b"));

		// @formatter:off
        // Note: The order of the keys looks arbitrary, but it is deterministic
        String frontendConfiguration = service.getFrontendConfiguration();
        assertThat(StringUtils.trimAllWhitespace(frontendConfiguration)).isEqualToIgnoringWhitespace(
            "{" +
                "\"level1b\":{" +
                    "\"level2a\":{" +
                        "\"key3\":\"value-1b-2a\"" +
                    "}," +
                    "\"level2b\":{" +
                        "\"key4\":\"value-1b-2b\"" +
                    "}" +
                "}," +
                "\"level1a\":{" +
                    "\"level2b\":{" +
                        "\"key2\":\"value-1a-2b\"" +
                    "}," +
                    "\"level2a\":{" +
                        "\"key1\":\"value-1a-2a\"" +
                    "}" +
                "}" +
            "}"
        );
		// @formatter:on
	}

	@Test
	public void withPropertyTreeSet_shouldCacheResult() {
		setupService(Map.of(
				FRONTEND_CONFIGURATION_PREFIX + ".level1a.level2a.key1", "value-1a-2a",
				FRONTEND_CONFIGURATION_PREFIX + ".level1a.level2b.key2", "value-1a-2b",
				FRONTEND_CONFIGURATION_PREFIX + ".level1b.level2a.key3", "value-1b-2a",
				FRONTEND_CONFIGURATION_PREFIX + ".level1b.level2b.key4", "value-1b-2b"));

		long start1 = System.currentTimeMillis();
		String frontendConfiguration1 = service.getFrontendConfiguration();
		long end1 = System.currentTimeMillis();

		long start2 = System.currentTimeMillis();
		String frontendConfiguration2 = service.getFrontendConfiguration();
		long end2 = System.currentTimeMillis();

		assertThat(frontendConfiguration1).isEqualTo(frontendConfiguration2);
		assertThat(end1 - start1).isGreaterThanOrEqualTo(end2 - start2);
	}

	@Test
	public void withEnvironmentConfiguration_shouldReturnEnvironmentMetadata() {
		setupService(Map.of(
				ENVIRONMENT_ID_CONFIGURATION_KEY, "ID",
				ENVIRONMENT_NAME_CONFIGURATION_KEY, "NAME"));
		assertThat(service.getEnvironmentId()).isEqualTo("ID");
		assertThat(service.getEnvironmentName()).isEqualTo("NAME");
	}

	private void setupService(Map<String, String> properties) {
		configurationService = new ConfigurationServiceFake();
		properties.forEach(configurationService::setProperty);
		service = new EnvironmentSpecificFrontendConfigurationService(configurationService);
	}
}
