package me.cxdev.commerce.toolkit.setup.importer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;

import org.apache.commons.lang3.StringUtils;

/**
 * In addition to the {@link PrefixBasedDataImporter}, this class adds a {@link SystemSetupParameter} for every key.
 * In that way, a system administrator can reimport the project data imports manually at any time to restore some data.
 *
 * @see PrefixBasedDataImporter
 */
public class ProjectDataImporter extends PrefixBasedDataImporter {
	private boolean importOnInitialization = false;
	private boolean allowManualImport = true;

	@Override
	public List<SystemSetupParameter> getSystemSetupParameters() {
		if (!allowManualImport) {
			return Collections.emptyList();
		}

		SystemSetupParameter parameter = new SystemSetupParameter(getPrefix());
		parameter.setLabel("Optional: " + getTitle());
		parameter.setMultiSelect(true);
		getEnvironment().getKeys(getPrefix()).stream()
				.map(this::shortenKey)
				.sorted()
				.forEach(key -> parameter.addValue(key, false));
		if (parameter.getValues() == null || parameter.getValues().size() == 0) {
			return Collections.emptyList();
		} else {
			return Collections.singletonList(parameter);
		}
	}

	@Override
	protected Predicate<String> getKeyFilter(SystemSetupContext context) {
		return key -> {
			if (isSystemInitialization(context) && importOnInitialization) {
				return true;
			}

			String[] parameters = context.getParameters(context.getExtensionName() + "_" + getPrefix());
			return parameters != null && Arrays.stream(parameters).anyMatch(shortenKey(key)::equalsIgnoreCase);
		};
	}

	private String shortenKey(String key) {
		return StringUtils.replaceOnce(key, getPrefix() + ".", "");
	}

	private boolean isSystemInitialization(SystemSetupContext context) {
		return context.getProcess().isInit() && !context.getProcess().isAll();
	}

	public void setImportOnInitialization(boolean importOnInitialization) {
		this.importOnInitialization = importOnInitialization;
	}

	public void setAllowManualImport(boolean allowManualImport) {
		this.allowManualImport = allowManualImport;
	}
}
