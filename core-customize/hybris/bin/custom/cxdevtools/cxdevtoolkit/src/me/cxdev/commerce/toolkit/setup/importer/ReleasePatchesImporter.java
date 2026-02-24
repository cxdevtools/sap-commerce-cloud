package me.cxdev.commerce.toolkit.setup.importer;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hybris.platform.core.initialization.SystemSetupContext;

import org.apache.commons.lang3.StringUtils;

import me.cxdev.commerce.toolkit.setup.SystemSetupEnvironment;

/**
 * The {@link ReleasePatchesImporter} was created to apply minor changes to essential and initial data, that have
 * already been imported in the past. As we are not able to reinitialize the system later on, we need to update those
 * data by applying a release patch. Those patches are handled and imported by this imported.
 *
 * In addition to the {@link PrefixBasedDataImporter}, this class adds custom filter by overloading the
 * {@link #getKeyFilter(SystemSetupContext)} method.
 *
 * The filter works in three phases:
 * - During system initialization it does not import any patch as by convention all patches are also maintained and
 * merged back into the original impex files
 * - During system update if filters all key based on pattern matching. All keys contain a release number by convention
 * which the is sorted alphanumerical and all keys that are before the latest release version are skipped.
 * - With every execution (initialize or update) the importer stores the highest release number and persists it. It is
 * necessary that the file is stored on a shared filesystem that is available to all cluster nodes.
 *
 * @see PrefixBasedDataImporter
 * @see SystemSetupEnvironment
 */
public class ReleasePatchesImporter extends PrefixBasedDataImporter {
	private Pattern releaseVersion = Pattern.compile("^[^.]+\\.[^.]+\\.[^.]+\\.([^.]+)\\..*$");

	@Override
	public void importData(SystemSetupContext context) {
		super.importData(context);
		storeProcessedReleaseItemsInEnvironment();
	}

	private void storeProcessedReleaseItemsInEnvironment() {
		getEnvironment().getKeys(getPrefix()).stream()
				.sorted()
				.filter(getKeyFilterWithDefault(true))
				.filter(StringUtils::isNotBlank)
				.forEach(key -> {
					Matcher matcher = releaseVersion.matcher(key);
					if (matcher.matches()) {
						String releaseNumber = matcher.group(1);
						getEnvironment().addProcessedItem(releaseNumber, key);
					}
				});
	}

	@Override
	protected Predicate<String> getKeyFilter(SystemSetupContext context) {
		boolean shouldExecuteByDefault = !context.getProcess().isInit();
		return getKeyFilterWithDefault(shouldExecuteByDefault);
	}

	private Predicate<String> getKeyFilterWithDefault(boolean defaultValue) {
		return new NewerReleaseVersionsFilter(
				getEnvironment().getLastProcessedReleaseVersion(),
				getEnvironment().getLastProcessedItems(),
				defaultValue,
				releaseVersion);
	}

	@Override
	public void setPrefix(String prefix) {
		super.setPrefix(prefix);
		releaseVersion = Pattern.compile("^" + Pattern.quote(prefix) + "\\.([^.]+)\\..*$");
	}

	private static class NewerReleaseVersionsFilter implements Predicate<String> {
		private final String version;
		private List<String> processedItems;
		private final boolean defaultValue;
		private Pattern releaseVersion;

		public NewerReleaseVersionsFilter(String version, List<String> processedItems, boolean defaultValue, Pattern releaseVersion) {
			this.version = StringUtils.defaultIfBlank(version, "");
			this.processedItems = processedItems;
			this.defaultValue = defaultValue;
			this.releaseVersion = releaseVersion;
		}

		@Override
		public boolean test(String key) {
			Matcher matcher = releaseVersion.matcher(key);
			if (matcher.matches()) {
				String releaseNumber = matcher.group(1);
				int comparisonResult = releaseNumber.compareToIgnoreCase(version);
				boolean isOlderVersion = comparisonResult < 0;
				boolean isSameVersion = comparisonResult == 0;
				boolean wasProcessed = processedItems.contains(key);
				if (isOlderVersion || (isSameVersion && wasProcessed)) {
					return false;
				}
			}

			return defaultValue;
		}
	}
}
