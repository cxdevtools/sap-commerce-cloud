package me.cxdev.commerce.proxy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for normalizing Spring resource paths across the proxy extension.
 * <p>
 * Ensures consistent handling of resource prefixes (like 'classpath:' and 'file:')
 * and mitigates common configuration mistakes (such as using 'classpath*:').
 * </p>
 */
public final class ResourcePathUtils {
	private static final Logger LOG = LoggerFactory.getLogger(ResourcePathUtils.class);

	private ResourcePathUtils() {
		// Prevent instantiation of utility class
	}

	/**
	 * Normalizes a file path (e.g., a Groovy script).
	 * Converts 'classpath*:' to 'classpath:' and auto-prepends 'classpath:' if no protocol is given.
	 *
	 * @param path        The raw path from properties.
	 * @param contextName A descriptive name for log warnings (e.g., "frontend rules").
	 * @return The normalized, ResourceLoader-compatible path.
	 */
	public static String normalizeFilePath(String path, String contextName) {
		if (path == null || path.trim().isEmpty()) {
			return path;
		}
		return applyPrefixFixes(path.trim(), contextName);
	}

	/**
	 * Normalizes a directory/base path (e.g., a UI folder).
	 * Removes trailing slashes for consistent concatenation, then applies prefix fixes.
	 *
	 * @param path        The raw directory path from properties.
	 * @param contextName A descriptive name for log warnings (e.g., "UI base location").
	 * @return The normalized, trailing-slash-free, ResourceLoader-compatible path.
	 */
	public static String normalizeDirectoryPath(String path, String contextName) {
		if (path == null || path.trim().isEmpty()) {
			return path;
		}
		String normalized = path.trim();
		if (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return applyPrefixFixes(normalized, contextName);
	}

	private static String applyPrefixFixes(String path, String contextName) {
		if (path.startsWith("classpath*:")) {
			LOG.warn("Invalid prefix 'classpath*:' detected for {} ({}). " +
					"Automatically correcting prefix to 'classpath:'.", contextName, path);
			return "classpath:" + path.substring("classpath*:".length());
		} else if (!path.startsWith("classpath:") && !path.startsWith("file:")) {
			return "classpath:" + path;
		}
		return path;
	}
}
