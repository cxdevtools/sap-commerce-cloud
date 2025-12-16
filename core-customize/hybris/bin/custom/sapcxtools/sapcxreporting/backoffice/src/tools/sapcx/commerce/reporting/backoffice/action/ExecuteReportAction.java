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
import com.hybris.cockpitng.actions.CockpitAction;

import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import tools.sapcx.commerce.reporting.model.QueryReportConfigurationModel;
import tools.sapcx.commerce.reporting.report.ReportService;
import tools.sapcx.commerce.reporting.report.data.QueryFileConfigurationData;
import tools.sapcx.commerce.reporting.search.FlexibleSearchGenericSearchService;
import tools.sapcx.commerce.reporting.search.GenericSearchResult;

import jakarta.annotation.Resource;

public class ExecuteReportAction implements CockpitAction<QueryReportConfigurationModel, Object> {
	private static final Logger LOG = LoggerFactory.getLogger(ExecuteReportAction.class);
	private static final String CONFIRMATION = "executereport.confirmation";
	private static final String SEARCH_ERROR = "executereport.errors.search";
	private static final String REPORT_GENERATE_ERROR = "executereport.errors.generation";
	private static final String FILE_READ_ERROR = "executereport.errors.fileread";

	@Resource(name = "cxGenericSearchService")
	private FlexibleSearchGenericSearchService flexibleSearchService;

	@Resource(name = "cxReportService")
	private ReportService dataReportService;

	@Resource(name = "queryConfigurationConverter")
	private Converter<QueryReportConfigurationModel, QueryFileConfigurationData> queryConfigurationConverter;

	@Override
	public ActionResult<Object> perform(ActionContext<QueryReportConfigurationModel> actionContext) {
		QueryReportConfigurationModel report = actionContext.getData();

		String query = report.getSearchQuery();
		Map<String, Object> params = dataReportService.getReportParameters(report);

		LOG.debug("Executing query {} with params {}", query, params);
		GenericSearchResult searchResult = flexibleSearchService.search(query, params);

		if (searchResult.hasError()) {
			return error(MessageFormat.format(actionContext.getLabel(SEARCH_ERROR), searchResult.getError()));
		}

		QueryFileConfigurationData queryFileConfigurationData = queryConfigurationConverter.convert(report);
		Optional<File> reportFile = dataReportService.getReportFile(queryFileConfigurationData, searchResult);
		if (!reportFile.isPresent()) {
			return error(actionContext.getLabel(REPORT_GENERATE_ERROR));
		}

		File media = reportFile.get();
		try {
			String extension = FilenameUtils.getExtension(media.getAbsolutePath());
			String filename = defaultIfBlank(report.getTitle(), report.getId()) + "." + extension;
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

	private ActionResult<Object> success() {
		return new ActionResult<>(ActionResult.SUCCESS);
	}

	private ActionResult<Object> error(String msg) {
		Messagebox.show(msg, "Error", Messagebox.OK, Messagebox.ERROR);

		ActionResult<Object> result = new ActionResult<>(ActionResult.ERROR);
		result.setResultMessage(msg);
		return result;
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
