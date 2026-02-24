package me.cxdev.commerce.reporting.config;

import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.Test;

import me.cxdev.commerce.toolkit.testing.verifier.InstalledExtensionVerifier;

@UnitTest
public class ExtensionConfigurationTests {
	@Test
	public void extensionConfiguration() {
		InstalledExtensionVerifier.verifier()
				.requires("cxdevreporting")
				.requires("platformbackoffice")
				.requires("cxdevtoolkit")
				.verify();
	}
}
