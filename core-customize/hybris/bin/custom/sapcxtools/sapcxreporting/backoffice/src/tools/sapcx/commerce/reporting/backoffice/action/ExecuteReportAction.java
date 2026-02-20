package tools.sapcx.commerce.reporting.backoffice.action;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;

import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import tools.sapcx.commerce.reporting.enums.SearchQueryType;
import tools.sapcx.commerce.reporting.model.QueryReportConfigurationModel;
import tools.sapcx.commerce.reporting.report.ReportService;
import tools.sapcx.commerce.reporting.report.data.QueryFileConfigurationData;
import tools.sapcx.commerce.reporting.search.GenericSearchResult;

import jakarta.annotation.Resource;
import tools.sapcx.commerce.reporting.search.GenericSearchService;

public class ExecuteReportAction extends AbstractReportAction {
	private static final Logger LOG = LoggerFactory.getLogger(ExecuteReportAction.class);
	private static final String CONFIRMATION = "executereport.confirmation";
	private static final String SEARCH_ERROR = "executereport.errors.search";
	private static final String REPORT_GENERATE_ERROR = "executereport.errors.generation";
	private static final String FILE_READ_ERROR = "executereport.errors.fileread";

	@Resource(name = "queryConfigurationConverter")
	private Converter<QueryReportConfigurationModel, QueryFileConfigurationData> queryConfigurationConverter;

	@Override
	protected ActionResult<Object> processSearchResult(ActionContext<QueryReportConfigurationModel> actionContext, GenericSearchResult searchResult) {
		final QueryReportConfigurationModel report = actionContext.getData();
		if (searchResult.hasError()) {
			return error(MessageFormat.format(actionContext.getLabel(SEARCH_ERROR), searchResult.getError()));
		}

		final QueryFileConfigurationData queryFileConfigurationData = queryConfigurationConverter.convert(report);
		final Optional<File> reportFile = dataReportService.getReportFile(queryFileConfigurationData, searchResult);
		if (!reportFile.isPresent()) {
			return error(actionContext.getLabel(REPORT_GENERATE_ERROR));
		}

		final File media = reportFile.get();
		try {
			final String extension = FilenameUtils.getExtension(media.getAbsolutePath());
			final String filename = defaultIfBlank(report.getTitle(), report.getId()) + "." + extension;
			Filedownload.save(new FileInputStream(media), Files.probeContentType(media.toPath()), filename);
			return success();
		} catch (IOException e) {
			LOG.error("Error reading media file for report " + report.getTitle(), e);
			return error(actionContext.getLabel(FILE_READ_ERROR));
		} finally {
			try {
				Files.delete(media.toPath());
			} catch (IOException e) {
				LOG.warn("Error deleting temporary media file at: " + media.getAbsolutePath(), e);
			}
		}
	}

	@Override
	public boolean needsConfirmation(ActionContext<QueryReportConfigurationModel> ctx) {
		return true;
	}

	@Override
	public String getConfirmationMessage(ActionContext<QueryReportConfigurationModel> ctx) {
		return ctx.getLabel(CONFIRMATION);
	}
}
