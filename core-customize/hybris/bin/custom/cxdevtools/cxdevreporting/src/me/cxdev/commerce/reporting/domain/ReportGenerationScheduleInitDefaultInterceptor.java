package me.cxdev.commerce.reporting.domain;

import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.interceptor.InitDefaultsInterceptor;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.internal.model.ServicelayerJobModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.cxdev.commerce.reporting.model.ReportGenerationScheduleModel;

public class ReportGenerationScheduleInitDefaultInterceptor implements InitDefaultsInterceptor<ReportGenerationScheduleModel> {
	private static final Logger LOG = LoggerFactory.getLogger(ReportGenerationScheduleInitDefaultInterceptor.class);

	private final FlexibleSearchService flexibleSearchService;
	private final String jobCode;

	public ReportGenerationScheduleInitDefaultInterceptor(FlexibleSearchService flexibleSearchService, String jobCode) {
		this.flexibleSearchService = flexibleSearchService;
		this.jobCode = jobCode;
	}

	@Override
	public void onInitDefaults(ReportGenerationScheduleModel cronJob, InterceptorContext interceptorContext) {
		if (cronJob.getJob() == null) {
			cronJob.setJob(getServiceLayerJob());
		}
	}

	private ServicelayerJobModel getServiceLayerJob() {
		try {
			ServicelayerJobModel example = new ServicelayerJobModel();
			example.setCode(jobCode);
			return flexibleSearchService.getModelByExample(example);
		} catch (ModelNotFoundException | AmbiguousIdentifierException e) {
			LOG.error("Could not get report generator servicelayer job with code: " + jobCode + ". " +
					"Please verify your configuration and make sure the spring bean " +
					"of the job performable was set before running the system update.", e);
			return null;
		}
	}
}
