package me.cxdev.commerce.reporting.search;

import static org.assertj.core.api.Assertions.assertThat;

import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.Test;

@UnitTest
public class GenericSearchResultHeaderTests {
	@Test
	public void getExportNameReturnsIndexAsFallback() {
		GenericSearchResultHeader header = new GenericSearchResultHeader(1, null, null);
		assertThat(header.getExportName()).isEqualTo("1");
	}

	@Test
	public void getExportNameReturnsNameIfSet() {
		GenericSearchResultHeader header = new GenericSearchResultHeader(1, "name", null);
		assertThat(header.getExportName()).isEqualTo("name");
	}

	@Test
	public void getExportNameReturnsLabelIfSet() {
		GenericSearchResultHeader header = new GenericSearchResultHeader(1, null, "label");
		assertThat(header.getExportName()).isEqualTo("label");
	}

	@Test
	public void getExportNameFavorsLabelOverNameOverIndex() {
		GenericSearchResultHeader header = new GenericSearchResultHeader(1, "name", "label");
		assertThat(header.getExportName()).isEqualTo("label");
	}

	@Test
	public void toStringContainsAllAttributes() {
		GenericSearchResultHeader header = new GenericSearchResultHeader(1, "name", "label");
		assertThat(header.toString()).startsWith(GenericSearchResultHeader.class.getCanonicalName());
		assertThat(header.toString()).endsWith("[columnIndex=1,name=name,label=label]");
	}

}
