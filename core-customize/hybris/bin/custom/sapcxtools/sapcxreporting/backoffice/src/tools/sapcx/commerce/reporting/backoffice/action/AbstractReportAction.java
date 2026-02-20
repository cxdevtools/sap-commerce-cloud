package tools.sapcx.commerce.reporting.backoffice.action;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zhtml.Messagebox;
import tools.sapcx.commerce.reporting.enums.SearchQueryType;
import tools.sapcx.commerce.reporting.model.QueryReportConfigurationModel;
import tools.sapcx.commerce.reporting.report.ReportService;
import tools.sapcx.commerce.reporting.search.GenericSearchResult;
import tools.sapcx.commerce.reporting.search.GenericSearchService;

import java.text.MessageFormat;
import java.util.Map;

public abstract class AbstractReportAction implements CockpitAction<QueryReportConfigurationModel, Object> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractReportAction.class);
    private static final String WRONG_QUERYTYPE = "report.errors.querytype";

    @Resource(name = "cxGenericSearchServicesMap")
    protected Map<SearchQueryType, GenericSearchService> genericSearchServices;

    @Resource(name = "cxReportService")
    protected ReportService dataReportService;

    @Override
    public boolean needsConfirmation(ActionContext<QueryReportConfigurationModel> ctx) {
        return false;
    }

    @Override
    public ActionResult<Object> perform(ActionContext<QueryReportConfigurationModel> actionContext) {
        final QueryReportConfigurationModel report = actionContext.getData();
        if (!genericSearchServices.containsKey(report.getSearchQueryType())) {
            return error(MessageFormat.format(actionContext.getLabel(WRONG_QUERYTYPE), report.getSearchQueryType()));
        }

        final GenericSearchService genericSearchService = genericSearchServices.get(report.getSearchQueryType());
        final String query = report.getSearchQuery();
        final Map<String, Object> params = dataReportService.getReportParameters(report);
        final Map<String, Object> configuration = dataReportService.getReportConfiguration(report);

        LOG.debug("Processing query {} with params {} and config {}", query, params, configuration);
        final GenericSearchResult searchResult = genericSearchService.search(query, params, configuration);
        return processSearchResult(actionContext, searchResult);
    }

    protected abstract ActionResult<Object> processSearchResult(ActionContext<QueryReportConfigurationModel> actionContext, GenericSearchResult searchResult);

    protected ActionResult<Object> error() {
        return error(null);
    }

    protected ActionResult<Object> error(String msg) {
        if (msg != null) {
            Messagebox.show(msg, "Error", Messagebox.OK, Messagebox.ERROR);
        }
        final ActionResult<Object> result = new ActionResult<>(ActionResult.ERROR);
        result.setResultMessage(msg);
        return result;
    }

    protected ActionResult<Object> success() {
        return success(null);
    }

    protected ActionResult<Object> success(String msg) {
        if (msg != null) {
            Messagebox.show(msg);
        }
        return new ActionResult<>(ActionResult.SUCCESS);
    }
}
