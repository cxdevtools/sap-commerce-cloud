package tools.sapcx.commerce.search.backoffice.wizard;

import java.util.*;

import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;

import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.solrfacetsearch.enums.IndexerOperationValues;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.model.indexer.cron.SolrIndexerCronJobModel;
import de.hybris.platform.solrfacetsearchbackoffice.wizards.BaseSolrIndexerWizardStep;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.*;

import tools.sapcx.commerce.search.model.SolrIndexerConfigurableCronJobModel;

public class SolrIndexerConfigurableOperationStep extends BaseSolrIndexerWizardStep {
	private CronJobService cronJobService;
	private String jobDefinitionCode;

	@Override
	public void render(Component component, ViewType viewType, Map<String, String> map, DataType dataType, WidgetInstanceManager widgetInstanceManager) {
		this.setWidgetController(widgetInstanceManager);
		this.initCustomView(component);
	}

	private void initCustomView(final Component component) {
		// Create Components
		Combobox operationsCombo = initOperationsCombo();
		Combobox indexedTypesCombo = initIndexedTypesCombo(component);
		Button startButton = initStartButton(component, operationsCombo, indexedTypesCombo);

		// Layout
		Vlayout vlayout = new Vlayout();

		// Second Row
		Div firstRow = new Div();
		vlayout.appendChild(firstRow);
		firstRow.appendChild(new Label(this.getLabelService().getObjectLabel("IndexerOperationValues")));
		firstRow.appendChild(operationsCombo);

		// Seperator
		Separator separator = new Separator();
		separator.setBar(true);
		vlayout.appendChild(separator);

		// Third Row
		Div secondRow = new Div();
		vlayout.appendChild(secondRow);
		secondRow.appendChild(new Label(this.getLabelService().getObjectLabel("SolrIndexedType")));
		secondRow.appendChild(indexedTypesCombo);

		Div forthRow = new Div();
		vlayout.appendChild(forthRow);
		forthRow.appendChild(startButton);
		component.appendChild(vlayout);
	}

	private Button initStartButton(Component component, Combobox operationsCombo, Combobox indexedTypesCombo) {
		Button startButton = new Button();
		startButton.setLabel(Labels.getLabel("com.hybris.cockpitng.widgets.configurableflow.create.solrindexer.cronjob.start"));
		startButton.addEventListener("onClick", event -> {
			SolrIndexerConfigurableCronJobModel cronJob = SolrIndexerConfigurableOperationStep.this.getCronJob(component);
			cronJob.setIndexerOperation(operationsCombo.getSelectedItem().getValue());
			cronJob.setIndexedTypes(List.of(indexedTypesCombo.getSelectedItem().getValue()));
			SolrIndexerConfigurableOperationStep.this.startCronJob(SolrIndexerConfigurableOperationStep.this.getWidgetController(), cronJob);
		});
		return startButton;
	}

	private Combobox initIndexedTypesCombo(Component component) {
		final Combobox indexedTypesCombo = new Combobox();
		SolrIndexerCronJobModel solrIndexerCronJobModel = getCronJob(component);
		indexedTypesCombo.setModel(new ListModelList<>(solrIndexerCronJobModel.getFacetSearchConfig().getSolrIndexedTypes()));
		indexedTypesCombo.setItemRenderer((ComboitemRenderer<SolrIndexedTypeModel>) (comboItem, solrIndexedType, index) -> {
			comboItem.setLabel(solrIndexedType.getType().getName());
			comboItem.setValue(solrIndexedType);
		});
		return indexedTypesCombo;
	}

	private Combobox initOperationsCombo() {
		final Combobox operationsCombo = new Combobox();
		operationsCombo.setModel(new ListModelList<>(IndexerOperationValues.values()));
		operationsCombo.setItemRenderer((radio, entity, index) -> {
			IndexerOperationValues indexerOperation = (IndexerOperationValues) entity;
			radio.setLabel(SolrIndexerConfigurableOperationStep.this.getLabelService().getObjectLabel(indexerOperation));
			radio.setValue(entity);
			if (IndexerOperationValues.PARTIAL_UPDATE.equals(indexerOperation)) {
				radio.setDisabled(true);
				radio.setVisible(false);
			}
		});
		return operationsCombo;
	}

	private SolrIndexerConfigurableCronJobModel getCronJob(Component component) {
		SolrIndexerConfigurableCronJobModel job = this.getCurrentObject(component, SolrIndexerConfigurableCronJobModel.class);
		job.setJob(cronJobService.getJob(jobDefinitionCode));
		return job;
	}

	public void setCronJobService(CronJobService cronJobService) {
		super.setCronJobService(cronJobService);
		this.cronJobService = cronJobService;
	}

	public void setJobDefinitionCode(String jobDefinitionCode) {
		this.jobDefinitionCode = jobDefinitionCode;
	}
}
