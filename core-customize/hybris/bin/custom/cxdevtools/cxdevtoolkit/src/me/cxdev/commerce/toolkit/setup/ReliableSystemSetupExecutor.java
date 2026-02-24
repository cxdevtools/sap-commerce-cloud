package me.cxdev.commerce.toolkit.setup;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.validation.services.ValidationService;

import org.assertj.core.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Composed {@link SystemSetup} runner that combines several {@link ImpExDataImporter} that follow different semantics.
 *
 * Those are:
 * - elementary data: will be imported first and only once during system initialization
 * - release patch data: will be imported second during essential data processing
 * - essential data: will be imported third during essential data processing
 * - project data: will be imported last during project data processing
 *
 * Multiple project data importers can be defined and attached. Those data importers will be executed in the order
 * they are defined. Typically, there are multiple project data imports such as "initial data", "sample data", and
 * "test data".
 */
public final class ReliableSystemSetupExecutor implements ApplicationContextAware {
	static final Logger LOG = LoggerFactory.getLogger(SystemSetupEnvironment.class);

	@VisibleForTesting
	static final String COCKPIT_CONFIGURATION_SERVICE = "cockpitConfigurationService";

	private ApplicationContext applicationContext;
	private final ValidationService validationService;
	private final ImpExDataImporter elementaryDataImporter;
	private final ImpExDataImporter releasePatchesImporter;
	private final ImpExDataImporter essentialDataImporter;
	private final List<ImpExDataImporter> projectDataImporters;

	public ReliableSystemSetupExecutor(
			ValidationService validationService,
			ImpExDataImporter elementaryDataImporter,
			ImpExDataImporter releasePatchesImporter,
			ImpExDataImporter essentialDataImporter,
			List<ImpExDataImporter> projectDataImporters) {
		this.validationService = validationService;
		this.elementaryDataImporter = elementaryDataImporter;
		this.releasePatchesImporter = releasePatchesImporter;
		this.essentialDataImporter = essentialDataImporter;
		this.projectDataImporters = projectDataImporters;
	}

	public void reliableSetupPhases(final SystemSetupContext context) {
		Consumer<ImpExDataImporter> importData = importer -> importer.importData(context);

		if (context.getType().isEssential()) {
			if (isSystemInitialization(context)) {
				Optional.ofNullable(elementaryDataImporter).ifPresent(importData);
			}

			Optional.ofNullable(releasePatchesImporter).ifPresent(importData);
			Optional.ofNullable(essentialDataImporter).ifPresent(importData);

			LOG.info("Reloading validation engine...");
			validationService.reloadValidationEngine();

			LOG.info("Resetting backoffice configuration...");
			resetBackofficeConfiguration();
		}

		if (context.getType().isProject()) {
			projectDataImporters.forEach(importData);
		}
	}

	/**
	 * Performs the reset of the backoffice configuration only if backoffice application is loaded. This method uses
	 * reflections to determine the backoffice configuration service to avoid a dependency on the backoffice module.
	 */
	private void resetBackofficeConfiguration() {
		try {
			Object cockpitConfigurationService = applicationContext.getBean(COCKPIT_CONFIGURATION_SERVICE);
			cockpitConfigurationService.getClass().getMethod("resetToDefaults").invoke(cockpitConfigurationService);
		} catch (BeansException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			// No bean found, no action required: Backoffice application not loaded!
			LOG.info("No backoffice application found. Skipping backoffice configuration reset.");
		}
	}

	public List<SystemSetupParameter> getSystemSetupParameters() {
		final List<SystemSetupParameter> params = new ArrayList<>();
		for (ImpExDataImporter projectDataImporter : projectDataImporters) {
			params.addAll(projectDataImporter.getSystemSetupParameters());
		}
		return params;
	}

	private boolean isSystemInitialization(SystemSetupContext context) {
		return context.getProcess().isInit() && !context.getProcess().isAll();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
