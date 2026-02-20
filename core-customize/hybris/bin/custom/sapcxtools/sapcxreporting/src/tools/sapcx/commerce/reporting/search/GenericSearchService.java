package tools.sapcx.commerce.reporting.search;

import java.util.Map;

/**
 * A service performing a {@link de.hybris.platform.servicelayer.search.FlexibleSearchQuery}, but instead of returning an
 * {@link de.hybris.platform.servicelayer.search.SearchResult} that does not hold any meta and header information about the query, it
 * returns a dynamic result (just like the {@link java.sql.ResultSet}) containing header information as well as all values in a list.
 */
public interface GenericSearchService {
	/**
	 * Executes the given query against the data source.
	 *
	 * @param query string representation of a search query, typically a flexible search or sql statement
	 * @return the {@link GenericSearchResult}
	 */
	default GenericSearchResult search(String query) {
		return search(query, Map.of());
	};

	/**
	 * Executes the given query with the parameter map against the data source.
	 *
	 * @param query string representation of a search query, typically a flexible search or sql statement
	 * @param parameters map of parameters that are used within the query (may contain any kind of item models)
	 * @return the {@link GenericSearchResult}
	 */
	default GenericSearchResult search(String query, Map<String, Object> parameters) {
		return search(query, parameters, Map.of());
	};

	/**
	 * Executes the given query with the parameter map against the data source.
	 *
	 * @param query string representation of a search query, typically a flexible search or sql statement
	 * @param parameters map of parameters that are used within the query (may contain any kind of item models)
	 * @param configuration map of configuration values that are used within the export
	 * @return the {@link GenericSearchResult}
	 */
	GenericSearchResult search(String query, Map<String, Object> parameters, Map<String, Object> configuration);
}
