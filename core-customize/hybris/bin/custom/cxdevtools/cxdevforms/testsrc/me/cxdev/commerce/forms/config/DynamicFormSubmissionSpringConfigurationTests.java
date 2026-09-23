package me.cxdev.commerce.forms.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import me.cxdev.commerce.forms.facade.DynamicFormSubmissionFacade;
import me.cxdev.commerce.forms.service.DynamicFormService;

@UnitTest
class DynamicFormSubmissionSpringConfigurationTests {
	@Test
	void wiresSubmissionGraphWithoutActivatingEmail() {
		try (var context = new GenericApplicationContext()) {
			context.getBeanFactory().registerSingleton("modelService", mock(ModelService.class));
			context.getBeanFactory().registerSingleton("userService", mock(UserService.class));
			context.getBeanFactory().registerSingleton("commonI18NService", mock(CommonI18NService.class));
			context.getBeanFactory().registerSingleton("eventService", mock(EventService.class));
			context.getBeanFactory().registerSingleton("baseSiteService", mock(BaseSiteService.class));
			context.getBeanFactory().registerSingleton("dynamicFormService", mock(DynamicFormService.class));
			context.getBeanFactory().registerSingleton("txManager", mock(PlatformTransactionManager.class));
			new XmlBeanDefinitionReader(context).loadBeanDefinitions("classpath:cxdevforms/cxdevforms-submissions-spring.xml");
			context.refresh();
			assertThat(context.getBean("dynamicFormSubmissionFacade")).isInstanceOf(DynamicFormSubmissionFacade.class);
			assertThat(context.containsBean("cxDynamicFormSubmissionEmailListener")).isFalse();
		}
	}
}
