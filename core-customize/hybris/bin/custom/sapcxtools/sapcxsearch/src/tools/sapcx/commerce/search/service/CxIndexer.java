package tools.sapcx.commerce.search.service;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

/**
 * Indexer interface to perform indexing operations against the underlying search index.
 * <p>
 * The indexer can be used to add or remove items to/from the index. The items can be
 * provided as a list or by performing the queries defined in the search configuration.
 */
public interface CxIndexer {
	/**
	 * Add a single item to the index.
	 *
	 * @param item item to add
	 * @param <T> type of item
	 * @throws IndexerException if indexer exception occurs
	 */
	default <T extends ItemModel> void addItem(T item) throws IndexerException {
		addItems(List.of(item));
	}

	/**
	 * Add a list of items to the index.
	 *
	 * @param items items to add
	 * @param <T> type of item
	 * @throws IndexerException if indexer exception occurs
	 */
	<T extends ItemModel> void addItems(List<T> items) throws IndexerException;

	/**
	 * Add items to the index by using the configured query.
	 *
	 * @throws IndexerException if indexer exception occurs
	 */
	void addItemsByQuery() throws IndexerException;

	/**
	 * Remove a single item from the index.
	 *
	 * @param item item to add
	 * @param <T> type of item
	 * @throws IndexerException if indexer exception occurs
	 */
	default <T extends ItemModel> void removeItem(T item) throws IndexerException {
		removeItems(List.of(item));
	}

	/**
	 * Remove a list of items from the index.
	 *
	 * @param items items to remove
	 * @param <T> type of item
	 * @throws IndexerException if indexer exception occurs
	 */
	<T extends ItemModel> void removeItems(List<T> items) throws IndexerException;

	/**
	 * Remove items from the index by using the configured query.
	 *
	 * @throws IndexerException if indexer exception occurs
	 */
	void removeItemsByQuery() throws IndexerException;

	/**
	 * Get a list of primary keys from a list of items.
	 *
	 * @param items list of items
	 * @return list of primary keys
	 * @param <T> type of item
	 */
	default <T extends ItemModel> List<PK> getPks(List<T> items) {
		return emptyIfNull(items).stream()
				.map(ItemModel::getPk)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
}
