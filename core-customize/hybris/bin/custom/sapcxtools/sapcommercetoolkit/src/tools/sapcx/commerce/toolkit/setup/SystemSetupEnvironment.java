package tools.sapcx.commerce.toolkit.setup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.impex.ImportConfig;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.assertj.core.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This facade handles the access to important properties for the system setup process. It also keeps track of imported
 * files and verifies the release numbers of the files accordingly.
 */
public final class SystemSetupEnvironment {
	static final Logger LOG = LoggerFactory.getLogger(SystemSetupEnvironment.class);
	@VisibleForTesting
	static final String LEGACYMODEKEY = "sapcommercetoolkit.impeximport.configuration.legacymode";
	@VisibleForTesting
	static final String ENABLECODEEXECUTIONKEY = "sapcommercetoolkit.impeximport.configuration.enablecodeexecution";
	@VisibleForTesting
	static final String VALIDATIONMODEKEY = "sapcommercetoolkit.impeximport.configuration.validationmode";
	@VisibleForTesting
	static final String DEFAULTLOCALEKEY = "sapcommercetoolkit.impeximport.configuration.defaultlocale";
	@VisibleForTesting
	static final String ISDEVELOPMENTKEY = "sapcommercetoolkit.impeximport.environment.isdevelopment";
	@VisibleForTesting
	static final String SUPPORTLOCALIZATIONKEY = "sapcommercetoolkit.impeximport.environment.supportlocalizedfiles";
	@VisibleForTesting
	static final String LASTPROCESSEDRELEASEVERSIONKEY = "sapcommercetoolkit.impeximport.configuration.lastprocessedreleaseversion";
	@VisibleForTesting
	static final String LASTPROCESSEDRELEASEITEMSKEY = "sapcommercetoolkit.impeximport.configuration.lastprocessedreleaseitems";
	@VisibleForTesting
	static final String FILE_HEADER = "This file is generated automatically by the sapcommercetoolkit extension. Do not change the file manually!";

	private String fileName;
	private FileBasedConfigurationBuilder<PropertiesConfiguration> persistentConfiguration;
	private ConfigurationService configurationService;

	public boolean useLegacyModeForImpEx() {
		return configurationService.getConfiguration().getBoolean(LEGACYMODEKEY, false);
	}

	public boolean enableCodeExecution() {
		return configurationService.getConfiguration().getBoolean(ENABLECODEEXECUTIONKEY, true);
	}

	public ImportConfig.ValidationMode getValidationMode() {
		String validationModeCode = configurationService.getConfiguration().getString(VALIDATIONMODEKEY, "strict");
		if ("relaxed".equalsIgnoreCase(validationModeCode)) {
			return ImportConfig.ValidationMode.RELAXED;
		} else {
			return ImportConfig.ValidationMode.STRICT;
		}
	}

	public Locale getDefaultLocaleForImpEx() {
		return getLocaleFromConfig(DEFAULTLOCALEKEY, Locale.ENGLISH);
	}

	public boolean isDevelopment() {
		return configurationService.getConfiguration().getBoolean(ISDEVELOPMENTKEY, false);
	}

	public boolean supportLocalizedImpExFiles() {
		return configurationService.getConfiguration().getBoolean(SUPPORTLOCALIZATIONKEY, false);
	}

	public void addProcessedItem(String version, String key) {
		setLastProcessedReleaseVersion(version);
		getPersistentConfiguration().addProperty(LASTPROCESSEDRELEASEITEMSKEY, key);
	}

	private PropertiesConfiguration getPersistentConfiguration() {
		try {
			if (persistentConfiguration == null) {
				throw new ConfigurationException("No persistent configuration found!");
			}
			return persistentConfiguration.getConfiguration();
		} catch (ConfigurationException e) {
			LOG.error(
					"Persistent configuration could not be loaded, any modification will be lost! Please make sure you have configured the file path correctly. Current value is '{}'",
					fileName, e);
			return new PropertiesConfiguration();
		}
	}

	public void setLastProcessedReleaseVersion(String version) {
		String lastVersion = getLastProcessedReleaseVersion();
		if (version.compareToIgnoreCase(lastVersion) > 0) {
			getPersistentConfiguration().clearProperty(LASTPROCESSEDRELEASEITEMSKEY);
			getPersistentConfiguration().setProperty(LASTPROCESSEDRELEASEVERSIONKEY, version);
		}
	}

	public String getLastProcessedReleaseVersion() {
		return getPersistentConfiguration().getString(LASTPROCESSEDRELEASEVERSIONKEY, "");
	}

	public List<String> getLastProcessedItems() {
		ArrayList<String> keys = new ArrayList<>();
		getPersistentConfiguration().getList(LASTPROCESSEDRELEASEITEMSKEY).stream()
				.map(String.class::cast)
				.forEach(keys::add);
		return keys;
	}

	public List<String> getKeys(String prefix) {
		ArrayList<String> keys = new ArrayList<>();
		configurationService.getConfiguration().getKeys(prefix).forEachRemaining(keys::add);
		return keys;
	}

	public String mapKeyToFile(String key) {
		return configurationService.getConfiguration().getString(key, "");
	}

	private Locale getLocaleFromConfig(String key, Locale defaultValue) {
		String locale = configurationService.getConfiguration().getString(key);

		if (locale == null) {
			return defaultValue;
		}

		int countrySeparator = locale.indexOf('_');
		int variantSeparator = locale.indexOf('-');
		if (countrySeparator != -1 && variantSeparator != -1) {
			String language = locale.substring(0, countrySeparator);
			String country = locale.substring(countrySeparator + 1, variantSeparator);
			String variant = locale.substring(variantSeparator + 1);
			return new Locale(language, country, variant);
		}

		if (countrySeparator != -1) {
			String language = locale.substring(0, countrySeparator);
			String country = locale.substring(countrySeparator + 1);
			return new Locale(language, country);
		}

		return new Locale(locale);
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public void setConfigurationFile(String fileName) {
		this.fileName = fileName;

		try {
			File file = new File(fileName);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				if (!file.createNewFile()) {
					throw new IOException("Cannot create file at: " + fileName);
				}
			}

			persistentConfiguration = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
			persistentConfiguration.setParameters(new Parameters()
					.fileBased()
					.setEncoding("UTF-8")
					.setFile(file)
					.getParameters());
			persistentConfiguration.setAutoSave(true);
			persistentConfiguration.getConfiguration().setHeader(FILE_HEADER);
		} catch (IOException | ConfigurationException e) {
			LOG.error("Cannot read or create persistent configuration file at: " + fileName + ". Please create the file manually and restart the server.", e);
			persistentConfiguration = null;
		}
	}
}
