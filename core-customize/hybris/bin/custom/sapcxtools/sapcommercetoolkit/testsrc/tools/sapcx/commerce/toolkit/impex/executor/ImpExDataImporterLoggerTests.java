package tools.sapcx.commerce.toolkit.impex.executor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Writer;
import java.util.Collections;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.util.JspContext;

import org.apache.commons.io.output.StringBuilderWriter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockJspWriter;

@UnitTest
public class ImpExDataImporterLoggerTests {
	private SystemSetupContext context;
	private Writer jspOutWriter = new StringBuilderWriter();

	private ImpExDataImporterLogger logger;

	@Before
	public void setUp() throws Exception {
		jspOutWriter = new StringBuilderWriter();

		context = new SystemSetupContext(Collections.emptyMap(), SystemSetup.Type.ESSENTIAL, SystemSetup.Process.ALL, "extensionName");
		context.setJspContext(new JspContext(new MockJspWriter(jspOutWriter), null, null));

		logger = new ImpExDataImporterLogger();
	}

	@Test
	public void verifyStartLogsToJspContext() {
		logger.start(context, "test");
		assertThat(jspOutWriter.toString()).isEqualTo(coloredTextWithNewline("> Starting importing of: test", "green"));
	}

	@Test
	public void verifyDebugDoesNotLogToJspContext() {
		logger.debug(context, "debug text");
		assertThat(jspOutWriter.toString()).isEmpty();
	}

	@Test
	public void verifyDebugWithExceptionDoesNotLogToJspContext() {
		logger.debug(context, "debug text");
		assertThat(jspOutWriter.toString()).isEmpty();
	}

	@Test
	public void verifyDebugLogsToJspContextIfDebugIsEnabled() {
		logger.debug(context, "debug text");
		assertThat(jspOutWriter.toString()).isEqualTo(coloredTextWithNewline("debug text", "cyan"));
	}

	@Test
	public void verifyDebugWithExceptionLogsToJspContextIfDebugIsEnabled() {
		logger.debug(context, "debug text");
		assertThat(jspOutWriter.toString()).isEqualTo(coloredTextWithNewline("debug text", "cyan"));
	}

	@Test
	public void verifyStopLogsToJspContext() {
		logger.stop(context, "test");
		assertThat(jspOutWriter.toString()).isEqualTo(coloredTextWithNewline("> Import of test is done.", "green"));
	}

	@Test
	public void verifyInfoLogsToJspContext() {
		logger.info(context, "info text");
		assertThat(jspOutWriter.toString()).isEqualTo(coloredTextWithNewline("info text", "green"));
	}

	@Test
	public void verifyInfoWithExceptionLogsToJspContext() {
		logger.info(context, "info text", new RuntimeException("exception"));
		assertThat(jspOutWriter.toString()).isEqualTo(coloredTextWithNewline("info text", "green"));
	}

	@Test
	public void verifyErrorLogsToJspContext() {
		logger.error(context, "error text");
		assertThat(jspOutWriter.toString()).isEqualTo(coloredTextWithNewline("error text", "red"));
	}

	@Test
	public void verifyErrorWithExceptionLogsToJspContext() {
		logger.error(context, "error text", new RuntimeException("exception"));
		assertThat(jspOutWriter.toString()).isEqualTo(coloredTextWithNewline("error text", "red"));
	}

	private String coloredTextWithNewline(String text, String color) {
		return "<font color='" + color + "'>" + text + "</font><br/>\n";
	}
}
