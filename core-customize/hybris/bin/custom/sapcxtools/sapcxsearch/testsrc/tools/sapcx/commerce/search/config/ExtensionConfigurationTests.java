package tools.sapcx.commerce.search.config;

import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.Test;

import tools.sapcx.commerce.toolkit.testing.verifier.InstalledExtensionVerifier;

@UnitTest
public class ExtensionConfigurationTests {
	@Test
	public void extensionConfiguration() {
		InstalledExtensionVerifier.verifier()
				.requires("sapcommercetoolkit")
				.requires("backoffice")
				.requires("solrfacetsearch")
				.requires("solrfacetsearchbackoffice")
				.verify();
	}
}
