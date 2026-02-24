package me.cxdev.commerce.reporting.download;

import java.io.InputStream;

public interface ReportDownloadFacade {
	/**
	 * Provides an {@link InputStream} of an Excel file that was created
	 * based on the search for the given type and query.
	 *
	 * @param title the title for the report
	 * @param type the type to search for
	 * @param query the query to run on the type
	 * @return
	 */
	InputStream getReport(String title, String type, String query);
}
