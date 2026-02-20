package tools.sapcx.commerce.reporting.report;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import tools.sapcx.commerce.reporting.enums.ReportExportFormat;
import tools.sapcx.commerce.reporting.model.QueryReportConfigurationModel;
import tools.sapcx.commerce.reporting.report.data.QueryFileConfigurationData;
import tools.sapcx.commerce.reporting.search.GenericSearchResult;

/**
 * Service to turn a generic search result into medias.
 */
public interface ReportService {
	/**
	 * Creates a file from a generic search result and returns it as an {@link Optional} of {@link File}
	 *
	 * @param report    the report to be generated
	 * @param result    the search result to turn into a csv file
	 * @return the generated report file, empty if no report was generated
	 */
	Optional<File> getReportFile(QueryFileConfigurationData report, GenericSearchResult result);

	/**
	 * Gets a map of the configured parameters with its name as key and the item or item list as value
	 *
	 * @param report to get parameter config from
	 * @return the report parameters
	 */
	Map<String, Object> getReportParameters(QueryReportConfigurationModel report);

	/**
	 * Gets a map of the configured values with its name as key and the value
	 *
	 * @param report to get configuration from
	 * @return the report configuration
	 */
	Map<String, Object> getReportConfiguration(QueryReportConfigurationModel report);

	/**
	 * Return the directory to be used for report generation.
	 *
	 * @return the report directory
	 */
	File getReportDirectory();

	/**
	 * Get all configured report Formats
	 *
	 * @return List with report export formats
	 */
	List<ReportExportFormat> getConfiguredReportFormats();
}
