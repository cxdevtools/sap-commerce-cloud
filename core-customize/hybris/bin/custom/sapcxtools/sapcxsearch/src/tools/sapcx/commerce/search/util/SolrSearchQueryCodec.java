package tools.sapcx.commerce.search.util;

import java.util.ArrayList;
import java.util.List;

import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchQueryData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchQueryTermData;

/**
 * INFO: This class is an exact copy of de.hybris.platform.commercewebservices.core.util.ws.impl.DefaultSearchQueryCodec
 * This was done so that we have this class accessible to our Facade Layer classes (while the Original Class
 * stays in the WebContext and hence is not reachable from Facade Layer)
 */
public class SolrSearchQueryCodec implements SearchQueryCodec<SolrSearchQueryData> {
	protected static final int NEXT_TERM = 2;

	@Override
	public SolrSearchQueryData decodeQuery(final String queryString) {
		final SolrSearchQueryData searchQuery = new SolrSearchQueryData();
		final List<SolrSearchQueryTermData> filters = new ArrayList<SolrSearchQueryTermData>();

		if (queryString == null) {
			return searchQuery;
		}

		final String[] parts = queryString.split(":");

		if (parts.length > 0) {
			searchQuery.setFreeTextSearch(parts[0]);
			if (parts.length > 1) {
				searchQuery.setSort(parts[1]);
			}
		}

		for (int i = NEXT_TERM; i < parts.length; i = i + NEXT_TERM) {
			final SolrSearchQueryTermData term = new SolrSearchQueryTermData();
			term.setKey(parts[i]);
			term.setValue(parts[i + 1]);
			filters.add(term);
		}
		searchQuery.setFilterTerms(filters);

		return searchQuery;
	}

	@Override
	public String encodeQuery(final SolrSearchQueryData searchQueryData) {
		if (searchQueryData == null) {
			return null;
		}

		final StringBuilder builder = new StringBuilder();
		builder.append((searchQueryData.getFreeTextSearch() == null) ? "" : searchQueryData.getFreeTextSearch());

		if (searchQueryData.getSort() != null //
				|| (searchQueryData.getFilterTerms() != null && !searchQueryData.getFilterTerms().isEmpty())) {
			builder.append(":");
			builder.append((searchQueryData.getSort() == null) ? "" : searchQueryData.getSort());
		}

		final List<SolrSearchQueryTermData> terms = searchQueryData.getFilterTerms();
		if (terms != null && !terms.isEmpty()) {
			for (final SolrSearchQueryTermData term : searchQueryData.getFilterTerms()) {
				builder.append(":");
				builder.append(term.getKey());
				builder.append(":");
				builder.append(term.getValue());
			}
		}

		// URLEncode?
		return builder.toString();
	}
}
