package tools.sapcx.commerce.toolkit.impex.executor;

import de.hybris.platform.core.initialization.SystemSetupContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This logger facade provides simplified access to the logging stack and guarantees that log messages are shown both
 * in the log files of the system as well as in the JSP context of the triggered system setup process.
 */
public final class ImpExDataImporterLogger {
	static final Logger LOG = LoggerFactory.getLogger(ImpExDataImporterLogger.class);

	private final Logger logger;

	public ImpExDataImporterLogger() {
		this(LOG);
	}

	public ImpExDataImporterLogger(Logger logger) {
		this.logger = logger;
	}

	public void debug(SystemSetupContext context, String message) {
		debug(context, message, null);
	}

	public void debug(SystemSetupContext context, String message, Exception e) {
		logger.debug(message, e);
		if (logger.isDebugEnabled()) {
			printToJsp(context, message, "cyan");
		}
	}

	public void info(SystemSetupContext context, String message) {
		info(context, message, null);
	}

	public void info(SystemSetupContext context, String message, Exception e) {
		logger.info(message, e);
		printToJsp(context, message, "green");
	}

	public void error(SystemSetupContext context, String message) {
		error(context, message, null);
	}

	public void error(SystemSetupContext context, String message, Exception e) {
		logger.error(message, e);
		printToJsp(context, message, "red");
	}

	private void printToJsp(SystemSetupContext context, String message, String color) {
		if (context.getJspContext() != null) {
			context.getJspContext().println("<font color='" + color + "'>" + message + "</font>");
		}
	}

	public void start(SystemSetupContext context, String dataId) {
		info(context, "> Starting importing of: " + dataId);
	}

	public void stop(SystemSetupContext context, String dataId) {
		info(context, "> Import of " + dataId + " is done.");
	}
}
