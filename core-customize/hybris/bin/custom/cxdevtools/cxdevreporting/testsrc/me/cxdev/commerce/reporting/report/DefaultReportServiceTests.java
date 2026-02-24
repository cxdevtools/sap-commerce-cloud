package me.cxdev.commerce.reporting.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import me.cxdev.commerce.reporting.enums.ReportExportFormat;
import me.cxdev.commerce.reporting.generator.ReportGenerator;
import me.cxdev.commerce.reporting.model.CatalogVersionConfigurationParameterModel;
import me.cxdev.commerce.reporting.model.CategoryConfigurationParameterModel;
import me.cxdev.commerce.reporting.model.ProductConfigurationParameterModel;
import me.cxdev.commerce.reporting.model.QueryReportConfigurationModel;
import me.cxdev.commerce.reporting.report.data.QueryFileConfigurationData;
import me.cxdev.commerce.reporting.search.GenericSearchResult;
import me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory;

@UnitTest
public class DefaultReportServiceTests {
	private static final GenericSearchResult EMPTY_SEARCH_RESULT = new GenericSearchResult(List.of(), List.of());
	private static final GenericSearchResult ERRONEOUS_SEARCH_RESULT = new GenericSearchResult("error!");

	private ReportGenerator reportGenerator;
	private QueryReportConfigurationModel report;
	private QueryFileConfigurationData fileConfiguration;
	private DefaultReportService service;

	@Before
	public void setUp() throws Exception {
		report = InMemoryModelFactory.createTestableItemModel(QueryReportConfigurationModel.class);
		report.setExportFormat(ReportExportFormat.CSV);

		fileConfiguration = new QueryFileConfigurationData();
		fileConfiguration.setExportFormat("CSV");

		reportGenerator = mock(ReportGenerator.class);
		when(reportGenerator.getExtension()).thenReturn("csv");
		when(reportGenerator.createReport(eq(fileConfiguration), eq(EMPTY_SEARCH_RESULT), any(File.class))).thenReturn(true);

		service = new DefaultReportService(Map.of(ReportExportFormat.CSV, reportGenerator));
	}

	@Test
	public void verifyReportDirectoryIsExisting() {
		File reportDirectory = service.getReportDirectory();
		assertThat(reportDirectory.exists()).isTrue();
		assertThat(reportDirectory.getAbsolutePath()).endsWith("cxdevtools/reports");
	}

	@Test
	public void verifyReportDirectoryIsRecreatedIfMissing() throws IOException {
		// Clear it, we use a call to get the dynamic temporary path for testing
		FileUtils.deleteDirectory(service.getReportDirectory().getParentFile());

		File reportDirectory = service.getReportDirectory();
		assertThat(reportDirectory.exists()).isTrue();
		assertThat(reportDirectory.getAbsolutePath()).endsWith("cxdevtools/reports");
	}

	@Test
	public void verifyReportParametersAreResolved() {
		ProductConfigurationParameterModel param1 = InMemoryModelFactory.createTestableItemModel(ProductConfigurationParameterModel.class);
		param1.setName("product");
		param1.setItem(InMemoryModelFactory.createTestableItemModel(ProductModel.class));

		CategoryConfigurationParameterModel param2 = InMemoryModelFactory.createTestableItemModel(CategoryConfigurationParameterModel.class);
		param2.setName("category");
		param2.setItem(InMemoryModelFactory.createTestableItemModel(CategoryModel.class));

		CatalogVersionConfigurationParameterModel param3 = InMemoryModelFactory.createTestableItemModel(CatalogVersionConfigurationParameterModel.class);
		param3.setName("catalogVersion");
		param3.setItemList(
				List.of(InMemoryModelFactory.createTestableItemModel(CatalogVersionModel.class), InMemoryModelFactory.createTestableItemModel(CatalogVersionModel.class)));

		report.setParameters(List.of(param1, param2, param3));

		Map<String, Object> reportParameters = service.getReportParameters(report);

		assertThat(reportParameters).hasSize(3);
		assertThat(reportParameters).containsKeys("product", "category", "catalogVersion");
		assertThat(reportParameters.values()).hasAtLeastOneElementOfType(ProductModel.class);
		assertThat(reportParameters.values()).hasAtLeastOneElementOfType(CategoryModel.class);
		assertThat(reportParameters.values()).hasAtLeastOneElementOfType(List.class);
	}

	@Test
	public void withErrorsInSearchResult_noReportIsGenerated() {
		Optional<File> reportFile = service.getReportFile(fileConfiguration, ERRONEOUS_SEARCH_RESULT);
		assertThat(reportFile).isNotPresent();
	}

	@Test
	public void withMissingExportFormatMapping_noReportIsGenerated() {
		fileConfiguration.setExportFormat("EXCEL");
		Optional<File> reportFile = service.getReportFile(fileConfiguration, EMPTY_SEARCH_RESULT);
		assertThat(reportFile).isNotPresent();
	}

	@Test
	public void withMatchingExportFormatMapping_reportIsGenerated() {
		Optional<File> reportFile = service.getReportFile(fileConfiguration, EMPTY_SEARCH_RESULT);

		assertThat(reportFile).isPresent();
		assertThat(reportFile.get()).exists();
		verify(reportGenerator).createReport(eq(fileConfiguration), eq(EMPTY_SEARCH_RESULT), any(File.class));
	}

	@Test
	public void whenReportGeneratorFails_noReportIsGenerated() {
		doThrow(RuntimeException.class).when(reportGenerator).createReport(eq(fileConfiguration), eq(EMPTY_SEARCH_RESULT), any(File.class));

		Optional<File> reportFile = service.getReportFile(fileConfiguration, EMPTY_SEARCH_RESULT);

		assertThat(reportFile).isNotPresent();
		verify(reportGenerator).createReport(eq(fileConfiguration), eq(EMPTY_SEARCH_RESULT), any(File.class));
	}

	@Test
	public void whenReportFileCannotBeCreated_noReportIsGenerated() throws IOException {
		File fileUnabledToBeCreated = mock(File.class);
		when(fileUnabledToBeCreated.exists()).thenReturn(false);
		when(fileUnabledToBeCreated.createNewFile()).thenReturn(false);

		service = new DefaultReportService(Map.of(ReportExportFormat.CSV, reportGenerator)) {
			@Override
			protected File getTemporaryReportFile(String filename) {
				return fileUnabledToBeCreated;
			}
		};

		Optional<File> reportFile = service.getReportFile(fileConfiguration, EMPTY_SEARCH_RESULT);

		assertThat(reportFile).isNotPresent();
		verify(reportGenerator, never()).createReport(eq(fileConfiguration), eq(EMPTY_SEARCH_RESULT), any(File.class));
	}

	@Test
	public void whenReportFileCreationFails_noReportIsGenerated() throws IOException {
		File fileUnabledToBeCreated = mock(File.class);
		when(fileUnabledToBeCreated.exists()).thenReturn(false);
		doThrow(IOException.class).when(fileUnabledToBeCreated).createNewFile();

		service = new DefaultReportService(Map.of(ReportExportFormat.CSV, reportGenerator)) {
			@Override
			protected File getTemporaryReportFile(String filename) {
				return fileUnabledToBeCreated;
			}
		};

		Optional<File> reportFile = service.getReportFile(fileConfiguration, EMPTY_SEARCH_RESULT);

		assertThat(reportFile).isNotPresent();
		verify(reportGenerator, never()).createReport(eq(fileConfiguration), eq(EMPTY_SEARCH_RESULT), any(File.class));
	}
}
