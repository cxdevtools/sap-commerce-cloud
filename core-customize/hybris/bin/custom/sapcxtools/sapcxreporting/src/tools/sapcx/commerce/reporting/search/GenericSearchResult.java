package tools.sapcx.commerce.reporting.search;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

public class GenericSearchResult {
	private List<GenericSearchResultHeader> headers;
	private List<Map<GenericSearchResultHeader, String>> values;
	private String error;

	public GenericSearchResult(List<GenericSearchResultHeader> headers, List<Map<GenericSearchResultHeader, String>> values) {
		this.headers = headers;
		this.values = values;
	}

	public GenericSearchResult(String error) {
		this.error = error;
	}

	public List<GenericSearchResultHeader> getHeaders() {
		return ListUtils.emptyIfNull(headers);
	}

	public List<String> getHeaderNames() {
		return getHeaders().stream()
				.map(GenericSearchResultHeader::getExportName)
				.collect(Collectors.toList());
	}

	public List<Map<GenericSearchResultHeader, String>> getValues() {
		return ListUtils.emptyIfNull(values);
	}

	public int getResultColumns() {
		return getHeaders().size();
	}

	public int getResultRows() {
		return getValues().size();
	}

	public boolean hasError() {
		return StringUtils.isNotBlank(error);
	}

	public String getError() {
		return error;
	}
}
