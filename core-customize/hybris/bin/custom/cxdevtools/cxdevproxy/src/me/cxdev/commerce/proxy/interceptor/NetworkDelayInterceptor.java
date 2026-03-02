package me.cxdev.commerce.proxy.interceptor;

import java.util.concurrent.ThreadLocalRandom;

import io.undertow.server.HttpServerExchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.cxdev.commerce.proxy.util.TimeUtils;

/**
 * Artificially delays the request processing to simulate network latency
 * or a slow backend environment. Perfect for testing frontend loading states.
 * <p>
 * Supports a randomized delay between a configured minimum and maximum value.
 * Note: Uses Thread.sleep() which blocks the current worker thread.
 * </p>
 */
class NetworkDelayInterceptor implements ProxyExchangeInterceptor {
	private static final Logger LOG = LoggerFactory.getLogger(NetworkDelayInterceptor.class);

	private long minDelayInMillis;
	private long maxDelayInMillis;

	/**
	 * Convenience constructor to assign a fixed delay (sets both min and max to the same value).
	 */
	NetworkDelayInterceptor(String delay) {
		this.minDelayInMillis = TimeUtils.parseIntervalToMillis(delay, "Network delay interceptor interval");
		this.maxDelayInMillis = this.minDelayInMillis;
	}

	NetworkDelayInterceptor(String minDelay, String maxDelay) {
		this.minDelayInMillis = TimeUtils.parseIntervalToMillis(minDelay, "Network minimum delay interceptor interval");
		;
		this.maxDelayInMillis = TimeUtils.parseIntervalToMillis(maxDelay, "Network maximum delay interceptor interval");
		;
	}

	@Override
	public void apply(HttpServerExchange exchange) {
		long actualDelay = calculateDelay();

		if (actualDelay > 0) {
			try {
				LOG.debug("Simulating network delay of {} ms for request: {}", actualDelay, exchange.getRequestPath());
				Thread.sleep(actualDelay);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOG.warn("Network delay simulation was interrupted", e);
			}
		}
	}

	/**
	 * Calculates the delay to be applied.
	 * Returns a random value between min and max (inclusive) if they differ.
	 */
	private long calculateDelay() {
		if (minDelayInMillis == maxDelayInMillis) {
			return minDelayInMillis;
		}
		if (minDelayInMillis > maxDelayInMillis) {
			LOG.warn("minDelayInMillis ({}) is greater than maxDelayInMillis ({}). Using minDelay.", minDelayInMillis, maxDelayInMillis);
			return minDelayInMillis;
		}
		return ThreadLocalRandom.current().nextLong(minDelayInMillis, maxDelayInMillis + 1);
	}
}
