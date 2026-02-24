package me.cxdev.commerce.reporting.generator.csv;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.cxdev.commerce.reporting.generator.ReportGenerator;
import me.cxdev.commerce.reporting.report.data.QueryFileConfigurationData;
import me.cxdev.commerce.reporting.search.GenericSearchResult;
import me.cxdev.commerce.reporting.search.GenericSearchResultHeader;

public class CsvReportGenerator implements ReportGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(CsvReportGenerator.class);
	private static final String CSV_EXTENSION = "csv";

	@Override
	public boolean createReport(QueryFileConfigurationData report, GenericSearchResult result, File file) {
		try (CsvReportWriter csvWriter = new CsvReportWriter(file, report)) {
			List<GenericSearchResultHeader> headers = result.getHeaders();
			addRow(csvWriter, result.getHeaderNames());

			List<Map<GenericSearchResultHeader, String>> rowValues = result.getValues();
			List<String> values = new ArrayList<>(result.getResultColumns());

			int numberOfRows = result.getResultRows();
			for (int rowIndex = 0; rowIndex < numberOfRows; rowIndex++) {
				Map<GenericSearchResultHeader, String> rowValue = rowValues.get(rowIndex);
				for (GenericSearchResultHeader header : headers) {
					values.add(rowValue.get(header));
				}
				addRow(csvWriter, values);
				values.clear();
			}

			return true;
		} catch (IOException e) {
			LOG.error(String.format("Could not write CSV to file: %s", file.getAbsolutePath()), e);
			return false;
		}
	}

	private void addRow(CsvReportWriter csvWriter, Collection<String> values) throws IOException {
		Map<Integer, String> csvLine = new LinkedHashMap<>();
		Iterator<String> valueIterator = values.iterator();
		for (int columnIndex = 0; valueIterator.hasNext(); columnIndex++) {
			csvLine.put(columnIndex, valueIterator.next());
		}
		csvWriter.write(csvLine);
	}

	@Override
	public String getExtension() {
		return CSV_EXTENSION;
	}
}
