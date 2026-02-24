package me.cxdev.commerce.reporting.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.internal.model.ServicelayerJobModel;

import org.junit.Before;
import org.junit.Test;

import me.cxdev.commerce.reporting.model.ReportGenerationScheduleModel;
import me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory;
import me.cxdev.commerce.toolkit.testing.testdoubles.core.InterceptorContextStub;
import me.cxdev.commerce.toolkit.testing.testdoubles.search.FlexibleSearchServiceStub;

@UnitTest
public class ReportGeneratorCronJobInitDefaultInterceptorTests {
	private InterceptorContext interceptorContext;
	private ReportGenerationScheduleInitDefaultInterceptor interceptor;

	@Before
	public void setUp() throws Exception {
		interceptorContext = InterceptorContextStub.interceptorContext().stub();
	}

	@Test
	public void ifJobIsNotFound_cronJobWillRemainWithoutAJob() throws InterceptorException {
		ReportGenerationScheduleModel cronJob = InMemoryModelFactory.createTestableItemModel(ReportGenerationScheduleModel.class);

		interceptor = new ReportGenerationScheduleInitDefaultInterceptor(new FlexibleSearchServiceStub(), "jobCode");
		interceptor.onInitDefaults(cronJob, interceptorContext);

		assertThat(cronJob.getJob()).isNull();
	}

	@Test
	public void emptyCronJobWillBeFilledWithJob() throws InterceptorException {
		ReportGenerationScheduleModel cronJob = InMemoryModelFactory.createTestableItemModel(ReportGenerationScheduleModel.class);
		ServicelayerJobModel job = InMemoryModelFactory.createTestableItemModel(ServicelayerJobModel.class);
		job.setCode("jobCode");

		interceptor = new ReportGenerationScheduleInitDefaultInterceptor(new FlexibleSearchServiceStub(job), "jobCode");
		interceptor.onInitDefaults(cronJob, interceptorContext);

		assertThat(cronJob.getJob()).isEqualTo(job);
	}

}
