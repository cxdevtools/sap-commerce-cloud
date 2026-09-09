package me.cxdev.commerce.forms.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.impl.AbstractPopulatingConverter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

import me.cxdev.commerce.forms.facade.DynamicFormFacade;
import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;
import me.cxdev.commerce.forms.model.DynamicFormModel;
import me.cxdev.commerce.forms.service.DynamicFormService;

/** Tests module wiring with the real recursive converter graph. */
@UnitTest
class DynamicFormsSpringConfigurationTest {
	@Test
	void shouldResolveRecursiveConverters() {
		final DynamicFormService service = mock(DynamicFormService.class);
		final DynamicFormModel form = mock(DynamicFormModel.class);
		final DynamicFormFieldModel field = mock(DynamicFormFieldModel.class);
		final DynamicFormFieldValueModel option = mock(DynamicFormFieldValueModel.class);
		final DynamicFormFieldModel child = mock(DynamicFormFieldModel.class);
		when(form.getId()).thenReturn("contact");
		when(form.getFormFields()).thenReturn(List.of(field));
		when(field.isActive()).thenReturn(true);
		when(field.getId()).thenReturn("reason");
		when(field.getFormFieldValues()).thenReturn(List.of(option));
		when(option.getId()).thenReturn("technical");
		when(option.getChildFields()).thenReturn(List.of(child));
		when(child.getId()).thenReturn("serial");
		when(service.getDynamicFormForId("contact")).thenReturn(Optional.of(form));

		try (GenericApplicationContext context = new GenericApplicationContext()) {
			context.getBeanFactory().registerSingleton("dynamicFormService", service);
			final RootBeanDefinition converter = new RootBeanDefinition(AbstractPopulatingConverter.class);
			converter.setAbstract(true);
			context.registerBeanDefinition("abstractPopulatingConverter", converter);
			new XmlBeanDefinitionReader(context).loadBeanDefinitions("classpath:cxdevforms/cxdevforms-facades-spring.xml");
			context.refresh();

			final DynamicFormFacade facade = context.getBean("cxDynamicFormFacade", DynamicFormFacade.class);
			final var result = facade.getDynamicFormForId("contact");
			assertEquals("serial", result.getFormFields().get(0).getFormFieldValues().get(0).getChildFields().get(0).getId());

		}
	}
}
