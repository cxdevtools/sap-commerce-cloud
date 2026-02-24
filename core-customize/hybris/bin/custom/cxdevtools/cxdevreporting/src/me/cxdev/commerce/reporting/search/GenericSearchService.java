package me.cxdev.commerce.reporting.search;

import java.util.Map;

/**
 * A service performing a {@link de.hybris.platform.servicelayer.search.FlexibleSearchQuery}, but instead of returning an
 * {@link de.hybris.platform.servicelayer.search.SearchResult} that does not hold any meta and header information about the query, it
 * returns a dynamic result (just like the {@link java.sql.ResultSet}) containing header information as well as all values in a list.
 */
public interface GenericSearchService {
	/**
	 * Executes the given query with the parameter map against the data source.
	 *
	 * @param query string representation of a search query, typically a flexible search or sql statement
	 * @param parameters map of parameters that are used within the query (may contain any kind of item models)
	 * @return the {@link GenericSearchResult}
	 */
	GenericSearchResult search(String query, Map<String, Object> parameters);
}
