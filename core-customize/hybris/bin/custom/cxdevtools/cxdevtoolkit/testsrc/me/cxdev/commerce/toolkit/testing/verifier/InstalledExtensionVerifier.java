package me.cxdev.commerce.toolkit.testing.verifier;

import static org.apache.commons.collections4.SetUtils.emptyIfNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import de.hybris.bootstrap.config.ConfigUtil;
import de.hybris.bootstrap.config.ExtensionInfo;
import de.hybris.bootstrap.config.PlatformConfig;
import de.hybris.bootstrap.typesystem.OverridenItemsXml;
import de.hybris.bootstrap.typesystem.YTypeSystemLoader;
import de.hybris.bootstrap.typesystem.YTypeSystemSource;

public class InstalledExtensionVerifier extends YTypeSystemSource {
	public static InstalledExtensionVerifier verifier() {
		PlatformConfig platformConfig = ConfigUtil.getPlatformConfig(InstalledExtensionVerifier.class);
		return new InstalledExtensionVerifier(platformConfig);
	}

	private Set<String> requiredExtensions = new HashSet<>();
	private Set<String> prohibitedExtensions = new HashSet<>();

	private InstalledExtensionVerifier(PlatformConfig platformConfig) {
		super(
				platformConfig,
				new YTypeSystemLoader(true),
				OverridenItemsXml.empty());
	}

	public InstalledExtensionVerifier requires(String... requiredExtensions) {
		if (requiredExtensions == null) {
			return this;
		}
		return requires(Arrays.stream(requiredExtensions).collect(Collectors.toSet()));
	}

	public InstalledExtensionVerifier requires(Set<String> requiredExtensions) {
		this.requiredExtensions.addAll(emptyIfNull(requiredExtensions));
		return this;
	}

	public InstalledExtensionVerifier prohibits(String... prohibitedExtensions) {
		if (prohibitedExtensions == null) {
			return this;
		}
		return prohibits(Arrays.stream(prohibitedExtensions).collect(Collectors.toSet()));
	}

	public InstalledExtensionVerifier prohibits(Set<String> prohibitedExtensions) {
		this.prohibitedExtensions.addAll(emptyIfNull(prohibitedExtensions));
		return this;
	}

	public void verify() {
		Set<String> configuredExtensions = this.getExtensionCfgs().stream()
				.map(ExtensionInfo::getName)
				.collect(Collectors.toSet());

		if (!this.requiredExtensions.isEmpty()) {
			assertThat(configuredExtensions)
					.describedAs("Required extensions missing in configuration!")
					.containsAll(this.requiredExtensions);
		}

		if (!this.prohibitedExtensions.isEmpty()) {
			assertThat(configuredExtensions)
					.describedAs("Prohibited extensions found in configuration!")
					.doesNotContainAnyElementsOf(this.prohibitedExtensions);
		}
	}
}
