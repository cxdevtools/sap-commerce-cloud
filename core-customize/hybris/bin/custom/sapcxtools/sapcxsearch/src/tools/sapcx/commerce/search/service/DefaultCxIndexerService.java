package tools.sapcx.commerce.search.service;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerService;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategy;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategyFactory;

import org.slf4j.Logger;

public class DefaultCxIndexerService implements CxIndexerService {
	private static final Logger LOG = getLogger(DefaultCxIndexerService.class);

	private final IndexerStrategyFactory indexerStrategyFactory;
	private final FacetSearchConfigService facetSearchConfigService;
	private final IndexerService indexerService;

	public DefaultCxIndexerService(
			IndexerStrategyFactory indexerStrategyFactory,
			FacetSearchConfigService facetSearchConfigService,
			IndexerService indexerService) {
		this.indexerStrategyFactory = indexerStrategyFactory;
		this.facetSearchConfigService = facetSearchConfigService;
		this.indexerService = indexerService;
	}

	@Override
	public CxIndexer getSearchIndexer(String searchConfig, String indexedType) throws FacetConfigServiceException {
		FacetSearchConfig config = facetSearchConfigService.getConfiguration(searchConfig);
		IndexedType type = facetSearchConfigService.resolveIndexedType(config, indexedType);
		return new DefaultCxIndexer(indexerService, config, type);
	}

	@Override
	public void performIndexForIndexedTypes(FacetSearchConfig facetSearchConfig, IndexOperation indexOperation, List<String> indexedTypeNameList) throws IndexerException {
		performIndexForIndexedTypes(facetSearchConfig, indexOperation, indexedTypeNameList, Collections.emptyMap());
	}

	@Override
	public void performIndexForIndexedTypes(
			FacetSearchConfig facetSearchConfig,
			IndexOperation indexOperation,
			List<String> indexedTypeNameList,
			Map<String, String> indexerHints) throws IndexerException {
		List<IndexedType> indexedTypeList = getTypesToIndex(facetSearchConfig, indexedTypeNameList);
		for (IndexedType indexedType : indexedTypeList) {
			LOG.info(String.format("Perform index operation '%s' for indexed type '%s'", indexOperation, indexedType.getIdentifier()));
			IndexerStrategy indexerStrategy = createIndexerStrategy(facetSearchConfig);
			indexerStrategy.setIndexOperation(indexOperation);
			indexerStrategy.setFacetSearchConfig(facetSearchConfig);
			indexerStrategy.setIndexedType(indexedType);
			indexerStrategy.setIndexerHints(indexerHints);
			indexerStrategy.execute();
		}
	}

	private List<IndexedType> getTypesToIndex(FacetSearchConfig facetSearchConfig, List<String> indexedTypeNameList) {
		return facetSearchConfig.getIndexConfig().getIndexedTypes().values().stream()
				.filter(indexedType -> indexedTypeNameList.contains(indexedType.getIdentifier()))
				.collect(Collectors.toList());
	}

	private IndexerStrategy createIndexerStrategy(FacetSearchConfig facetSearchConfig) throws IndexerException {
		return indexerStrategyFactory.createIndexerStrategy(facetSearchConfig);
	}
}
