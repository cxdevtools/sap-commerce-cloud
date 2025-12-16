package tools.sapcx.commerce.search.service;

import java.util.List;
import java.util.Map;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

/**
 * Cx Indexer Service with custom indexer operation functions.
 */
public interface CxIndexerService {
	/**
	 * Get the search indexer for given search config and indexed type.
	 *
	 * @param searchConfig search config code
	 * @param indexedType indexed type code
	 * @return search indexer
	 * @throws FacetConfigServiceException if no indexer was found
	 */
	CxIndexer getSearchIndexer(String searchConfig, String indexedType) throws FacetConfigServiceException;

	/**
	 * Add a single item to the index.
	 *
	 * @param searchConfig search config to use
	 * @param indexedType indexed type to use
	 * @param item item to add
	 * @param <T> type of item
	 * @throws IndexerException if indexer exception occurs
	 */
	default <T extends ItemModel> void addItemToIndex(String searchConfig, String indexedType, T item) throws FacetConfigServiceException, IndexerException {
		getSearchIndexer(searchConfig, indexedType).addItem(item);
	}

	/**
	 * Add a list of items to the index.
	 *
	 * @param searchConfig search config to use
	 * @param indexedType indexed type to use
	 * @param items items to add
	 * @param <T> type of items
	 * @throws IndexerException if indexer exception occurs
	 */
	default <T extends ItemModel> void addItemsToIndex(String searchConfig, String indexedType, List<T> items) throws FacetConfigServiceException, IndexerException {
		getSearchIndexer(searchConfig, indexedType).addItems(items);
	}

	/**
	 * Remove a single item from the index.
	 *
	 * @param searchConfig search config to use
	 * @param indexedType indexed type to use
	 * @param item item to remove
	 * @param <T> type of item
	 * @throws IndexerException if indexer exception occurs
	 */
	default <T extends ItemModel> void removeItemFromIndex(String searchConfig, String indexedType, T item) throws FacetConfigServiceException, IndexerException {
		getSearchIndexer(searchConfig, indexedType).removeItem(item);
	}

	/**
	 * Remove a list of items from the index.
	 *
	 * @param searchConfig search config to use
	 * @param indexedType indexed type to use
	 * @param items items to remove
	 * @param <T> type of items
	 * @throws IndexerException if indexer exception occurs
	 */
	default <T extends ItemModel> void removeItemsFromIndex(String searchConfig, String indexedType, List<T> items) throws FacetConfigServiceException, IndexerException {
		getSearchIndexer(searchConfig, indexedType).removeItems(items);
	}

	/**
	 * Perform index operation for given search config, index operation and a list of indexed types.
	 *
	 * @param facetSearchConfig   facet search config to use
	 * @param indexOperation      index operation to perform
	 * @param indexedTypeNameList list of indexed types to be indexed
	 * @throws IndexerException if indexer exception occurs
	 */
	void performIndexForIndexedTypes(
			FacetSearchConfig facetSearchConfig,
			IndexOperation indexOperation,
			List<String> indexedTypeNameList) throws IndexerException;

	/**
	 * Perform index Operation for given search config, index operation, list of indexed types and map of hints.
	 *
	 * @param facetSearchConfig   facet search config to use
	 * @param indexOperation      index operation to perform
	 * @param indexedTypeNameList list of indexed types to be indexed
	 * @param indexerHints        map of indexer hints to be included
	 * @throws IndexerException if indexer exception occurs
	 */
	void performIndexForIndexedTypes(
			FacetSearchConfig facetSearchConfig,
			IndexOperation indexOperation,
			List<String> indexedTypeNameList,
			Map<String, String> indexerHints) throws IndexerException;
}
