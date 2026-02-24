package me.cxdev.commerce.reporting.generator.csv;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import me.cxdev.commerce.reporting.report.data.QueryFileConfigurationData;
import me.cxdev.commerce.reporting.search.GenericSearchResult;
import me.cxdev.commerce.reporting.search.GenericSearchResultHeader;

@UnitTest
public class CsvReportGeneratorTests {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private File tempReportFile;
	private QueryFileConfigurationData queryReportConfiguration;
	private List<GenericSearchResultHeader> headers = new ArrayList<>();
	private List<Map<GenericSearchResultHeader, String>> values = new ArrayList<>();

	private CsvReportGenerator generator;

	@Before
	public void setUp() throws Exception {
		tempReportFile = tempFolder.newFile("report.csv");

		queryReportConfiguration = new QueryFileConfigurationData();
		queryReportConfiguration.setCsvEncoding("UTF-8");
		queryReportConfiguration.setCsvCommentChar('#');
		queryReportConfiguration.setCsvFieldSeparator(';');
		queryReportConfiguration.setCsvTextSeparator('"');
		queryReportConfiguration.setCsvLineBreak("\n");

		generator = new CsvReportGenerator();
	}

	@Test
	public void verifyGeneratorReturnCsvAsExtension() {
		assertThat(generator.getExtension()).isEqualTo("csv");
	}

	@Test
	public void withEmptyHeaderAndEmptyResult_generatesEmptyResultFileWithEmptyHeaderLine() {
		GenericSearchResult genericSearchResult = new GenericSearchResult(headers, values);

		boolean generated = generator.createReport(queryReportConfiguration, genericSearchResult, tempReportFile);

		assertThat(generated).isTrue();
		assertThat(tempReportFile).exists();
		assertThat(tempReportFile).hasContent("\n");
	}

	@Test
	public void withHeaderAndEmptyResult_generatesEmptyResultFileWithHeaderLine() {
		GenericSearchResultHeader column1 = new GenericSearchResultHeader(1, "code", "Identifier");
		GenericSearchResultHeader column2 = new GenericSearchResultHeader(2, "name", "Description");
		headers.addAll(List.of(column1, column2));

		GenericSearchResult genericSearchResult = new GenericSearchResult(headers, values);

		boolean generated = generator.createReport(queryReportConfiguration, genericSearchResult, tempReportFile);

		assertThat(generated).isTrue();
		assertThat(tempReportFile).exists();
		assertThat(tempReportFile).hasContent("Identifier;Description\n");
	}

	@Test
	public void withHeaderAndResult_generatesResultFileWithWithHeaderLineAndRows() {
		GenericSearchResultHeader column1 = new GenericSearchResultHeader(1, "code", "Identifier");
		GenericSearchResultHeader column2 = new GenericSearchResultHeader(2, "name", "Description");
		headers.addAll(List.of(column1, column2));

		values.add(Map.of(column1, "1234", column2, "Example 1234"));
		values.add(Map.of(column1, "5678", column2, "Example 5678"));
		values.add(Map.of(column1, "9999", column2, "Example 9!"));

		GenericSearchResult genericSearchResult = new GenericSearchResult(headers, values);

		boolean generated = generator.createReport(queryReportConfiguration, genericSearchResult, tempReportFile);

		assertThat(generated).isTrue();
		assertThat(tempReportFile).exists();
		assertThat(tempReportFile).hasContent("Identifier;Description\n1234;Example 1234\n5678;Example 5678\n9999;Example 9!\n");
	}

	@Test
	public void ifFileWriteFails_() {
		GenericSearchResultHeader column1 = new GenericSearchResultHeader(1, "code", "Identifier");
		GenericSearchResultHeader column2 = new GenericSearchResultHeader(2, "name", "Description");
		headers.addAll(List.of(column1, column2));

		values.add(Map.of(column1, "1234", column2, "Example 1234"));
		values.add(Map.of(column1, "5678", column2, "Example 5678"));
		values.add(Map.of(column1, "9999", column2, "Example 9!"));

		GenericSearchResult genericSearchResult = new GenericSearchResult(headers, values);

		File nonExistingFile = tempFolder.getRoot();
		boolean generated = generator.createReport(queryReportConfiguration, genericSearchResult, nonExistingFile);

		assertThat(generated).isFalse();
	}
}
