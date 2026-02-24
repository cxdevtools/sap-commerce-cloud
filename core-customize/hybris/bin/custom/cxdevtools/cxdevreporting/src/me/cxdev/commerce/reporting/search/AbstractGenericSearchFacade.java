package me.cxdev.commerce.reporting.search;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.hybris.platform.commercefacades.search.data.SearchStateData;
import de.hybris.platform.commerceservices.search.facetdata.FacetSearchPageData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;

public abstract class AbstractGenericSearchFacade<T> implements GenericSearchService {
	private static final int DEFAULT_PAGE_SIZE = 100;

	protected abstract Map<String, Function<T, String>> getExportFieldConfiguration();

	protected abstract FacetSearchPageData<SearchStateData, T> doSearch(String query, PageableData pageableData);

	protected int getPageSize() {
		return DEFAULT_PAGE_SIZE;
	}

	@Override
	public GenericSearchResult search(String query, Map<String, Object> parameters) {
		int currentPage = 0;
		PageableData pageableData = new PageableData();
		pageableData.setCurrentPage(currentPage);
		pageableData.setPageSize(getPageSize());

		Map<String, Function<T, String>> valueProvider = getExportFieldConfiguration();
		List<GenericSearchResultHeader> headers = getHeaders(valueProvider.keySet());
		List<Map<GenericSearchResultHeader, String>> values = new ArrayList<>();

		// Fetch first result for pagination information
		FacetSearchPageData<SearchStateData, T> result = doSearch(query, pageableData);
		int numberOfPages = result.getPagination().getNumberOfPages();

		// Iterate over result pages
		for (; currentPage < numberOfPages; currentPage++) {
			if (currentPage != 0) {
				pageableData.setCurrentPage(currentPage);
				result = doSearch(query, pageableData);
			}
			values.addAll(getValues(headers, valueProvider, result));
		}
		return new GenericSearchResult(headers, values);
	}

	private List<GenericSearchResultHeader> getHeaders(Set<String> headerNames) {
		List<GenericSearchResultHeader> headers = new ArrayList<>();

		Iterator<String> iterator = headerNames.iterator();
		for (int i = 0; iterator.hasNext(); i++) {
			String columnName = iterator.next();
			headers.add(new GenericSearchResultHeader(i, columnName, columnName));
		}
		return headers;
	}

	private List<Map<GenericSearchResultHeader, String>> getValues(List<GenericSearchResultHeader> headers, Map<String, Function<T, String>> valueProvider,
			FacetSearchPageData<SearchStateData, T> searchResult) {
		List<T> results = emptyIfNull(searchResult.getResults());
		int totalResults = results.size();

		List<Map<GenericSearchResultHeader, String>> resultTable = new ArrayList<>(totalResults);
		for (T data : results) {
			Map<GenericSearchResultHeader, String> rowValues = new LinkedHashMap<>(headers.size());
			for (GenericSearchResultHeader header : headers) {
				rowValues.put(header, valueProvider.get(header.getColumnName()).apply(data));
			}
			resultTable.add(rowValues);
		}
		return resultTable;
	}
}
