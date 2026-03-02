package me.cxdev.commerce.proxy.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ResourcePathUtilsTest {
	private static final String CONTEXT = "test context";

	// --- File Path Normalization Tests ---

	@Test
	void testNormalizeFilePath_WithValidPrefixes_ReturnsAsIs() {
		assertEquals("classpath:my/script.groovy",
				ResourcePathUtils.normalizeFilePath("classpath:my/script.groovy", CONTEXT));
		assertEquals("file:/opt/my/script.groovy",
				ResourcePathUtils.normalizeFilePath("file:/opt/my/script.groovy", CONTEXT));
	}

	@Test
	void testNormalizeFilePath_WithClasspathStar_ReplacesPrefix() {
		assertEquals("classpath:my/script.groovy",
				ResourcePathUtils.normalizeFilePath("classpath*:my/script.groovy", CONTEXT),
				"Should automatically fix the invalid 'classpath*:' prefix");
	}

	@Test
	void testNormalizeFilePath_WithoutPrefix_PrependsClasspath() {
		assertEquals("classpath:my/script.groovy",
				ResourcePathUtils.normalizeFilePath("my/script.groovy", CONTEXT),
				"Should fallback to 'classpath:' if no protocol is provided");
	}

	@Test
	void testNormalizeFilePath_WithWhitespace_TrimsPath() {
		assertEquals("classpath:my/script.groovy",
				ResourcePathUtils.normalizeFilePath("  classpath:my/script.groovy  ", CONTEXT));
	}

	@Test
	void testNormalizeFilePath_NullOrEmpty_ReturnsAsIs() {
		assertNull(ResourcePathUtils.normalizeFilePath(null, CONTEXT));
		assertEquals("", ResourcePathUtils.normalizeFilePath("", CONTEXT));
		assertEquals("   ", ResourcePathUtils.normalizeFilePath("   ", CONTEXT),
				"Blank strings should be returned as is according to the implementation");
	}

	// --- Directory Path Normalization Tests ---

	@Test
	void testNormalizeDirectoryPath_RemovesTrailingSlash() {
		assertEquals("classpath:my/dir",
				ResourcePathUtils.normalizeDirectoryPath("classpath:my/dir/", CONTEXT));
		assertEquals("file:/opt/my/dir",
				ResourcePathUtils.normalizeDirectoryPath("file:/opt/my/dir/", CONTEXT));
	}

	@Test
	void testNormalizeDirectoryPath_WithoutTrailingSlash_RemainsUnchanged() {
		assertEquals("classpath:my/dir",
				ResourcePathUtils.normalizeDirectoryPath("classpath:my/dir", CONTEXT));
	}

	@Test
	void testNormalizeDirectoryPath_AppliesPrefixFixes() {
		// Testet die Kombination aus Trailing-Slash-Removal und Prefix-Fix
		assertEquals("classpath:my/dir",
				ResourcePathUtils.normalizeDirectoryPath("classpath*:my/dir/", CONTEXT));
		assertEquals("classpath:my/dir",
				ResourcePathUtils.normalizeDirectoryPath("my/dir/", CONTEXT));
		assertEquals("classpath:my/dir",
				ResourcePathUtils.normalizeDirectoryPath("  my/dir/  ", CONTEXT));
	}

	@Test
	void testNormalizeDirectoryPath_NullOrEmpty_ReturnsAsIs() {
		assertNull(ResourcePathUtils.normalizeDirectoryPath(null, CONTEXT));
		assertEquals("", ResourcePathUtils.normalizeDirectoryPath("", CONTEXT));
		assertEquals("   ", ResourcePathUtils.normalizeDirectoryPath("   ", CONTEXT));
	}
}
