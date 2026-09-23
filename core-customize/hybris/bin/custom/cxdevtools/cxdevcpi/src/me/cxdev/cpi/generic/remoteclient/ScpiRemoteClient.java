package me.cxdev.cpi.generic.remoteclient;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;

/**
 * Abstraction of the {@link org.springframework.web.client.RestOperations} interface,
 * specifying a basic set of RESTful operations. Implemented by RestTemplate.
 *
 * Uses the destination URL and the SAP outbound RestTemplate configuration for
 * authentication, message conversion and request interceptors. Outbound facade
 * decorators (including creation of monitoring records) are not invoked here.
 */
public interface ScpiRemoteClient {
	@Nullable
	<T> T getForObject(Class<T> responseType, Object... uriVariables) throws RestClientException;

	@Nullable
	<T> T getForObject(Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException;

	@Nullable
	<T> T getForObject(Class<T> responseType) throws RestClientException;

	<T> ResponseEntity<T> getForEntity(Class<T> responseType, Object... uriVariables) throws RestClientException;

	<T> ResponseEntity<T> getForEntity(Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException;

	<T> ResponseEntity<T> getForEntity(Class<T> responseType) throws RestClientException;

	HttpHeaders headForHeaders(Object... uriVariables) throws RestClientException;

	HttpHeaders headForHeaders(Map<String, ?> uriVariables) throws RestClientException;

	HttpHeaders headForHeaders() throws RestClientException;

	@Nullable
	URI postForLocation(@Nullable Object request, Object... uriVariables) throws RestClientException;

	@Nullable
	URI postForLocation(@Nullable Object request, Map<String, ?> uriVariables) throws RestClientException;

	@Nullable
	URI postForLocation(@Nullable Object request) throws RestClientException;

	@Nullable
	<T> T postForObject(@Nullable Object request, Class<T> responseType, Object... uriVariables) throws RestClientException;

	@Nullable
	<T> T postForObject(@Nullable Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException;

	@Nullable
	<T> T postForObject(@Nullable Object request, Class<T> responseType) throws RestClientException;

	<T> ResponseEntity<T> postForEntity(@Nullable Object request, Class<T> responseType, Object... uriVariables) throws RestClientException;

	<T> ResponseEntity<T> postForEntity(@Nullable Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException;

	<T> ResponseEntity<T> postForEntity(@Nullable Object request, Class<T> responseType) throws RestClientException;

	void put(@Nullable Object request, Object... uriVariables) throws RestClientException;

	void put(@Nullable Object request, Map<String, ?> uriVariables) throws RestClientException;

	void put(@Nullable Object request) throws RestClientException;

	@Nullable
	<T> T patchForObject(@Nullable Object request, Class<T> responseType, Object... uriVariables) throws RestClientException;

	@Nullable
	<T> T patchForObject(@Nullable Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException;

	@Nullable
	<T> T patchForObject(@Nullable Object request, Class<T> responseType) throws RestClientException;

	void delete(Object... uriVariables) throws RestClientException;

	void delete(Map<String, ?> uriVariables) throws RestClientException;

	void delete() throws RestClientException;

	Set<HttpMethod> optionsForAllow(Object... uriVariables) throws RestClientException;

	Set<HttpMethod> optionsForAllow(Map<String, ?> uriVariables) throws RestClientException;

	Set<HttpMethod> optionsForAllow() throws RestClientException;

	<T> ResponseEntity<T> exchange(HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) throws RestClientException;

	<T> ResponseEntity<T> exchange(HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException;

	<T> ResponseEntity<T> exchange(HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType) throws RestClientException;

	<T> ResponseEntity<T> exchange(HttpMethod method, @Nullable HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Object... uriVariables)
			throws RestClientException;

	<T> ResponseEntity<T> exchange(HttpMethod method, @Nullable HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables)
			throws RestClientException;

	<T> ResponseEntity<T> exchange(HttpMethod method, @Nullable HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType) throws RestClientException;

	@Nullable
	<T> T execute(HttpMethod method, @Nullable RequestCallback requestCallback, @Nullable ResponseExtractor<T> responseExtractor, Object... uriVariables)
			throws RestClientException;

	@Nullable
	<T> T execute(HttpMethod method, @Nullable RequestCallback requestCallback, @Nullable ResponseExtractor<T> responseExtractor, Map<String, ?> uriVariables)
			throws RestClientException;

	@Nullable
	<T> T execute(HttpMethod method, @Nullable RequestCallback requestCallback, @Nullable ResponseExtractor<T> responseExtractor) throws RestClientException;
}
