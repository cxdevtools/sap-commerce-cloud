package tools.sapcx.commerce.reporting.report;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.sapcx.commerce.reporting.enums.ReportExportFormat;
import tools.sapcx.commerce.reporting.generator.ReportGenerator;
import tools.sapcx.commerce.reporting.model.QueryReportConfigurationModel;
import tools.sapcx.commerce.reporting.model.QueryReportConfigurationParameterModel;
import tools.sapcx.commerce.reporting.report.data.QueryFileConfigurationData;
import tools.sapcx.commerce.reporting.search.GenericSearchResult;

public class DefaultReportService implements ReportService {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultReportService.class);

	private Map<ReportExportFormat, ReportGenerator> generators;

	@Override
	public Optional<File> getReportFile(QueryFileConfigurationData report, GenericSearchResult result) {
		if (result.hasError()) {
			return Optional.empty();
		}

		ReportExportFormat exportFormat = ReportExportFormat.valueOf(report.getExportFormat());
		ReportGenerator reportGenerator = generators.get(exportFormat);
		if (reportGenerator == null) {
			LOG.error(String.format("No generator registered for export format: %s. " +
					"Make sure you register new export format with the following spring map: 'reportGeneratorMap'", exportFormat.getCode()));
			return Optional.empty();
		}

		String filename = String.format("%s.%s", UUID.randomUUID(), reportGenerator.getExtension());
		File file = getTemporaryReportFile(filename);
		try {
			if (!file.exists() && !file.createNewFile()) {
				return Optional.empty();
			}
		} catch (IOException e) {
			LOG.error(String.format("Could not create temporary file for report '%s' at: %s", report.getTitle(), file.getAbsolutePath()), e);
			return Optional.empty();
		}

		boolean reportWasGenerated = false;
		try {
			reportWasGenerated = reportGenerator.createReport(report, result, file);
		} catch (Exception e) {
			LOG.error(String.format("Could not generate report '%s'. Unexpected exception occurred!", report.getTitle()), e);
		}

		if (!reportWasGenerated) {
			try {
				Files.delete(file.toPath());
			} catch (IOException e) {
				LOG.warn("Error deleting temporary report file at: " + file.getAbsolutePath(), e);
			}
			file = null;
		}

		return Optional.ofNullable(file);
	}

	@VisibleForTesting
	protected File getTemporaryReportFile(String filename) {
		return new File(getReportDirectory(), filename);
	}

	@Override
	public Map<String, Object> getReportParameters(QueryReportConfigurationModel report) {
		HashMap<String, Object> params = new HashMap<>();
		for (QueryReportConfigurationParameterModel param : emptyIfNull(report.getParameters())) {
			if (CollectionUtils.isNotEmpty(param.getItemList())) {
				params.put(param.getName(), param.getItemList());
			} else if (param.getItem() != null) {
				params.put(param.getName(), param.getItem());
			}
		}
		return params;
	}

	public File getReportDirectory() {
		File reportDir = FileUtils.getTempDirectory()
				.toPath()
				.resolve("sapcxtools")
				.resolve("reports")
				.toFile();
		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}
		return reportDir;
	}

	@Override
	public List<ReportExportFormat> getConfiguredReportFormats() {
		return generators.keySet().stream().collect(Collectors.toList());
	}

	public void setGenerators(Map<ReportExportFormat, ReportGenerator> generators) {
		this.generators = generators;
	}
}
