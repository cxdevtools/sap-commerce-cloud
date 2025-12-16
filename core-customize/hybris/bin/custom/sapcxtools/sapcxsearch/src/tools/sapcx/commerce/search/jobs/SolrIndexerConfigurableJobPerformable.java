package tools.sapcx.commerce.search.jobs;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.stream.Collectors;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.enums.IndexerOperationValues;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;

import org.slf4j.Logger;

import tools.sapcx.commerce.search.model.SolrIndexerConfigurableCronJobModel;
import tools.sapcx.commerce.search.service.CxIndexerService;

public class SolrIndexerConfigurableJobPerformable extends AbstractJobPerformable<SolrIndexerConfigurableCronJobModel> {
	private static final Logger LOG = getLogger(SolrIndexerConfigurableJobPerformable.class);

	private CxIndexerService cxIndexerService;
	private FacetSearchConfigService facetSearchConfigService;

	public SolrIndexerConfigurableJobPerformable(CxIndexerService cxIndexerService, FacetSearchConfigService facetSearchConfigService) {
		this.cxIndexerService = cxIndexerService;
		this.facetSearchConfigService = facetSearchConfigService;
	}

	@Override
	public PerformResult perform(SolrIndexerConfigurableCronJobModel jobModel) {
		try {
			FacetSearchConfig facetSearchConfig = getFacetSearchConfig(jobModel);
			List<String> indexedTypeNameList = getIndexedTypeNameList(jobModel);
			IndexOperation indexOperation = getIndexerOperation(jobModel);

			cxIndexerService.performIndexForIndexedTypes(facetSearchConfig, indexOperation, indexedTypeNameList, jobModel.getIndexerHints());
		} catch (FacetConfigServiceException | IndexerException e) {
			LOG.error(e.getMessage(), e);
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	private FacetSearchConfig getFacetSearchConfig(SolrIndexerConfigurableCronJobModel jobModel) throws FacetConfigServiceException {
		return facetSearchConfigService.getConfiguration(jobModel.getFacetSearchConfig().getName());
	}

	private List<String> getIndexedTypeNameList(SolrIndexerConfigurableCronJobModel jobModel) {
		return jobModel.getIndexedTypes().stream()
				.map(SolrIndexedTypeModel::getIdentifier)
				.collect(Collectors.toList());
	}

	private IndexOperation getIndexerOperation(SolrIndexerConfigurableCronJobModel jobModel) {
		if (jobModel.getIndexerOperation() == IndexerOperationValues.FULL) {
			return IndexOperation.FULL;
		} else if (jobModel.getIndexerOperation() == IndexerOperationValues.UPDATE) {
			return IndexOperation.UPDATE;
		} else if (jobModel.getIndexerOperation() == IndexerOperationValues.DELETE) {
			return IndexOperation.DELETE;
		} else if (jobModel.getIndexerOperation() == IndexerOperationValues.PARTIAL_UPDATE) {
			return IndexOperation.PARTIAL_UPDATE;
		}
		return IndexOperation.FULL;
	}
}
