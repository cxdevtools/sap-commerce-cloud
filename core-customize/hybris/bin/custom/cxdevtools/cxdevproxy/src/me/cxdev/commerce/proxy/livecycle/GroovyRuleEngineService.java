package me.cxdev.commerce.proxy.livecycle;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptor;
import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptorCondition;
import me.cxdev.commerce.proxy.interceptor.condition.Conditions;

/**
 * Core service responsible for compiling, evaluating, and hot-reloading Groovy DSL scripts.
 * <p>
 * This engine acts as the bridge between the Spring ApplicationContext and the dynamic
 * Undertow proxy routing. It initializes a {@link groovy.lang.GroovyShell} and populates
 * its binding with pre-configured Spring beans (such as standard proxy handlers and
 * pre-defined conditions).
 * </p>
 * <p>
 * To provide a seamless Developer Experience (DX), it configures the Groovy compiler with
 * automatic package imports for handlers and static star imports for the {@link Conditions}
 * factory. This enables a clean, fluent, and boilerplate-free DSL for developers to define routing rules.
 * </p>
 */
public class GroovyRuleEngineService implements ApplicationContextAware, ResourceLoaderAware {
	private static final Logger LOG = LoggerFactory.getLogger(GroovyRuleEngineService.class);
	private static final String CONDITION_BEAN_PREFIX = "cxdevproxyCondition";

	private ApplicationContext applicationContext;
	private ResourceLoader resourceLoader;
	private GroovyShell shell;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		initGroovyShell();
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	private void initGroovyShell() {
		Binding binding = new Binding();

		Map<String, ProxyExchangeInterceptor> handlers = applicationContext.getBeansOfType(ProxyExchangeInterceptor.class);
		for (Map.Entry<String, ProxyExchangeInterceptor> entry : handlers.entrySet()) {
			binding.setVariable(entry.getKey(), entry.getValue());
			LOG.debug("Bound Spring handler bean '{}' to Groovy Context", entry.getKey());
		}

		Map<String, ProxyExchangeInterceptorCondition> conditions = applicationContext.getBeansOfType(ProxyExchangeInterceptorCondition.class);
		for (Map.Entry<String, ProxyExchangeInterceptorCondition> entry : conditions.entrySet()) {
			String beanName = entry.getKey();
			String bindingName = beanName;
			if (beanName.startsWith(CONDITION_BEAN_PREFIX) && beanName.length() > CONDITION_BEAN_PREFIX.length()) {
				String stripped = beanName.substring(CONDITION_BEAN_PREFIX.length());
				bindingName = Character.toLowerCase(stripped.charAt(0)) + stripped.substring(1);
			}
			binding.setVariable(bindingName, entry.getValue());
			LOG.debug("Bound Spring condition bean '{}' as '{}' to Groovy Context", beanName, bindingName);
		}

		ImportCustomizer importCustomizer = new ImportCustomizer();
		importCustomizer.addStarImports("me.cxdev.commerce.proxy.interceptor");
		importCustomizer.addStaticStars("me.cxdev.commerce.proxy.condition.Conditions");

		CompilerConfiguration config = new CompilerConfiguration();
		config.addCompilationCustomizers(importCustomizer);

		this.shell = new GroovyShell(this.getClass().getClassLoader(), binding, config);
	}

	/**
	 * Resolves the configured path to a physical File object. Needed for the File-Watcher to check
	 * lastModified timestamps.
	 */
	public File resolveScriptFile(String locationPath) {
		try {
			Resource resource = resourceLoader.getResource("classpath:" + locationPath);
			if (resource.exists()) {
				// This works flawlessly in local Hybris because extensions are exploded folders
				return resource.getFile();
			} else {
				LOG.warn("Configured Groovy script not found at path: {}", locationPath);
			}
		} catch (Exception e) {
			LOG.error("Could not resolve path {} to a physical file.", locationPath, e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<ProxyExchangeInterceptor> evaluateScript(File scriptFile) {
		if (scriptFile == null || !scriptFile.exists()) {
			return Collections.emptyList();
		}

		try {
			LOG.debug("Evaluating Groovy rules from: {}", scriptFile.getAbsolutePath());
			Object result = shell.evaluate(scriptFile);

			if (result instanceof List) {
				return (List<ProxyExchangeInterceptor>) result;
			} else {
				LOG.error("Groovy script {} must return a List<ProxyHttpServerExchangeHandler>", scriptFile.getName());
			}
		} catch (Exception e) {
			LOG.error("Failed to compile or execute Groovy script: {}", scriptFile.getName(), e);
		}

		return Collections.emptyList();
	}
}
