package me.cxdev.commerce.toolkit.config;

import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.Test;

import me.cxdev.commerce.toolkit.testing.verifier.InstalledExtensionVerifier;

@UnitTest
public class ExtensionConfigurationTests {
	@Test
	public void extensionConfiguration() {
		InstalledExtensionVerifier.verifier()
				.requires("cxdevtoolkit")
				.verify();
	}
}
