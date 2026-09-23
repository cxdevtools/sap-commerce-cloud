package me.cxdev.cpi.generic.remoteclient;

import static org.apache.commons.collections4.MapUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import jakarta.annotation.Nonnull;

public class RestTemplateScpiRemoteClient implements ScpiRemoteClient {
	private static final String URL_QUERY_KEY = "urlQuery";
	private ConsumedDestinationModel destination;
	private RestOperations template;

	public ConsumedDestinationModel getDestination() {
		return this.destination;
	}

	RestTemplateScpiRemoteClient(@Nonnull ConsumedDestinationModel destination, @Nonnull RestOperations template) {
		this.destination = destination;
		this.template = template;
	}

	@Override
	public <T> T getForObject(Class<T> responseType, Object... uriVariables) throws RestClientException {
		return template.getForObject(getUrl(), responseType, uriVariables);
	}

	@Override
	public <T> T getForObject(Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
		return template.getForObject(getUrl(), responseType, uriVariables);
	}

	@Override
	public <T> T getForObject(Class<T> responseType) throws RestClientException {
		return template.getForObject(getUrl(), responseType);
	}

	@Override
	public <T> ResponseEntity<T> getForEntity(Class<T> responseType, Object... uriVariables) throws RestClientException {
		return template.getForEntity(getUrl(), responseType, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> getForEntity(Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
		return template.getForEntity(getUrl(), responseType, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> getForEntity(Class<T> responseType) throws RestClientException {
		return template.getForEntity(getUrl(), responseType);
	}

	@Override
	public HttpHeaders headForHeaders(Object... uriVariables) throws RestClientException {
		return template.headForHeaders(getUrl(), uriVariables);
	}

	@Override
	public HttpHeaders headForHeaders(Map<String, ?> uriVariables) throws RestClientException {
		return template.headForHeaders(getUrl(), uriVariables);
	}

	@Override
	public HttpHeaders headForHeaders() throws RestClientException {
		return template.headForHeaders(getUrl());
	}

	@Override
	public URI postForLocation(Object request, Object... uriVariables) throws RestClientException {
		return template.postForLocation(getUrl(), request, uriVariables);
	}

	@Override
	public URI postForLocation(Object request, Map<String, ?> uriVariables) throws RestClientException {
		return template.postForLocation(getUrl(), request, uriVariables);
	}

	@Override
	public URI postForLocation(Object request) throws RestClientException {
		return template.postForLocation(getUrl(), request);
	}

	@Override
	public <T> T postForObject(Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
		return template.postForObject(getUrl(), request, responseType, uriVariables);
	}

	@Override
	public <T> T postForObject(Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
		return template.postForObject(getUrl(), request, responseType, uriVariables);
	}

	@Override
	public <T> T postForObject(Object request, Class<T> responseType) throws RestClientException {
		return template.postForObject(getUrl(), request, responseType);
	}

	@Override
	public <T> ResponseEntity<T> postForEntity(Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
		return template.postForEntity(getUrl(), request, responseType, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> postForEntity(Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
		return template.postForEntity(getUrl(), request, responseType, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> postForEntity(Object request, Class<T> responseType) throws RestClientException {
		return template.postForEntity(getUrl(), request, responseType);
	}

	@Override
	public void put(Object request, Object... uriVariables) throws RestClientException {
		template.put(getUrl(), request, uriVariables);
	}

	@Override
	public void put(Object request, Map<String, ?> uriVariables) throws RestClientException {
		template.put(getUrl(), request, uriVariables);
	}

	@Override
	public void put(Object request) throws RestClientException {
		template.put(getUrl(), request);
	}

	@Override
	public <T> T patchForObject(Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
		return template.patchForObject(getUrl(), request, responseType, uriVariables);
	}

	@Override
	public <T> T patchForObject(Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
		return template.patchForObject(getUrl(), request, responseType, uriVariables);
	}

	@Override
	public <T> T patchForObject(Object request, Class<T> responseType) throws RestClientException {
		return template.patchForObject(getUrl(), request, responseType);
	}

	@Override
	public void delete(Object... uriVariables) throws RestClientException {
		template.delete(getUrl(), uriVariables);
	}

	@Override
	public void delete(Map<String, ?> uriVariables) throws RestClientException {
		template.delete(getUrl(), uriVariables);
	}

	@Override
	public void delete() throws RestClientException {
		template.delete(getUrl());
	}

	@Override
	public Set<HttpMethod> optionsForAllow(Object... uriVariables) throws RestClientException {
		return template.optionsForAllow(getUrl(), uriVariables);
	}

	@Override
	public Set<HttpMethod> optionsForAllow(Map<String, ?> uriVariables) throws RestClientException {
		return template.optionsForAllow(getUrl(), uriVariables);
	}

	@Override
	public Set<HttpMethod> optionsForAllow() throws RestClientException {
		return template.optionsForAllow(getUrl());
	}

	@Override
	public <T> ResponseEntity<T> exchange(HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) throws RestClientException {
		return template.exchange(getUrl(), method, requestEntity, responseType, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> exchange(HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
		return template.exchange(getUrl(), method, requestEntity, responseType, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> exchange(HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) throws RestClientException {
		return template.exchange(getUrl(), method, requestEntity, responseType);
	}

	@Override
	public <T> ResponseEntity<T> exchange(HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Object... uriVariables)
			throws RestClientException {
		return template.exchange(getUrl(), method, requestEntity, responseType, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> exchange(HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables)
			throws RestClientException {
		return template.exchange(getUrl(), method, requestEntity, responseType, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> exchange(HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType) throws RestClientException {
		return template.exchange(getUrl(), method, requestEntity, responseType);
	}

	@Override
	public <T> T execute(HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Object... uriVariables) throws RestClientException {
		return template.execute(getUrl(), method, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> T execute(HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Map<String, ?> uriVariables) throws RestClientException {
		return template.execute(getUrl(), method, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> T execute(HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
		return template.execute(getUrl(), method, requestCallback, responseExtractor);
	}

	private String getUrl() {
		String url = destination.getUrl();
		String query = defaultIfBlank(emptyIfNull(destination.getAdditionalProperties()).get(URL_QUERY_KEY), null);
		return (query == null) ? url : String.join("?", url, query);
	}
}
