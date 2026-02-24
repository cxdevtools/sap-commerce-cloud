package me.cxdev.commerce.reporting.generator;

import java.io.File;

import me.cxdev.commerce.reporting.report.data.QueryFileConfigurationData;
import me.cxdev.commerce.reporting.search.GenericSearchResult;

public interface ReportGenerator {
	boolean createReport(QueryFileConfigurationData report, GenericSearchResult searchResult, File file);

	String getExtension();
}
