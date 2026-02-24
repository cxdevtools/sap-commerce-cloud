package me.cxdev.commerce.reporting.generator.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.function.Supplier;

import de.hybris.platform.util.CSVWriter;

import org.apache.commons.lang3.StringUtils;

import me.cxdev.commerce.reporting.model.QueryReportConfigurationModel;
import me.cxdev.commerce.reporting.report.data.QueryFileConfigurationData;

/**
 * Implementation of the {@link CSVWriter} that takes the configuration for the CSV file from the underlying
 * {@link QueryReportConfigurationModel} instead of using the system wide configuration settings.
 */
public class CsvReportWriter extends CSVWriter {
	private static final String INITIAL_ENCODING = "UTF-8";
	private static final char INITIAL_COMMENT_CHAR = '#';
	private static final char INITIAL_FIELD_SEPARATOR = ';';
	private static final char INITIAL_TEXT_SEPARATOR = '"';
	private static final String INITIAL_LINE_BREAK = StringUtils.LF;
	private static final String[] INITIAL_LINE_SEPARATORS = new String[] { "\n", "\r\n", "\r", "\u0085", "\u2028", "\u2029" };

	/**
	 * Initializes the {@link CsvReportWriter} with the {@link File} and {@link QueryReportConfigurationModel}. Missing values from the
	 * configuration will be replaced by the defaults of the underlying {@link CSVWriter}.
	 *
	 * @param file   the {@link File} to write to
	 * @param report the {@link QueryReportConfigurationModel} to fetch export format settings from
	 * @throws FileNotFoundException        if given {@link File} does not exist or is not writable
	 * @throws UnsupportedEncodingException if encoding of {@link QueryReportConfigurationModel} is not supported
	 */
	public CsvReportWriter(File file, QueryFileConfigurationData report) throws UnsupportedEncodingException, FileNotFoundException {
		super(file, StringUtils.defaultIfBlank(report.getCsvEncoding(), INITIAL_ENCODING));
		reinitializeDefaultsFromReport(report);
	}

	private void reinitializeDefaultsFromReport(QueryFileConfigurationData report) {
		setCommentchar(getValueWithDefaultSupplier(report.getCsvCommentChar(), super::getDefaultCommentChar));
		setFieldseparator(getValueWithDefaultSupplier(report.getCsvFieldSeparator(), super::getDefaultFieldSeparator));
		setTextseparator(getValueWithDefaultSupplier(report.getCsvTextSeparator(), super::getDefaultTextSeparator));
		setLinebreak(getValueWithDefaultSupplier(report.getCsvLineBreak(), super::getDefaultLineBreak));
	}

	private <T> T getValueWithDefaultSupplier(T value, Supplier<T> defaultSupplier) {
		return (value != null) ? value : defaultSupplier.get();
	}

	/**
	 * Overwritten to avoid boostrap of platform while testing. We simply provide the initial fallback value here.
	 * The configured value will be set during object initialization, but after the super constructor has finished.
	 *
	 * @return {@link #INITIAL_COMMENT_CHAR}
	 */
	@Override
	protected char getDefaultCommentChar() {
		return INITIAL_COMMENT_CHAR;
	}

	/**
	 * Overwritten to avoid boostrap of platform while testing. We simply provide the initial fallback value here.
	 * The configured value will be set during object initialization, but after the super constructor has finished.
	 *
	 * @return {@link #INITIAL_FIELD_SEPARATOR}
	 */
	@Override
	protected char getDefaultFieldSeparator() {
		return INITIAL_FIELD_SEPARATOR;
	}

	/**
	 * Overwritten to avoid boostrap of platform while testing. We simply provide the initial fallback value here.
	 * The configured value will be set during object initialization, but after the super constructor has finished.
	 *
	 * @return {@link #INITIAL_TEXT_SEPARATOR}
	 */
	@Override
	protected char getDefaultTextSeparator() {
		return INITIAL_TEXT_SEPARATOR;
	}

	/**
	 * Overwritten to avoid boostrap of platform while testing. We simply provide the initial fallback value here.
	 * The configured value will be set during object initialization, but after the super constructor has finished.
	 *
	 * @return {@link #INITIAL_LINE_BREAK}
	 */
	@Override
	protected String getDefaultLineBreak() {
		return INITIAL_LINE_BREAK;
	}

	/**
	 * Overwritten to avoid boostrap of platform while testing. We simply provide the initial fallback value here.
	 * The configured value will be set during object initialization, but after the super constructor has finished.
	 *
	 * @return {@link #INITIAL_LINE_SEPARATORS}
	 */
	@Override
	protected String[] getDefaultLineSeparators() {
		return INITIAL_LINE_SEPARATORS;
	}
}
