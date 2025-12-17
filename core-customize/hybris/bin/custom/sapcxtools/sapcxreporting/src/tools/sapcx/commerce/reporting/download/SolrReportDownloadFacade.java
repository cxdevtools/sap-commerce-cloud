package tools.sapcx.commerce.reporting.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import tools.sapcx.commerce.reporting.enums.ReportExportFormat;
import tools.sapcx.commerce.reporting.report.ReportService;
import tools.sapcx.commerce.reporting.report.data.QueryFileConfigurationData;
import tools.sapcx.commerce.reporting.search.AbstractGenericSearchFacade;
import tools.sapcx.commerce.reporting.search.GenericSearchResult;

public class SolrReportDownloadFacade implements ReportDownloadFacade {
	private final ReportService reportService;
	private final Map<String, AbstractGenericSearchFacade<?>> reportSearchFacades;

	public SolrReportDownloadFacade(ReportService reportService, Map<String, AbstractGenericSearchFacade<?>> reportSearchFacades) {
		this.reportService = reportService;
		this.reportSearchFacades = reportSearchFacades;
	}

	@Override
	public InputStream getReport(String title, String type, String query) {
		try {
			AbstractGenericSearchFacade<?> reportSearchFacade = reportSearchFacades.get(type);
			if (reportSearchFacade == null) {
				throw new IllegalArgumentException("Unknown report type '{}'! Please check configuration!");
			}

			GenericSearchResult searchResult = reportSearchFacade.search(query, Map.of());
			File reportFile = reportService.getReportFile(getQueryFileConfigurationData(title), searchResult)
					.orElseThrow(() -> new FileNotFoundException("Report service did not provide a file!"));
			return new SelfDeletingFileInputStream(reportFile);
		} catch (IllegalArgumentException | FileNotFoundException e) {
			throw new ExcelDocumentNotAvailableException(String.format("Excel export for query '%s' failed!", query), e);
		}
	}

	private QueryFileConfigurationData getQueryFileConfigurationData(String title) {
		QueryFileConfigurationData config = new QueryFileConfigurationData();
		config.setTitle(title);
		config.setCompress(false);
		config.setExportFormat(ReportExportFormat.EXCEL.getCode());
		config.setExcelAutosizeColumns(true);
		config.setExcelFreezeHeader(true);
		config.setExcelHighlightHeader(true);
		config.setExcelActivateFilter(true);
		config.setExcelAlternatingLines(false);
		return config;
	}
}
