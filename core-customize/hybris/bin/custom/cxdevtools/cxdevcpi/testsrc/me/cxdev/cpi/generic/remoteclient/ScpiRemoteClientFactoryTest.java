package me.cxdev.cpi.generic.remoteclient;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel;
import de.hybris.platform.outboundservices.client.IntegrationRestTemplateFactory;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.springframework.web.client.RestOperations;

@UnitTest
public class ScpiRemoteClientFactoryTest {

	private FlexibleSearchService searchService;
	private IntegrationRestTemplateFactory templateFactory;
	private RestOperations template;
	private ScpiRemoteClientFactory factory;
	private ConsumedDestinationModel destination;

	@Before
	public void setUp() {
		searchService = mock(FlexibleSearchService.class);
		templateFactory = mock(IntegrationRestTemplateFactory.class);
		template = mock(RestOperations.class);
		destination = mock(ConsumedDestinationModel.class);
		when(destination.getUrl()).thenReturn("https://cpi.example.invalid/http/documents");
		when(templateFactory.create(destination)).thenReturn(template);
		factory = new ScpiRemoteClientFactory(searchService, templateFactory);
	}

	@Test
	public void resolvesDestinationByIdAndUsesSapTemplateForTypedCalls() {
		when(searchService.getModelByExample(any(ConsumedDestinationModel.class))).thenReturn(destination);
		// Keep creation of the lookup example independent of the platform tenant.
		try (MockedConstruction<ConsumedDestinationModel> models = mockConstruction(ConsumedDestinationModel.class)) {
			RestTemplateScpiRemoteClient client = factory.clientForDestination("myDestination");
			client.getForObject(String.class);

			ArgumentCaptor<ConsumedDestinationModel> example = ArgumentCaptor.forClass(ConsumedDestinationModel.class);
			verify(searchService).getModelByExample(example.capture());
			assertSame(models.constructed().get(0), example.getValue());
			verify(example.getValue()).setId("myDestination");
			assertSame(destination, client.getDestination());
			verify(templateFactory).create(destination);
			verify(template).getForObject(destination.getUrl(), String.class);
		}
	}

	@Test
	public void acceptsResolvedDestinationWithoutAnotherLookup() {
		assertSame(destination, factory.clientForDestination(destination).getDestination());
		verifyNoInteractions(searchService);
		verify(templateFactory).create(destination);
	}

	@Test
	public void reportsLookupFailureWithoutCreatingTemplate() {
		when(searchService.getModelByExample(any(ConsumedDestinationModel.class)))
				.thenThrow(new ModelNotFoundException("Missing destination"));

		try (MockedConstruction<ConsumedDestinationModel> models = mockConstruction(ConsumedDestinationModel.class)) {
			assertThrows(ModelNotFoundException.class, () -> factory.clientForDestination("missing"));
			verifyNoInteractions(templateFactory);
		}
	}

	@Test
	public void propagatesTemplateCreationFailure() {
		IllegalArgumentException failure = new IllegalArgumentException("Unsupported credential");
		when(templateFactory.create(destination)).thenThrow(failure);

		assertSame(failure, assertThrows(IllegalArgumentException.class, () -> factory.clientForDestination(destination)));
	}
}
