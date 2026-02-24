package me.cxdev.commerce.toolkit.setup;

import java.util.Collections;
import java.util.List;

import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;

/**
 * Generic interface to describe an {code}ImpExDataImporter{code} that participates in the process
 * of system setup. The {@link #importData(SystemSetupContext)} method will be triggered by the
 * {@link ToolkitSystemSetup}.
 *
 * By default, a {code}ImpExDataImporter{code} performs its operations silently during system
 * initialization or system update. By implementing the {@link #getSystemSetupParameters()} method
 * an implementing class can contribute to the optional list of parameters during system update.
 * These parameters will then be visible on the system update page and can be configured by the
 * administrator.
 */
@FunctionalInterface
public interface ImpExDataImporter {
	default List<SystemSetupParameter> getSystemSetupParameters() {
		return Collections.emptyList();
	}

	void importData(SystemSetupContext context);
}
