package tools.sapcx.commerce.toolkit.setup;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Locale;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.impex.ImportConfig;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tools.sapcx.commerce.toolkit.testing.testdoubles.config.ConfigurationServiceFake;

@UnitTest
public class SystemSetupEnvironmentTests {
	private ConfigurationServiceFake configurationServiceFake;
	private SystemSetupEnvironment environment;
	private File tempFile;

	@Before
	public void setUp() throws Exception {
		configurationServiceFake = new ConfigurationServiceFake();
		tempFile = File.createTempFile("configuration", ".properties");
		environment = new SystemSetupEnvironment(tempFile.getAbsolutePath(), configurationServiceFake);
	}

	@After
	public void removeTempFile() throws Exception {
		tempFile.delete();
	}

	@Test
	public void withoutConfiguration_defaultValuesAreReturned() {
		assertThat(environment.useLegacyModeForImpEx()).isFalse();
		assertThat(environment.enableCodeExecution()).isTrue();
		assertThat(environment.getValidationMode()).isEqualTo(ImportConfig.ValidationMode.STRICT);
		assertThat(environment.getDefaultLocaleForImpEx()).isEqualTo(Locale.ENGLISH);
		assertThat(environment.isDevelopment()).isFalse();
		assertThat(environment.supportLocalizedImpExFiles()).isFalse();
	}

	@Test
	public void withConfigurationSetToTrue_configuredValuesAreReturned() {
		configurationServiceFake.setProperty(SystemSetupEnvironment.LEGACYMODEKEY, true);
		configurationServiceFake.setProperty(SystemSetupEnvironment.ENABLECODEEXECUTIONKEY, true);
		configurationServiceFake.setProperty(SystemSetupEnvironment.VALIDATIONMODEKEY, "strict");
		configurationServiceFake.setProperty(SystemSetupEnvironment.ISDEVELOPMENTKEY, true);
		configurationServiceFake.setProperty(SystemSetupEnvironment.SUPPORTLOCALIZATIONKEY, true);
		assertThat(environment.useLegacyModeForImpEx()).isTrue();
		assertThat(environment.enableCodeExecution()).isTrue();
		assertThat(environment.getValidationMode()).isEqualTo(ImportConfig.ValidationMode.STRICT);
		assertThat(environment.isDevelopment()).isTrue();
		assertThat(environment.supportLocalizedImpExFiles()).isTrue();
	}

	@Test
	public void withConfigurationSetToFalse_configuredValuesAreReturned() {
		configurationServiceFake.setProperty(SystemSetupEnvironment.LEGACYMODEKEY, false);
		configurationServiceFake.setProperty(SystemSetupEnvironment.ENABLECODEEXECUTIONKEY, false);
		configurationServiceFake.setProperty(SystemSetupEnvironment.VALIDATIONMODEKEY, "relaxed");
		configurationServiceFake.setProperty(SystemSetupEnvironment.ISDEVELOPMENTKEY, false);
		configurationServiceFake.setProperty(SystemSetupEnvironment.SUPPORTLOCALIZATIONKEY, false);
		assertThat(environment.useLegacyModeForImpEx()).isFalse();
		assertThat(environment.enableCodeExecution()).isFalse();
		assertThat(environment.getValidationMode()).isEqualTo(ImportConfig.ValidationMode.RELAXED);
		assertThat(environment.isDevelopment()).isFalse();
		assertThat(environment.supportLocalizedImpExFiles()).isFalse();
	}

	@Test
	public void withLanguageOnly_localeIsResolvedCorrectly() {
		configurationServiceFake.setProperty(SystemSetupEnvironment.DEFAULTLOCALEKEY, "de");
		assertThat(environment.getDefaultLocaleForImpEx()).isEqualTo(Locale.GERMAN);
	}

	@Test
	public void withLanguageAndCountry_localeIsResolvedCorrectly() {
		configurationServiceFake.setProperty(SystemSetupEnvironment.DEFAULTLOCALEKEY, "de_DE");
		assertThat(environment.getDefaultLocaleForImpEx()).isEqualTo(Locale.GERMANY);
	}

	@Test
	public void withLanguageAndCountryAndVariant_localeIsResolvedCorrectly() {
		configurationServiceFake.setProperty(SystemSetupEnvironment.DEFAULTLOCALEKEY, "de_DE-HESSEN");
		Locale defaultLocale = environment.getDefaultLocaleForImpEx();
		assertThat(defaultLocale.getLanguage()).isEqualTo("de");
		assertThat(defaultLocale.getCountry()).isEqualTo("DE");
		assertThat(defaultLocale.getVariant()).isEqualTo("HESSEN");
	}

	@Test
	public void withConfigurationForPrefix_allValuesAreReturned() {
		configurationServiceFake.setProperty("namoperation.impeximport.essentialdata.0001", "file1.txt");
		configurationServiceFake.setProperty("namoperation.impeximport.essentialdata.0002", "file2.txt");
		configurationServiceFake.setProperty("namoperation.impeximport.essentialdata.0003", "file3.txt");

		assertThat(environment.getKeys("namoperation.impeximport.essentialdata")).containsExactlyInAnyOrder(
				"namoperation.impeximport.essentialdata.0001",
				"namoperation.impeximport.essentialdata.0002",
				"namoperation.impeximport.essentialdata.0003");
	}

	@Test
	public void withConfigurationForPrefix_mapToKeyIsResolved() {
		configurationServiceFake.setProperty("namoperation.impeximport.essentialdata.0001", "file1.txt");
		configurationServiceFake.setProperty("namoperation.impeximport.essentialdata.0002", "file2.txt");
		configurationServiceFake.setProperty("namoperation.impeximport.essentialdata.0003", "file3.txt");

		assertThat(environment.mapKeyToFile("namoperation.impeximport.essentialdata.0001")).isEqualTo("file1.txt");
		assertThat(environment.mapKeyToFile("namoperation.impeximport.essentialdata.0002")).isEqualTo("file2.txt");
		assertThat(environment.mapKeyToFile("namoperation.impeximport.essentialdata.0003")).isEqualTo("file3.txt");
	}

	@Test
	public void verifyPersistentConfigurationFileIsCreatedIfAbsent() throws Exception {
		tempFile.delete();
		environment = new SystemSetupEnvironment(tempFile.getAbsolutePath(), configurationServiceFake);

		assertThat(tempFile).exists().canRead().canWrite();
		assertThat(tempFile).hasContent("# " + environment.FILE_HEADER + "\n\n");
	}

	@Test
	public void verifyPersistentConfigurationFileIsReadIfAvailable() throws Exception {
		FileUtils.writeStringToFile(tempFile, "# " + SystemSetupEnvironment.FILE_HEADER + "\n\n");
		FileUtils.writeStringToFile(tempFile, SystemSetupEnvironment.LASTPROCESSEDRELEASEVERSIONKEY + " = release1x0x0\n");

		assertThat(environment.getLastProcessedReleaseVersion()).isEqualTo("release1x0x0");
	}
}
