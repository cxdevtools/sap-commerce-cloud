package me.cxdev.commerce.proxy.i18n;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@ExtendWith(MockitoExtension.class)
class ClasspathMergingMessageSourceTest {
	private ClasspathMergingMessageSource messageSource;

	@BeforeEach
	void setUp() {
		messageSource = new ClasspathMergingMessageSource();
		messageSource.setBaseName("cxdevproxy/i18n/messages");
		messageSource.setCacheRefreshIntervalMillis("0s");
	}

	private Resource createMockResource(String propertiesContent, File mockFile) throws IOException {
		Resource resource = mock(Resource.class);
		lenient().when(resource.getInputStream()).thenAnswer(inv -> new ByteArrayInputStream(propertiesContent.getBytes(StandardCharsets.ISO_8859_1)));

		if (mockFile != null) {
			lenient().when(resource.getFile()).thenReturn(mockFile);
		} else {
			lenient().when(resource.getFile()).thenThrow(new IOException("Not a file system resource"));
		}
		return resource;
	}

	@Test
	void testMergingMultipleResources() throws Exception {
		File file1 = mock(File.class);
		Resource res1 = createMockResource("key.one=value1\nkey.shared=from1", file1);

		File file2 = mock(File.class);
		Resource res2 = createMockResource("key.two=value2\nkey.shared=from2", file2);

		try (MockedConstruction<PathMatchingResourcePatternResolver> mocked = Mockito.mockConstruction(
				PathMatchingResourcePatternResolver.class,
				(mockResolver, context) -> {
					when(mockResolver.getResources(anyString())).thenReturn(new Resource[] { res1, res2 });
				})) {

			assertEquals("value1", messageSource.getMessage("key.one", null, "default", Locale.ENGLISH));
			assertEquals("value2", messageSource.getMessage("key.two", null, "default", Locale.ENGLISH));

			assertEquals("from2", messageSource.getMessage("key.shared", null, "default", Locale.ENGLISH));
		}
	}

	@Test
	void testHotReloadingOnModifiedFile() throws Exception {
		File mockFile = mock(File.class);
		when(mockFile.lastModified()).thenReturn(1000L); // Initiale "Zeit"

		String[] fileContent = new String[] { "dynamic.key=initialValue" };
		Resource res = mock(Resource.class);
		when(res.getInputStream()).thenAnswer(inv -> new ByteArrayInputStream(fileContent[0].getBytes(StandardCharsets.ISO_8859_1)));
		when(res.getFile()).thenReturn(mockFile);

		try (MockedConstruction<PathMatchingResourcePatternResolver> mocked = Mockito.mockConstruction(
				PathMatchingResourcePatternResolver.class,
				(mockResolver, context) -> {
					when(mockResolver.getResources(anyString())).thenReturn(new Resource[] { res });
				})) {

			assertEquals("initialValue", messageSource.getMessage("dynamic.key", null, "default", Locale.ENGLISH));

			fileContent[0] = "dynamic.key=updatedValue";
			when(mockFile.lastModified()).thenReturn(2000L);

			assertEquals("updatedValue", messageSource.getMessage("dynamic.key", null, "default", Locale.ENGLISH),
					"MessageSource should have detected the file change and reloaded properties");
		}
	}

	@Test
	void testCacheDebouncingInterval() throws Exception {
		messageSource.setCacheRefreshIntervalMillis("50ms");

		File mockFile = mock(File.class);
		when(mockFile.lastModified()).thenReturn(1000L);
		String[] fileContent = new String[] { "debounce.key=oldValue" };

		Resource res = createMockResource(fileContent[0], mockFile);
		when(res.getInputStream()).thenAnswer(inv -> new ByteArrayInputStream(fileContent[0].getBytes(StandardCharsets.ISO_8859_1)));

		try (MockedConstruction<PathMatchingResourcePatternResolver> mocked = Mockito.mockConstruction(
				PathMatchingResourcePatternResolver.class,
				(mockResolver, context) -> {
					when(mockResolver.getResources(anyString())).thenReturn(new Resource[] { res });
				})) {

			assertEquals("oldValue", messageSource.getMessage("debounce.key", null, "default", Locale.ENGLISH));

			fileContent[0] = "debounce.key=newValue";
			when(mockFile.lastModified()).thenReturn(2000L);

			assertEquals("oldValue", messageSource.getMessage("debounce.key", null, "default", Locale.ENGLISH));

			Thread.sleep(60);

			assertEquals("newValue", messageSource.getMessage("debounce.key", null, "default", Locale.ENGLISH));
		}
	}

	@Test
	void testResourceInsideJar_DoesNotCrash() throws Exception {
		Resource res = createMockResource("jar.key=jarValue", null);

		try (MockedConstruction<PathMatchingResourcePatternResolver> mocked = Mockito.mockConstruction(
				PathMatchingResourcePatternResolver.class,
				(mockResolver, context) -> {
					when(mockResolver.getResources(anyString())).thenReturn(new Resource[] { res });
				})) {

			assertEquals("jarValue", messageSource.getMessage("jar.key", null, "default", Locale.ENGLISH));
		}
	}

	@Test
	void testInvalidRefreshInterval_FallsBackGracefully() {
		messageSource.setCacheRefreshIntervalMillis("100ms");
		messageSource.setCacheRefreshIntervalMillis("invalid_format");
	}

	@Test
	void testResolveCode_ReturnsMessageFormatWithPlaceholders() throws Exception {
		Resource res = createMockResource("greeting.param=Hello, {0}! Welcome to {1}.", null);

		try (MockedConstruction<PathMatchingResourcePatternResolver> mocked = Mockito.mockConstruction(
				PathMatchingResourcePatternResolver.class,
				(mockResolver, context) -> {
					when(mockResolver.getResources(anyString())).thenReturn(new Resource[] { res });
				})) {

			MessageFormat format = messageSource.resolveCode("greeting.param", Locale.ENGLISH);

			assertNotNull(format, "MessageFormat should not be null for existing key");
			assertEquals("Hello, {0}! Welcome to {1}.", format.toPattern(), "The pattern should match the property value");
			assertEquals(Locale.ENGLISH, format.getLocale(), "The locale of the MessageFormat should match the requested locale");

			String formattedMessage = format.format(new Object[] { "John", "CX Dev Proxy" });
			assertEquals("Hello, John! Welcome to CX Dev Proxy.", formattedMessage);

			MessageFormat nullFormat = messageSource.resolveCode("unknown.key", Locale.ENGLISH);

			assertNull(nullFormat, "MessageFormat should be null for missing keys");
		}
	}
}
