package me.cxdev.commerce.proxy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for parsing human-readable time intervals (e.g., "5s", "10m", "1h")
 * into milliseconds.
 */
public final class TimeUtils {
	private static final Logger LOG = LoggerFactory.getLogger(TimeUtils.class);

	private TimeUtils() {
		// Prevent instantiation
	}

	/**
	 * Parses a time interval string into milliseconds.
	 * Supports 'ms', 's', 'm', 'h', and 'd'. Falls back to milliseconds if no unit is provided.
	 *
	 * @param interval     The interval string from properties (e.g., "5s").
	 * @param defaultValue The fallback value in milliseconds if parsing fails or input is empty.
	 * @param contextName  A descriptive name for logging (e.g., "Groovy rule reload").
	 * @return The parsed interval in milliseconds.
	 */
	public static long parseIntervalToMillis(String interval, long defaultValue, String contextName) {
		if (interval == null || interval.trim().isEmpty()) {
			return defaultValue;
		}

		String trimmed = interval.trim().toLowerCase();
		long multiplier = 1;
		long value;

		try {
			if (trimmed.endsWith("ms")) {
				value = Long.parseLong(trimmed.substring(0, trimmed.length() - 2));
			} else if (trimmed.endsWith("s")) {
				value = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
				multiplier = 1000L;
			} else if (trimmed.endsWith("m")) {
				value = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
				multiplier = 60L * 1000L;
			} else if (trimmed.endsWith("h")) {
				value = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
				multiplier = 60L * 60L * 1000L;
			} else if (trimmed.endsWith("d")) {
				value = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
				multiplier = 24L * 60L * 60L * 1000L;
			} else {
				value = Long.parseLong(trimmed); // Default to ms if no unit
			}
			long result = value * multiplier;
			LOG.debug("Parsed time interval for '{}' to {} ms", contextName, result);
			return result;
		} catch (NumberFormatException e) {
			LOG.error("Invalid time format for {} ('{}'). Using default: {} ms", contextName, interval, defaultValue);
			return defaultValue;
		}
	}
}
