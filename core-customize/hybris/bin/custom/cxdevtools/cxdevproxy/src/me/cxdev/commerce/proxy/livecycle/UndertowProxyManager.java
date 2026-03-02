package me.cxdev.commerce.proxy.livecycle;

import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import de.hybris.bootstrap.config.ExtensionInfo;
import de.hybris.bootstrap.config.WebExtensionModule;
import de.hybris.platform.core.MasterTenant;
import de.hybris.platform.core.Registry;
import de.hybris.platform.util.Utilities;

import io.undertow.Undertow;
import io.undertow.protocols.ssl.UndertowXnioSsl;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.ssl.XnioSsl;

import me.cxdev.commerce.proxy.handler.ProxyRouteHandler;
import me.cxdev.commerce.proxy.interceptor.ProxyExchangeInterceptor;
import me.cxdev.commerce.proxy.trust.AcceptAllTrustManager;
import me.cxdev.commerce.proxy.util.ResourcePathUtils;
import me.cxdev.commerce.proxy.util.TimeUtils;

/**
 * Manages the lifecycle of an embedded Undertow reverse proxy server for SAP Commerce.
 * <p>
 * This manager is responsible for starting and stopping the Undertow server, configuring
 * inbound SSL connections, and setting up the routing rules to dispatch incoming requests
 * either to a local frontend (e.g., an Angular Dev Server) or the SAP Commerce backend (Tomcat).
 * It also supports intercepting requests via custom local route handlers.
 * </p>
 */
public class UndertowProxyManager implements SmartLifecycle, InitializingBean, DisposableBean {
	private static final Logger LOG = LoggerFactory.getLogger(UndertowProxyManager.class);

	// Spring Injected Properties
	private boolean enabled;

	// SSL Properties
	private boolean sslEnabled;
	private String sslKeystorePath;
	private String sslKeystorePassword;
	private String sslKeystoreAlias;

	// Server Binding Properties
	private String serverBindAddress;
	private String serverHostname;
	private String serverProtocol;
	private int serverPort;

	// Proxy Target Properties
	private String frontendProtocol;
	private String frontendHostname;
	private int frontendPort;
	private String frontendRulesFilePath;
	private String backendProtocol;
	private String backendHostname;
	private int backendPort;
	private String backendRulesFilePath;
	private String backendContexts;

	// Rule Engine
	private long groovyRuleReloadIntervalMs = 5000;
	private GroovyRuleEngineService groovyRuleEngineService;

	// Thread-safe references for hot-reloading handlers
	private final AtomicReference<List<ProxyExchangeInterceptor>> frontendHandlersRef = new AtomicReference<>();
	private final AtomicReference<List<ProxyExchangeInterceptor>> backendHandlersRef = new AtomicReference<>();

	// Watcher Status
	private ScheduledExecutorService watcherExecutor;
	private File frontendScriptFile;
	private File backendScriptFile;
	private long lastModifiedFrontend = 0;
	private long lastModifiedBackend = 0;

	// List of Local Routes
	private List<ProxyRouteHandler> routeHandlers;

	private Undertow server;
	private boolean running = false;

	/**
	 * Initializes the proxy manager after all Spring properties have been set.
	 * Prepares the atomic handler lists, resolves the Groovy script files,
	 * triggers the initial script evaluation, and starts the file watcher for hot-reloading.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		frontendHandlersRef.set(List.of());
		backendHandlersRef.set(List.of());

		if (groovyRuleEngineService != null) {
			frontendScriptFile = groovyRuleEngineService.resolveScriptFile(frontendRulesFilePath);
			backendScriptFile = groovyRuleEngineService.resolveScriptFile(backendRulesFilePath);

			reloadFrontendRulesIfChanged();
			reloadBackendRulesIfChanged();

			startFileWatcher();
		}
	}

	/**
	 * Starts a daemon thread that periodically checks the Groovy rule scripts for modifications.
	 * If changes are detected, the scripts are recompiled and smoothly hot-swapped.
	 */
	private void startFileWatcher() {
		watcherExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread thread = new Thread(r, "CxDevProxy-RuleWatcher");
			thread.setDaemon(true);
			return thread;
		});

		watcherExecutor.scheduleWithFixedDelay(() -> {
			reloadFrontendRulesIfChanged();
			reloadBackendRulesIfChanged();
		}, groovyRuleReloadIntervalMs, groovyRuleReloadIntervalMs, TimeUnit.MILLISECONDS);

		LOG.info("Started Groovy Rule Watcher for dynamic hot-reloading every {} ms.", groovyRuleReloadIntervalMs);
	}

	/**
	 * Evaluates the frontend Groovy script if the file has been modified since the last check.
	 * Replaces the active handlers atomically to ensure zero proxy downtime.
	 */
	private void reloadFrontendRulesIfChanged() {
		if (frontendScriptFile != null && frontendScriptFile.exists()) {
			long currentModified = frontendScriptFile.lastModified();
			if (currentModified > lastModifiedFrontend) {
				LOG.info("Detected change in frontend rules script. Compiling and reloading...");
				List<ProxyExchangeInterceptor> newHandlers = groovyRuleEngineService.evaluateScript(frontendScriptFile);
				if (newHandlers != null && !newHandlers.isEmpty()) {
					frontendHandlersRef.set(newHandlers); // ATOMIC SWAP!
					lastModifiedFrontend = currentModified;
					LOG.info("Frontend rules successfully reloaded. Active handlers: {}", newHandlers.size());
				}
			}
		}
	}

	/**
	 * Evaluates the backend Groovy script if the file has been modified since the last check.
	 * Replaces the active handlers atomically to ensure zero proxy downtime.
	 */
	private void reloadBackendRulesIfChanged() {
		if (backendScriptFile != null && backendScriptFile.exists()) {
			long currentModified = backendScriptFile.lastModified();
			if (currentModified > lastModifiedBackend) {
				LOG.info("Detected change in backend rules script. Compiling and reloading...");
				List<ProxyExchangeInterceptor> newHandlers = groovyRuleEngineService.evaluateScript(backendScriptFile);
				if (newHandlers != null && !newHandlers.isEmpty()) {
					backendHandlersRef.set(newHandlers); // ATOMIC SWAP!
					lastModifiedBackend = currentModified;
					LOG.info("Backend rules successfully reloaded. Active handlers: {}", newHandlers.size());
				}
			}
		}
	}

	/**
	 * Starts the embedded Undertow proxy server.
	 * Initializes proxy clients for frontend and backend, applies custom handler rules,
	 * and binds the server to the configured host and port.
	 */
	@Override
	public void start() {
		if (!enabled) {
			LOG.info("cxdevproxy is disabled (cxdevproxy.enabled=false). Undertow proxy will not be started.");
			return;
		}

		if (!sslEnabled) {
			this.serverProtocol = "http";
			LOG.info("SSL is disabled. Forcing server protocol to 'http'.");
		}

		LOG.info("Starting embedded Undertow proxy (Protocol: {})...", this.serverProtocol);

		try {
			String frontendUrl = frontendProtocol + "://" + frontendHostname + ":" + frontendPort;
			LoadBalancingProxyClient frontendClient = new LoadBalancingProxyClient()
					.addHost(new URI(frontendUrl), createTrustAllXnioSsl(frontendHostname))
					.setConnectionsPerThread(20);

			String backendUrl = backendProtocol + "://" + backendHostname + ":" + backendPort;
			LoadBalancingProxyClient backendClient = new LoadBalancingProxyClient()
					.addHost(new URI(backendUrl), createTrustAllXnioSsl(backendHostname))
					.setConnectionsPerThread(20);

			HttpHandler baseFrontendHandler = ProxyHandler.builder().setProxyClient(frontendClient).build();
			HttpHandler baseBackendHandler = ProxyHandler.builder().setProxyClient(backendClient).setMaxRequestTime(30000).build();

			HttpHandler finalFrontendHandler = applyFrontendRules(baseFrontendHandler);
			HttpHandler finalBackendHandler = applyBackendRules(baseBackendHandler);

			List<String> activeBackendContexts = determineBackendContexts();
			LOG.info("Active backend routing contexts: {}", activeBackendContexts);

			HttpHandler routingHandler = exchange -> {
				// 1. Check if a route handler wants to intercept the request
				if (routeHandlers != null) {
					for (ProxyRouteHandler handler : routeHandlers) {
						if (handler.matches(exchange)) {
							LOG.debug("Serving request {} {} with local handler {}.", exchange.getRequestMethod(), exchange.getRequestURI(),
									handler.getClass().getSimpleName());
							handler.handleRequest(exchange);
							return;
						}
					}
				}

				// 2. Regular proxy routing if no local handler matched
				routeRequest(exchange, activeBackendContexts, finalBackendHandler, finalFrontendHandler);
			};

			Undertow.Builder serverBuilder = Undertow.builder().setHandler(routingHandler);

			if (sslEnabled) {
				SSLContext serverSslContext = createServerSSLContext();
				if (serverSslContext == null) {
					return;
				}
				serverBuilder.addHttpsListener(serverPort, serverBindAddress, serverSslContext);
				LOG.info("Undertow proxy listening securely on {}://{}:{} (HTTPS).", serverProtocol, serverBindAddress, serverPort);
			} else {
				serverBuilder.addHttpListener(serverPort, serverBindAddress);
				LOG.info("Undertow proxy listening on {}://{}:{} (HTTP).", serverProtocol, serverBindAddress, serverPort);
			}

			server = serverBuilder.build();
			server.start();
			running = true;

		} catch (Exception e) {
			LOG.error("Error starting Undertow proxy", e);
		}
	}

	/**
	 * Determines the URL context paths that should be routed to the SAP Commerce backend.
	 * Uses explicitly configured contexts if provided, otherwise performs auto-discovery
	 * by scanning all installed web extensions for their web roots.
	 *
	 * @return A list of backend context paths (e.g., ["/backoffice", "/occ"]).
	 */
	private List<String> determineBackendContexts() {
		if (StringUtils.isNotBlank(this.backendContexts)) {
			return Arrays.stream(this.backendContexts.split(","))
					.map(String::trim)
					.filter(StringUtils::isNotBlank)
					.toList();
		}

		LOG.info("No manual backend contexts configured. Starting auto-discovery via Utilities.getWebroot()...");

		return Utilities.getInstalledExtensionNames(MasterTenant.getInstance()).stream()
				.map(Utilities::getExtensionInfo)
				.filter(ExtensionInfo::isWebExtension)
				.map(ExtensionInfo::getWebModule)
				.map(WebExtensionModule::getWebRoot)
				.filter(not(isEqual("/")))
				.distinct()
				.sorted()
				.toList();
	}

	/**
	 * Routes the incoming HTTP request to either the backend or the frontend proxy handler
	 * based on the request path and the determined backend contexts.
	 *
	 * @param exchange        The current HTTP server exchange.
	 * @param backendContexts The list of context paths mapped to the backend.
	 * @param backendHandler  The handler responsible for backend routing.
	 * @param frontendHandler The handler responsible for frontend routing.
	 * @throws Exception If an error occurs during routing.
	 */
	private void routeRequest(HttpServerExchange exchange, List<String> backendContexts, HttpHandler backendHandler, HttpHandler frontendHandler) throws Exception {
		String path = exchange.getRequestPath();
		boolean isBackendRequest = backendContexts.stream().anyMatch(path::startsWith);

		if (isBackendRequest) {
			LOG.debug("Serving request {} {} with backend handler.", exchange.getRequestMethod(), exchange.getRequestURI());
			backendHandler.handleRequest(exchange);
		} else {
			LOG.debug("Serving request {} {} with frontend handler.", exchange.getRequestMethod(), exchange.getRequestURI());
			frontendHandler.handleRequest(exchange);
		}
	}

	/**
	 * Wraps the base frontend handler with any configured custom interceptors/handlers.
	 * Pulls the handlers atomically from the reference to ensure thread-safety during hot-reloads.
	 *
	 * @param next The base proxy handler.
	 * @return A chained HTTP handler applying all configured frontend rules.
	 */
	protected HttpHandler applyFrontendRules(HttpHandler next) {
		return exchange -> {
			List<ProxyExchangeInterceptor> currentHandlers = frontendHandlersRef.get();
			for (ProxyExchangeInterceptor handler : emptyIfNull(currentHandlers)) {
				handler.apply(exchange);
				if (exchange.isResponseStarted() || exchange.isComplete()) {
					break;
				}
			}
			next.handleRequest(exchange);
		};
	}

	/**
	 * Wraps the base backend handler with any configured custom interceptors/handlers.
	 * Pulls the handlers atomically from the reference to ensure thread-safety during hot-reloads.
	 *
	 * @param next The base proxy handler.
	 * @return A chained HTTP handler applying all configured backend rules.
	 */
	protected HttpHandler applyBackendRules(HttpHandler next) {
		return exchange -> {
			List<ProxyExchangeInterceptor> currentHandlers = backendHandlersRef.get();
			for (ProxyExchangeInterceptor handler : emptyIfNull(currentHandlers)) {
				handler.apply(exchange);
				if (exchange.isResponseStarted() || exchange.isComplete()) {
					break;
				}
			}
			next.handleRequest(exchange);
		};
	}

	/**
	 * Creates the SSL context used by the Undertow server to accept incoming HTTPS connections.
	 * Loads the keystore configured in the properties.
	 *
	 * @return The configured SSLContext, or null if configuration is invalid.
	 * @throws Exception If keystore loading or context initialization fails.
	 */
	private SSLContext createServerSSLContext() throws Exception {
		if (StringUtils.isBlank(sslKeystorePath)) {
			LOG.error("Keystore path is empty or not set. Proxy startup aborted.");
			return null;
		}

		File keystoreFile = new File(sslKeystorePath.trim());

		if (!keystoreFile.exists() || !keystoreFile.isFile()) {
			LOG.error("Keystore not found at: {}. Proxy server will not be started.", keystoreFile.getAbsolutePath());
			return null;
		}

		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		try (InputStream is = new FileInputStream(keystoreFile)) {
			keyStore.load(is, sslKeystorePassword != null ? sslKeystorePassword.toCharArray() : new char[0]);
		}

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, sslKeystorePassword != null ? sslKeystorePassword.toCharArray() : new char[0]);

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(kmf.getKeyManagers(), null, null);
		return sslContext;
	}

	/**
	 * Creates a permissive SSL client context for outbound proxy connections to target systems.
	 * Disables strict certificate validation to allow proxying to local self-signed endpoints.
	 *
	 * @param serverNameIndicator The SNI hostname to use, if any.
	 * @return The configured XnioSsl instance.
	 * @throws Exception If SSL context initialization fails.
	 */
	private XnioSsl createTrustAllXnioSsl(String serverNameIndicator) throws Exception {
		TrustManager[] trustAllCerts = new TrustManager[] {
				new AcceptAllTrustManager()
		};

		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());

		ClassLoader rootClassLoader = Registry.class.getClassLoader();
		return new UndertowXnioSsl(Xnio.getInstance(rootClassLoader), OptionMap.EMPTY, sc);
	}

	/**
	 * Stops the embedded Undertow proxy server and updates the running state.
	 */
	@Override
	public void stop() {
		if (server != null) {
			LOG.info("Stopping embedded Undertow proxy...");
			server.stop();
			running = false;
		}
	}

	/**
	 * Shuts down the background file watcher executor when the Spring context is destroyed.
	 */
	@Override
	public void destroy() throws Exception {
		if (watcherExecutor != null && !watcherExecutor.isShutdown()) {
			watcherExecutor.shutdownNow();
		}
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}

	// --- Standard Setters ---

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	public void setSslKeystorePath(String sslKeystorePath) {
		this.sslKeystorePath = sslKeystorePath;
	}

	public void setSslKeystorePassword(String sslKeystorePassword) {
		this.sslKeystorePassword = sslKeystorePassword;
	}

	public void setSslKeystoreAlias(String sslKeystoreAlias) {
		this.sslKeystoreAlias = sslKeystoreAlias;
	}

	public void setServerBindAddress(String serverBindAddress) {
		this.serverBindAddress = serverBindAddress;
	}

	public void setServerHostname(String serverHostname) {
		this.serverHostname = serverHostname;
	}

	public void setServerProtocol(String serverProtocol) {
		this.serverProtocol = serverProtocol;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public void setFrontendProtocol(String frontendProtocol) {
		this.frontendProtocol = frontendProtocol;
	}

	public void setFrontendHostname(String frontendHostname) {
		this.frontendHostname = frontendHostname;
	}

	public void setFrontendPort(int frontendPort) {
		this.frontendPort = frontendPort;
	}

	/**
	 * Sets the file path for the frontend Groovy rules.
	 * Automatically normalizes the path to ensure a valid ResourceLoader prefix.
	 */
	public void setFrontendRulesFilePath(String frontendRulesFilePath) {
		this.frontendRulesFilePath = ResourcePathUtils.normalizeFilePath(frontendRulesFilePath, "frontend rules");
	}

	public void setBackendProtocol(String backendProtocol) {
		this.backendProtocol = backendProtocol;
	}

	public void setBackendHostname(String backendHostname) {
		this.backendHostname = backendHostname;
	}

	public void setBackendPort(int backendPort) {
		this.backendPort = backendPort;
	}

	/**
	 * Sets the file path for the backend Groovy rules.
	 * Automatically normalizes the path to ensure a valid ResourceLoader prefix.
	 */
	public void setBackendRulesFilePath(String backendRulesFilePath) {
		this.backendRulesFilePath = ResourcePathUtils.normalizeFilePath(backendRulesFilePath, "backend rules");
	}

	public void setBackendContexts(String backendContexts) {
		this.backendContexts = backendContexts;
	}

	/**
	 * Smart setter allowing human-readable time intervals like "5s", "10m", "1h", etc.
	 * Fallback to milliseconds if no unit is provided.
	 *
	 * @param interval The interval string from Spring properties.
	 */
	public void setGroovyRuleReloadInterval(String interval) {
		this.groovyRuleReloadIntervalMs = TimeUtils.parseIntervalToMillis(interval, 5000L, "Groovy rule reload interval");
	}

	public void setGroovyRuleEngineService(GroovyRuleEngineService groovyRuleEngineService) {
		this.groovyRuleEngineService = groovyRuleEngineService;
	}

	public void setRouteHandlers(List<ProxyRouteHandler> routeHandlers) {
		this.routeHandlers = routeHandlers;
	}
}
