package me.cxdev.cpi.generic.remoteclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.util.List;
import java.util.Map;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@UnitTest
public class RestTemplateScpiRemoteClientTest {

	private static final String URL = "https://cpi.example.invalid/http/documents";

	private ConsumedDestinationModel destination;
	private ScpiRemoteClient client;
	private MockRestServiceServer server;

	@Before
	public void setUp() {
		destination = mock(ConsumedDestinationModel.class);
		when(destination.getUrl()).thenReturn(URL);
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
		RestTemplate template = new RestTemplate(List.of(converter));
		server = MockRestServiceServer.bindTo(template).build();
		client = new RestTemplateScpiRemoteClient(destination, template);
	}

	@Test
	public void mapsNestedResponseAndSerializesRequestWithUriVariables() {
		when(destination.getUrl()).thenReturn(URL + "/{id}");
		when(destination.getAdditionalProperties()).thenReturn(Map.of("urlQuery", "language={language}"));
		server.expect(requestTo(URL + "/42?language=en"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().string("{\"filter\":\"open\"}"))
				.andRespond(withSuccess("{\"items\":[{\"id\":\"42\"}]}", MediaType.APPLICATION_JSON));

		Envelope result = client.postForEntity(Map.of("filter", "open"), Envelope.class,
				Map.of("id", "42", "language", "en")).getBody();

		assertEquals("42", result.items.get(0).id);
		server.verify();
	}

	@Test
	public void mapsGenericListFromJsonServedAsOctetStream() {
		server.expect(requestTo(URL)).andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("[{\"id\":\"42\"}]", MediaType.APPLICATION_OCTET_STREAM));

		List<Document> result = client.exchange(HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Document>>() {
				}).getBody();

		assertEquals("42", result.get(0).id);
		server.verify();
	}

	@Test
	public void omitsQuerySeparatorWhenQueryIsAbsentOrBlank() {
		for (Map<String, String> properties : List.of(Map.<String, String>of(), Map.of("urlQuery", ""), Map.of("urlQuery", "  "))) {
			when(destination.getAdditionalProperties()).thenReturn(properties);
			server.expect(requestTo(URL)).andRespond(withSuccess("{\"id\":\"42\"}", MediaType.APPLICATION_JSON));
			assertEquals("42", client.getForObject(Document.class).id);
			server.verify();
			server.reset();
		}
	}

	@Test
	public void forwardsRequestBodyAndPositionalVariablesForLocation() {
		when(destination.getUrl()).thenReturn(URL + "/{id}");
		expectCreation(URL + "/42");

		assertEquals(URI.create(URL + "/created"), client.postForLocation(Map.of("name", "test"), "42"));
		server.verify();
	}

	@Test
	public void forwardsRequestBodyAndNamedVariablesForLocation() {
		when(destination.getUrl()).thenReturn(URL + "/{id}");
		expectCreation(URL + "/42");

		assertEquals(URI.create(URL + "/created"), client.postForLocation(Map.of("name", "test"), Map.of("id", "42")));
		server.verify();
	}

	@Test
	public void forwardsRequestBodyForLocationWithoutVariables() {
		expectCreation(URL);

		assertEquals(URI.create(URL + "/created"), client.postForLocation(Map.of("name", "test")));
		server.verify();
	}

	@Test
	public void propagatesHttpErrorsToTheConsumingStrategy() {
		server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

		assertThrows(HttpClientErrorException.BadRequest.class, () -> client.getForObject(Document.class));
		server.verify();
	}

	private void expectCreation(String url) {
		server.expect(requestTo(url)).andExpect(method(HttpMethod.POST))
				.andExpect(content().string("{\"name\":\"test\"}"))
				.andRespond(withStatus(HttpStatus.CREATED).location(URI.create(URL + "/created")));
	}

	public static class Envelope {
		public List<Document> items;
	}

	public static class Document {
		public String id;
	}
}
