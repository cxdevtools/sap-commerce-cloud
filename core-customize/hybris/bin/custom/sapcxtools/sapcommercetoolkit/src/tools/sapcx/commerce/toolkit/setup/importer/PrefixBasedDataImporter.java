package tools.sapcx.commerce.toolkit.setup.importer;

import java.util.function.Predicate;

import de.hybris.platform.core.initialization.SystemSetupContext;

import org.apache.commons.lang3.StringUtils;

import tools.sapcx.commerce.toolkit.impex.executor.ImpExDataImportExecutor;
import tools.sapcx.commerce.toolkit.setup.ImpExDataImporter;
import tools.sapcx.commerce.toolkit.setup.SystemSetupEnvironment;

/**
 * Implementation of the {@link ImpExDataImporter} that retrieves all configuration values from the environment that
 * match a given prefix parameter and imports the corresponding files in a alphanumeric order based upon the
 * configuration key.
 *
 * Example:
 *
 * <code>
 * sapcommercetoolkit.impeximport.sampledata.0100.categories=/path/to/file100.impex
 * sapcommercetoolkit.impeximport.sampledata.0200.classificationsystem=/path/to/file200.impex
 * sapcommercetoolkit.impeximport.sampledata.0150.products=/path/to/file150.impex
 * </code>
 *
 * In this example, an {@link PrefixBasedDataImporter} with prefix set to `sapcommercetoolkit,impeximport.sampledata`
 * imports the files in the following order: file100.impex, file150.impex and file200.impex
 *
 * The order is determined by the configuration keys and not by the loading order of the underlying properties files.
 */
public class PrefixBasedDataImporter implements ImpExDataImporter {
	private SystemSetupEnvironment environment;
	private ImpExDataImportExecutor impExDataImportExecutor;
	private String title;
	private String prefix;

	@Override
	public void importData(SystemSetupContext context) {
		getImpExDataImportExecutor().getLogger().start(context, getTitle());

		getEnvironment().getKeys(getPrefix()).stream()
				.sorted()
				.filter(getKeyFilter(context))
				.map(getEnvironment()::mapKeyToFile)
				.filter(StringUtils::isNotBlank)
				.forEach(file -> getImpExDataImportExecutor().importImpexFile(context, file));

		getImpExDataImportExecutor().getLogger().stop(context, getTitle());
	}

	protected Predicate<String> getKeyFilter(SystemSetupContext context) {
		return StringUtils::isNotBlank;
	}

	public void setEnvironment(SystemSetupEnvironment environment) {
		this.environment = environment;
	}

	public SystemSetupEnvironment getEnvironment() {
		return environment;
	}

	public void setImpExDataImportExecutor(ImpExDataImportExecutor impExDataImportExecutor) {
		this.impExDataImportExecutor = impExDataImportExecutor;
	}

	public ImpExDataImportExecutor getImpExDataImportExecutor() {
		return impExDataImportExecutor;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}
}
