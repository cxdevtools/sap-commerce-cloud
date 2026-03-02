package me.cxdev.commerce.proxy.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import jakarta.ws.rs.core.MediaType;

public final class Interceptors {
	private static final int DEFAULT_STATUS_CODE = 200;

	public static ProxyExchangeInterceptor htmlResponse(String responseBody) {
		return htmlResponse(DEFAULT_STATUS_CODE, responseBody);
	}

	public static ProxyExchangeInterceptor htmlResponse(int statusCode, String responseBody) {
		return staticResponse(statusCode, MediaType.TEXT_HTML, responseBody);
	}

	public static ProxyExchangeInterceptor jsonResponse(String responseBody) {
		return jsonResponse(DEFAULT_STATUS_CODE, responseBody);
	}

	public static ProxyExchangeInterceptor jsonResponse(int statusCode, String responseBody) {
		return staticResponse(statusCode, MediaType.APPLICATION_JSON, responseBody);
	}

	public static ProxyExchangeInterceptor staticResponse(int statusCode, String contentType, String responseBody) {
		String contentTypeWithFallback = StringUtils.defaultIfBlank(contentType, MediaType.TEXT_PLAIN);
		String responseBodyWithFallback = StringUtils.defaultIfBlank(responseBody, "");
		return new StaticResponseInterceptor(statusCode, contentTypeWithFallback, responseBodyWithFallback);
	}

	public static ProxyExchangeInterceptor networkDelay(String delay) {
		return new NetworkDelayInterceptor(delay);
	}

	public static ProxyExchangeInterceptor networkDelay(String minDelay, String maxDelay) {
		return new NetworkDelayInterceptor(minDelay, maxDelay);
	}

	public static Builder interceptor() {
		return new Builder();
	}

	public static class Builder {
		private final List<ProxyExchangeInterceptorCondition> conditions = new ArrayList<>();
		private boolean requireAllConditions = true;

		public Builder constrainedBy(ProxyExchangeInterceptorCondition... conditions) {
			if (conditions != null) {
				this.conditions.addAll(Arrays.asList(conditions));
			}
			return this;
		}

		public Builder requireAll(boolean value) {
			this.requireAllConditions = value;
			return this;
		}

		public ProxyExchangeInterceptor perform(ProxyExchangeInterceptor... interceptor) {
			List<ProxyExchangeInterceptor> interceptorAsList = interceptor != null ? Arrays.asList(interceptor) : List.of();
			return new ProxyInterceptor(this.conditions, interceptorAsList, this.requireAllConditions);
		}

		private Builder() {
		}
	}
}
