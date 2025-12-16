package tools.sapcx.commerce.search.service;

import java.util.List;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.IndexerService;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

class DefaultCxIndexer implements CxIndexer {
	private IndexerService indexerService;
	private FacetSearchConfig searchConfig;
	private IndexedType indexedType;

	public DefaultCxIndexer(IndexerService indexerService, FacetSearchConfig searchConfig, IndexedType indexedType) {
		this.indexerService = indexerService;
		this.searchConfig = searchConfig;
		this.indexedType = indexedType;
	}

	@Override
	public void addItemsByQuery() throws IndexerException {
		indexerService.updateTypeIndex(searchConfig, indexedType);
	}

	@Override
	public <T extends ItemModel> void addItems(List<T> items) throws IndexerException {
		indexerService.updateTypeIndex(searchConfig, indexedType, getPks(items));
	}

	@Override
	public void removeItemsByQuery() throws IndexerException {
		indexerService.deleteTypeIndex(searchConfig, indexedType);
	}

	@Override
	public <T extends ItemModel> void removeItems(List<T> items) throws IndexerException {
		indexerService.deleteTypeIndex(searchConfig, indexedType, getPks(items));
	}
}
