package me.cxdev.commerce.reporting.backoffice.action;

import java.text.MessageFormat;
import java.util.Map;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zhtml.Messagebox;

import jakarta.annotation.Resource;
import me.cxdev.commerce.reporting.model.QueryReportConfigurationModel;
import me.cxdev.commerce.reporting.report.ReportService;
import me.cxdev.commerce.reporting.search.FlexibleSearchGenericSearchService;
import me.cxdev.commerce.reporting.search.GenericSearchResult;

public class ValidateReportAction implements CockpitAction<QueryReportConfigurationModel, Object> {
	private static final Logger LOG = LoggerFactory.getLogger(ValidateReportAction.class);
	private static final String SEARCH_SUCCESS = "validatereport.successful";
	private static final String SEARCH_ERROR = "validatereport.errors.query";

	@Resource(name = "cxGenericSearchService")
	private FlexibleSearchGenericSearchService flexibleSearchService;

	@Resource(name = "cxReportService")
	private ReportService dataReportService;

	@Override
	public ActionResult<Object> perform(ActionContext<QueryReportConfigurationModel> actionContext) {
		QueryReportConfigurationModel report = actionContext.getData();
		String query = report.getSearchQuery();
		Map<String, Object> params = dataReportService.getReportParameters(report);

		LOG.debug("Executing query {} with params {}", query, params);
		GenericSearchResult search = flexibleSearchService.search(query, params);

		if (search.hasError()) {
			return error(MessageFormat.format(actionContext.getLabel(SEARCH_ERROR), search.getError()));
		} else {
			return success(MessageFormat.format(actionContext.getLabel(SEARCH_SUCCESS), search.getValues().size()));
		}
	}

	private ActionResult<Object> success(String msg) {
		Messagebox.show(msg);
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
		return false;
	}
}
