package tools.sapcx.commerce.toolkit.impex.executor;

import de.hybris.platform.core.initialization.SystemSetupContext;

import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface ImpExDataImportExecutor {
	void importImpexFile(SystemSetupContext context, String file, String fileEncoding);

	default void importImpexFile(SystemSetupContext context, String file) {
		importImpexFile(context, file, "UTF-8");
	}

	default ImpExDataImporterLogger getLogger() {
		return new ImpExDataImporterLogger(LoggerFactory.getLogger(getClass()));
	}
}
