package me.cxdev.commerce.proxy.i18n;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import me.cxdev.commerce.proxy.util.TimeUtils;

/**
 * A custom MessageSource that scans the entire classpath across all SAP Commerce extensions,
 * merges all matching property files, and automatically hot-reloads them if they are
 * modified on the local filesystem (exploded extensions).
 */
public class ClasspathMergingMessageSource extends AbstractMessageSource {
	private static final Logger LOG = LoggerFactory.getLogger(ClasspathMergingMessageSource.class);

	private String baseName = "cxdevproxy/i18n/messages";
	private long cacheRefreshIntervalMillis = 5000;

	private final ConcurrentHashMap<Locale, CachedBundle> cachedBundles = new ConcurrentHashMap<>();

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	/**
	 * Smart setter allowing human-readable time intervals like "5s", "10m", "1h", etc.
	 * Fallback to milliseconds if no unit is provided.
	 *
	 * @param interval The interval string from Spring properties.
	 */
	public void setCacheRefreshIntervalMillis(String interval) {
		this.cacheRefreshIntervalMillis = TimeUtils.parseIntervalToMillis(interval, 5000L, "Message cache refresh interval");
	}

	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		String format = resolveCodeWithoutArguments(code, locale);
		return format != null ? new MessageFormat(format, locale) : null;
	}

	@Override
	protected String resolveCodeWithoutArguments(String code, Locale locale) {
		CachedBundle bundle = cachedBundles.compute(locale, (loc, currentBundle) -> {
			if (currentBundle == null || currentBundle.isStale(cacheRefreshIntervalMillis)) {
				if (currentBundle != null) {
					LOG.info("Detected change in message files for locale '{}'. Reloading merged bundles...", loc.getLanguage());
				}
				return loadMergedProperties(loc);
			}
			return currentBundle;
		});

		return bundle.getProperties().getProperty(code);
	}

	private CachedBundle loadMergedProperties(Locale locale) {
		String resourcePattern = "classpath*:" + baseName + "_" + locale.getLanguage() + ".properties";

		Properties mergedProps = new Properties();
		List<WatchedFile> watchedFiles = new ArrayList<>();

		try {
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
			Resource[] resources = resolver.getResources(resourcePattern);

			for (Resource resource : resources) {
				try {
					Properties p = PropertiesLoaderUtils.loadProperties(resource);
					mergedProps.putAll(p);

					try {
						File file = resource.getFile();
						watchedFiles.add(new WatchedFile(file, file.lastModified()));
						LOG.debug("Watching message file for changes: {}", file.getAbsolutePath());
					} catch (IOException e) {
						LOG.debug("Resource is not a file on the filesystem (likely in a JAR). Not watching: {}", resource.getURI());
					}
				} catch (IOException e) {
					LOG.warn("Could not load properties from resource: {}", resource, e);
				}
			}
		} catch (IOException e) {
			LOG.error("Failed to resolve message bundle pattern: {}", resourcePattern, e);
		}

		return new CachedBundle(mergedProps, watchedFiles);
	}

	private static class CachedBundle {
		private final Properties properties;
		private final List<WatchedFile> watchedFiles;
		private long lastCheckTime;

		CachedBundle(Properties properties, List<WatchedFile> watchedFiles) {
			this.properties = properties;
			this.watchedFiles = watchedFiles;
			this.lastCheckTime = System.currentTimeMillis();
		}

		Properties getProperties() {
			return properties;
		}

		boolean isStale(long debounceMillis) {
			long now = System.currentTimeMillis();
			if (now - lastCheckTime < debounceMillis) {
				return false;
			}
			this.lastCheckTime = now;

			for (WatchedFile watchedFile : watchedFiles) {
				if (watchedFile.hasChanged()) {
					return true;
				}
			}
			return false;
		}
	}

	private static class WatchedFile {
		private final File file;
		private final long lastModifiedAtLoad;

		WatchedFile(File file, long lastModifiedAtLoad) {
			this.file = file;
			this.lastModifiedAtLoad = lastModifiedAtLoad;
		}

		boolean hasChanged() {
			return file.lastModified() > lastModifiedAtLoad;
		}
	}
}
