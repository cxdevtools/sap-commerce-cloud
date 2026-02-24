package me.cxdev.commerce.reporting.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import me.cxdev.commerce.reporting.model.QueryReportConfigurationModel;
import me.cxdev.commerce.reporting.report.data.QueryFileConfigurationData;

public class QueryReportConfigurationPopulator implements Populator<QueryReportConfigurationModel, QueryFileConfigurationData> {

	@Override
	public void populate(QueryReportConfigurationModel source, QueryFileConfigurationData target) throws ConversionException {
		target.setTitle(source.getTitle());
		target.setStringDateFormat(source.getStringDateFormat());
		target.setExportFormat(source.getExportFormat().getCode());
		target.setCompress(source.getCompress());
		target.setCsvEncoding(source.getCsvEncoding());
		target.setCsvCommentChar(source.getCsvCommentChar());
		target.setCsvFieldSeparator(source.getCsvFieldSeparator());
		target.setCsvTextSeparator(source.getCsvTextSeparator());
		target.setCsvLineBreak(source.getCsvLineBreak());
		target.setExcelHighlightHeader(source.isExcelHighlightHeader());
		target.setExcelFreezeHeader(source.isExcelFreezeHeader());
		target.setExcelActivateFilter(source.isExcelActivateFilter());
		target.setExcelAutosizeColumns(source.isExcelAutosizeColumns());
		target.setExcelAlternatingLines(source.isExcelAlternatingLines());
	}
}
