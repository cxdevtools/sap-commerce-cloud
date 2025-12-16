package tools.sapcx.commerce.search.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static tools.sapcx.commerce.toolkit.testing.itemmodel.InMemoryModelFactory.createTestableItemModel;

import java.util.List;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.enums.IndexerOperationValues;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;

import org.junit.Before;
import org.junit.Test;

import tools.sapcx.commerce.search.model.SolrIndexerConfigurableCronJobModel;
import tools.sapcx.commerce.search.service.CxIndexerService;

@UnitTest
public class SolrIndexerConfigurableJobPerformableTests {
	private SolrIndexerConfigurableJobPerformable jobPerformable;
	private CxIndexerService cxIndexerService;
	private FacetSearchConfigService facetSearchConfigService;
	private SolrIndexerConfigurableCronJobModel jobModel;
	private SolrFacetSearchConfigModel configModel;
	private FacetSearchConfig facetSearchConfig;

	@Before
	public void setUp() throws Exception {
		cxIndexerService = mock(CxIndexerService.class);
		facetSearchConfigService = mock(FacetSearchConfigService.class);
		jobPerformable = new SolrIndexerConfigurableJobPerformable(cxIndexerService, facetSearchConfigService);

		jobModel = createTestableItemModel(SolrIndexerConfigurableCronJobModel.class);
		configModel = createTestableItemModel(SolrFacetSearchConfigModel.class);
		configModel.setName("test-config");

		SolrIndexedTypeModel solrIndexedTypeModelA = createTestableItemModel(SolrIndexedTypeModel.class);
		solrIndexedTypeModelA.setIdentifier("type-a");

		SolrIndexedTypeModel solrIndexedTypeModelB = createTestableItemModel(SolrIndexedTypeModel.class);
		solrIndexedTypeModelB.setIdentifier("type-b");

		jobModel.setFacetSearchConfig(configModel);
		jobModel.setIndexedTypes(List.of(solrIndexedTypeModelA, solrIndexedTypeModelB));
		jobModel.setIndexerOperation(IndexerOperationValues.FULL);

		facetSearchConfig = new FacetSearchConfig();
	}

	@Test
	public void verifyPerform() throws FacetConfigServiceException {
		when(facetSearchConfigService.getConfiguration("test-config")).thenReturn(facetSearchConfig);
		PerformResult result = jobPerformable.perform(jobModel);
		assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
		assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
	}

	@Test()
	public void verifyPerformWithConfigError() throws FacetConfigServiceException {
		when(facetSearchConfigService.getConfiguration("test-config")).thenThrow(FacetConfigServiceException.class);
		PerformResult result = jobPerformable.perform(jobModel);
		assertThat(result.getResult()).isEqualTo(CronJobResult.ERROR);
		assertThat(result.getStatus()).isEqualTo(CronJobStatus.ABORTED);
	}

	@Test()
	public void verifyPerformWithIndexerError() throws FacetConfigServiceException, IndexerException {
		when(facetSearchConfigService.getConfiguration("test-config")).thenReturn(facetSearchConfig);

		doThrow(IndexerException.class).doNothing().when(cxIndexerService).performIndexForIndexedTypes(facetSearchConfig, IndexOperation.FULL, List.of("type-a", "type-b"), null);

		PerformResult result = jobPerformable.perform(jobModel);
		assertThat(result.getResult()).isEqualTo(CronJobResult.ERROR);
		assertThat(result.getStatus()).isEqualTo(CronJobStatus.ABORTED);
	}
}
