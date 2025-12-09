package tools.sapcx.commerce.sso.config;

import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.Test;

import tools.sapcx.commerce.toolkit.testing.verifier.InstalledExtensionVerifier;

@UnitTest
public class ExtensionConfigurationTests {
	@Test
	public void extensionConfiguration() {
		InstalledExtensionVerifier.verifier()
				.requires("sapcxsinglesignon")
				.requires("sapcommercetoolkit")
				.requires("commercewebservices")
				.requires("samlsinglesignon")
				.requires("oauth2")
				.verify();
	}
}
