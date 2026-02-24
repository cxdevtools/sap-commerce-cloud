package me.cxdev.commerce.toolkit.setup;

import java.util.List;

import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;

import me.cxdev.commerce.toolkit.constants.ToolkitConstants;

/**
 * System setup class for the toolkit extension.
 *
 * @deprecated since 4.1.3 use {@link ReliableSystemSetupExecutor} instead
 */
@SystemSetup(extension = ToolkitConstants.EXTENSIONNAME)
public class ToolkitSystemSetup {
	private ReliableSystemSetupExecutor reliableSystemSetupExecutor;
	private boolean performSystemSetup = true;

	public ToolkitSystemSetup(ReliableSystemSetupExecutor reliableSystemSetupExecutor, boolean performSystemSetup) {
		this.reliableSystemSetupExecutor = reliableSystemSetupExecutor;
		this.performSystemSetup = performSystemSetup;
	}

	@SystemSetup(process = SystemSetup.Process.ALL, type = SystemSetup.Type.ALL)
	public void reliableSetupPhases(final SystemSetupContext context) {
		if (performSystemSetup) {
			reliableSystemSetupExecutor.reliableSetupPhases(context);
		}
	}

	@SystemSetupParameterMethod
	public List<SystemSetupParameter> getSystemSetupParameters() {
		if (performSystemSetup) {
			return reliableSystemSetupExecutor.getSystemSetupParameters();
		} else {
			return List.of();
		}
	}
}
