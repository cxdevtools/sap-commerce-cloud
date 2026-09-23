package me.cxdev.cpi.generic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.outboundservices.cache.RestTemplateCache;
import de.hybris.platform.outboundservices.client.IntegrationRestTemplateFactory;
import de.hybris.platform.outboundservices.client.impl.DefaultIntegrationNoCredentialRestTemplateCreator;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;

import me.cxdev.cpi.generic.outbound.AbstractScpiOutboundStrategy;
import me.cxdev.cpi.generic.remoteclient.ScpiRemoteClientFactory;

@UnitTest
public class CxCpiSpringConfigurationTest {

	@Test
	public void wiresFactoryAndConsumingStrategyThroughPublicAliases() {
		try (GenericApplicationContext context = createContext()) {
			context.registerBeanDefinition("testStrategy", BeanDefinitionBuilder.genericBeanDefinition(TestStrategy.class)
					.setParentName("abstractScpiOutboundStrategy")
					.addPropertyValue("destinationId", "testDestination").getBeanDefinition());
			context.refresh();

			TestStrategy strategy = context.getBean("testStrategy", TestStrategy.class);
			ScpiRemoteClientFactory factory = context.getBean("scpiRemoteClientFactory", ScpiRemoteClientFactory.class);
			assertSame(factory, context.getBean("cxScpiRemoteClientFactory"));
			assertSame(factory, strategy.getRemoteClientFactory());
			assertEquals("testDestination", strategy.getDestinationId());
			assertTrue(context.getBeanFactory().getMergedBeanDefinition("abstractScpiOutboundStrategy").isAbstract());
			assertSame(context.getBean("integrationRestTemplateFactory"), ReflectionTestUtils.getField(factory, "restTemplateFactory"));
		}
	}

	@Test
	public void configuresSapCreatorsWithTypedConversionAndPreservesInfrastructure() {
		try (GenericApplicationContext context = createContext()) {
			context.registerBeanDefinition("testCreator", BeanDefinitionBuilder
					.genericBeanDefinition(DefaultIntegrationNoCredentialRestTemplateCreator.class)
					.setParentName("abstractRestTemplateCreator").getBeanDefinition());
			context.refresh();

			Object creator = context.getBean("testCreator");
			List<?> converters = (List<?>) ReflectionTestUtils.getField(creator, "messageConverters");
			HttpMessageConverter<?> converter = (HttpMessageConverter<?>) converters.get(0);
			assertTrue(converter.canRead(TestResponse.class, MediaType.APPLICATION_JSON));
			assertTrue(converter.canRead(TestResponse.class, MediaType.APPLICATION_OCTET_STREAM));
			assertTrue(converter.canWrite(TestResponse.class, MediaType.APPLICATION_JSON));
			assertSame(context.getBean("outboundClientHttpRequestFactory"), ReflectionTestUtils.getField(creator, "clientHttpRequestFactory"));
			assertSame(context.getBean("outboundRestTemplateCache"), ReflectionTestUtils.getField(creator, "cache"));
			assertEquals(context.getBean("restTemplateHeaderInterceptors"), ReflectionTestUtils.getField(creator, "requestInterceptors"));
		}
	}

	private GenericApplicationContext createContext() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.getBeanFactory().registerSingleton("flexibleSearchService", mock(FlexibleSearchService.class));
		context.getBeanFactory().registerSingleton("integrationRestTemplateFactory", mock(IntegrationRestTemplateFactory.class));
		context.getBeanFactory().registerSingleton("outboundClientHttpRequestFactory", mock(ClientHttpRequestFactory.class));
		context.getBeanFactory().registerSingleton("outboundRestTemplateCache", mock(RestTemplateCache.class));
		context.getBeanFactory().registerSingleton("restTemplateHeaderInterceptors", List.of(mock(ClientHttpRequestInterceptor.class)));
		new XmlBeanDefinitionReader(context).loadBeanDefinitions(new ClassPathResource("cxdevcpi-spring.xml"));
		return context;
	}

	public static class TestStrategy extends AbstractScpiOutboundStrategy {
	}

	public static class TestResponse {
		public String id;
	}
}
