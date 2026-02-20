package tools.sapcx.commerce.reporting.backoffice.action;

import java.text.MessageFormat;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.sapcx.commerce.reporting.model.QueryReportConfigurationModel;
import tools.sapcx.commerce.reporting.search.GenericSearchResult;

public class ValidateReportAction extends AbstractReportAction {
	private static final Logger LOG = LoggerFactory.getLogger(ValidateReportAction.class);
	private static final String SEARCH_SUCCESS = "validatereport.successful";
	private static final String SEARCH_ERROR = "validatereport.errors.query";

	@Override
	protected ActionResult<Object> processSearchResult(ActionContext<QueryReportConfigurationModel> actionContext, GenericSearchResult searchResult) {
		if (searchResult.hasError()) {
			return error(MessageFormat.format(actionContext.getLabel(SEARCH_ERROR), searchResult.getError()));
		} else {
			return success(MessageFormat.format(actionContext.getLabel(SEARCH_SUCCESS), searchResult.getValues().size()));
		}
	}
}
