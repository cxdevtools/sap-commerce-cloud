package me.cxdev.commerce.toolkit.email.fake;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.jobs.maintenance.MaintenanceCleanupStrategy;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

import org.joda.time.DateTime;

import me.cxdev.commerce.toolkit.model.LocallyStoredEmailModel;

public class RemoveLocallyHtmlEmailsMaintenanceCleanupStrategy implements MaintenanceCleanupStrategy<LocallyStoredEmailModel, CronJobModel> {
	private static final String FETCH_QUERY = String.format("SELECT {%s} FROM {%s} WHERE {%s} < ?threshold",
			LocallyStoredEmailModel.PK, LocallyStoredEmailModel._TYPECODE, LocallyStoredEmailModel.CREATIONTIME);

	private ModelService modelService;
	private int daysToKeepLocalEmails;

	public RemoveLocallyHtmlEmailsMaintenanceCleanupStrategy(ModelService modelService, int daysToKeepLocalEmails) {
		this.modelService = modelService;
		this.daysToKeepLocalEmails = daysToKeepLocalEmails;
	}

	@Override
	public FlexibleSearchQuery createFetchQuery(CronJobModel cjm) {
		FlexibleSearchQuery query = new FlexibleSearchQuery(FETCH_QUERY,
				Map.of("threshold", DateTime.now().minusDays(daysToKeepLocalEmails).toDate()));
		query.setResultClassList(Arrays.asList(LocallyStoredEmailModel.class));
		return query;
	}

	@Override
	public void process(List<LocallyStoredEmailModel> elements) {
		modelService.removeAll(emptyIfNull(elements));
	}
}
