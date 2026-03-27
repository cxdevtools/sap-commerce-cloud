package me.cxdev.commerce.proxy.livecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptor;
import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;

@ExtendWith(MockitoExtension.class)
class GroovyRuleEngineServiceTest {
	private GroovyRuleEngineService engineService;

	@Mock
	private ApplicationContext applicationContextMock;

	@Mock
	private ResourceLoader resourceLoaderMock;

	@Mock
	private Resource resourceMock;

	@Mock
	private ProxyExchangeInterceptor mockInterceptor;

	@Mock
	private ProxyExchangeInterceptorCondition mockCondition;

	@BeforeEach
	void setUp() {
		engineService = new GroovyRuleEngineService();
		engineService.setResourceLoader(resourceLoaderMock);

		// Setup Spring ApplicationContext mocks with custom bean names to test the prefix stripping
		Map<String, ProxyExchangeInterceptor> interceptors = new HashMap<>();
		interceptors.put("customInterceptorWithoutPrefix", mockInterceptor); // Edge case

		Map<String, ProxyExchangeInterceptorCondition> conditions = new HashMap<>();
		conditions.put("cxdevproxyConditionIsOcc", mockCondition);

		when(applicationContextMock.getBeansOfType(ProxyExchangeInterceptor.class)).thenReturn(interceptors);
		when(applicationContextMock.getBeansOfType(ProxyExchangeInterceptorCondition.class)).thenReturn(conditions);

		// This call triggers initGroovyShell() internally
		engineService.setApplicationContext(applicationContextMock);
	}

	// --- File Resolution Tests ---

	@Test
	void testResolveScriptFile_WithExistingResource_ReturnsFile() throws Exception {
		// Setup: Mock a valid classpath resource
		File mockFile = mock(File.class);
		when(resourceLoaderMock.getResource("classpath:my/script.groovy")).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(true);
		when(resourceMock.getFile()).thenReturn(mockFile);

		File resolvedFile = engineService.resolveScriptFile("my/script.groovy");

		assertNotNull(resolvedFile, "Should return a valid File object for existing resources");
		assertEquals(mockFile, resolvedFile);
	}

	@Test
	void testResolveScriptFile_WithMissingResource_ReturnsNull() {
		// Setup: Mock a resource that does not exist
		when(resourceLoaderMock.getResource(anyString())).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(false);

		File resolvedFile = engineService.resolveScriptFile("missing/script.groovy");

		assertNull(resolvedFile, "Should return null gracefully if the resource does not exist");
	}

	@Test
	void testResolveScriptFile_OnException_ReturnsNullGracefully() throws Exception {
		// Setup: Mock an IO exception during file retrieval (e.g., inside a JAR)
		when(resourceLoaderMock.getResource(anyString())).thenReturn(resourceMock);
		when(resourceMock.exists()).thenReturn(true);
		when(resourceMock.getFile()).thenThrow(new java.io.IOException("Inside JAR"));

		File resolvedFile = engineService.resolveScriptFile("jar/script.groovy");

		assertNull(resolvedFile, "Should swallow the exception and return null");
	}

	// --- Script Evaluation & Binding Tests ---

	@Test
	void testEvaluateScript_WithValidScriptAndBindings(@TempDir Path tempDir) throws Exception {
		// Setup: Create a real temporary file containing a Groovy script.
		// We use the variables exactly as they should be named after prefix-stripping.
		// We also test the static imports from Interceptors.class (e.g., jsonResponse).
		String groovyCode = "def interceptor1 = forwardedHeaders\n" +
				"def condition1 = isOcc\n" +
				"def fallback = customInterceptorWithoutPrefix\n" +
				"def inlineInterceptor = jsonResponse('{}')\n" +
				"return [interceptor1, inlineInterceptor]\n";

		File scriptFile = tempDir.resolve("rules.groovy").toFile();
		Files.writeString(scriptFile.toPath(), groovyCode);

		// Execution
		List<ProxyExchangeInterceptor> result = engineService.evaluateScript(scriptFile);

		// Assertions
		assertNotNull(result, "Result list should not be null");
		assertEquals(2, result.size(), "Script should return exactly two interceptors");

		// Assert that the first returned interceptor is the exact mock instance we injected
		assertEquals(mockInterceptor, result.get(0));
	}

	@Test
	void testEvaluateScript_ReturnsEmptyListOnWrongReturnType(@TempDir Path tempDir) throws Exception {
		// Setup: A script that returns a String instead of List<ProxyExchangeInterceptor>
		File scriptFile = tempDir.resolve("wrong_return.groovy").toFile();
		Files.writeString(scriptFile.toPath(), "return 'I am a string, not a list'");

		List<ProxyExchangeInterceptor> result = engineService.evaluateScript(scriptFile);

		assertNotNull(result);
		assertTrue(result.isEmpty(), "Should gracefully return an empty list if the script returns the wrong type");
	}

	@Test
	void testEvaluateScript_ReturnsEmptyListOnSyntaxError(@TempDir Path tempDir) throws Exception {
		// Setup: A script with invalid Groovy syntax
		File scriptFile = tempDir.resolve("syntax_error.groovy").toFile();
		Files.writeString(scriptFile.toPath(), "def invalid code structure {");

		List<ProxyExchangeInterceptor> result = engineService.evaluateScript(scriptFile);

		assertNotNull(result);
		assertTrue(result.isEmpty(), "Should swallow compilation exceptions and return an empty list");
	}

	@Test
	void testEvaluateScript_WithNullOrMissingFile_ReturnsEmptyList() {
		assertTrue(engineService.evaluateScript(null).isEmpty(), "Null file should return empty list");
		assertTrue(engineService.evaluateScript(new File("does_not_exist.groovy")).isEmpty(), "Missing file should return empty list");
	}
}
