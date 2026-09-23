package me.cxdev.cpi.generic.remoteclient;

import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel;
import de.hybris.platform.outboundservices.client.IntegrationRestTemplateFactory;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import jakarta.validation.constraints.NotNull;

public class ScpiRemoteClientFactory {
	private static final Logger LOG = LoggerFactory.getLogger(ScpiRemoteClientFactory.class);

	private FlexibleSearchService flexibleSearchService;
	private IntegrationRestTemplateFactory restTemplateFactory;

	public ScpiRemoteClientFactory(FlexibleSearchService flexibleSearchService, IntegrationRestTemplateFactory restTemplateFactory) {
		this.flexibleSearchService = flexibleSearchService;
		this.restTemplateFactory = restTemplateFactory;
	}

	public RestTemplateScpiRemoteClient clientForDestination(@NotNull String destinationId) {
		ConsumedDestinationModel destination = getConsumedDestination(destinationId);
		return clientForDestination(destination);
	}

	public RestTemplateScpiRemoteClient clientForDestination(@NotNull ConsumedDestinationModel destination) {
		RestOperations template = restTemplateFactory.create(destination);
		return new RestTemplateScpiRemoteClient(destination, template);
	}

	private ConsumedDestinationModel getConsumedDestination(String destinationId) {
		try {
			ConsumedDestinationModel example = new ConsumedDestinationModel();
			example.setId(destinationId);
			return flexibleSearchService.getModelByExample(example);
		} catch (RuntimeException e) {
			LOG.warn("Failed to find ConsumedDestination with id '{}'", destinationId, e);
			throw new ModelNotFoundException("Provided destination was not found.");
		}
	}
}
