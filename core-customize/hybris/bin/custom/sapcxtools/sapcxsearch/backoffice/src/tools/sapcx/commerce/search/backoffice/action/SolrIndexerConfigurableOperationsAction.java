package tools.sapcx.commerce.search.backoffice.action;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowContextParameterNames;

import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;

public class SolrIndexerConfigurableOperationsAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<SolrFacetSearchConfigModel, Object> {
	@Override
	public ActionResult<Object> perform(ActionContext<SolrFacetSearchConfigModel> actionContext) {
		Map<Object, Object> parametersMap = Maps.newHashMap();
		parametersMap.put(ConfigurableFlowContextParameterNames.TYPE_CODE.getName(), "SolrIndexerConfigurableOperationWizard");
		parametersMap.put("facetSearchConfig", actionContext.getData());
		this.sendOutput("operationWizard", parametersMap);
		return new ActionResult("success", (Object) null);
	}

	@Override
	public boolean canPerform(ActionContext<SolrFacetSearchConfigModel> ctx) {
		return CockpitAction.super.canPerform(ctx);
	}

	@Override
	public boolean needsConfirmation(ActionContext<SolrFacetSearchConfigModel> ctx) {
		return CockpitAction.super.needsConfirmation(ctx);
	}

	@Override
	public String getConfirmationMessage(ActionContext<SolrFacetSearchConfigModel> ctx) {
		return CockpitAction.super.getConfirmationMessage(ctx);
	}
}
