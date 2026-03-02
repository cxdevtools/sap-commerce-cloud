# CX Dev Proxy

The **CX Dev Proxy** is a powerful, Undertow-based local development proxy extension for SAP Commerce. It acts as a transparent reverse proxy in front of your local SAP Commerce Tomcat instance, solving the most common frontend and headless development pain points out-of-the-box.



## 🚀 Key Features

* **Groovy DSL & Hot-Reloading:** Define routing rules, mock APIs, and inject delays using a highly readable, fluent Groovy DSL. Save the script and the proxy updates instantly with **zero downtime**—no server restarts required!
* **Zero-Config JWT Mocking:** Automatically injects valid JWT tokens for local development. It uses the platform's native `jwkSource`, meaning tokens are fully trusted by SAP Commerce without any backend configuration.
* **Auto-CORS:** Automatically handles Cross-Origin Resource Sharing (CORS) preflight requests, echoing the incoming Origin header. Perfect for local Angular/React/Vue apps running on different ports (e.g., `localhost:4200`).
* **Latency Simulation:** Artificially delay API responses to test frontend loading states (spinners, skeletons) locally.
* **Endpoint Mocking:** Short-circuit requests to return static JSON responses for APIs that are not yet implemented in the backend.

---

## 🛠 Configuration & Properties

You can configure the core behavior via your `local.properties` (or `project.properties`).

```properties
# -----------------------------------------------------------------------
# CX Dev Proxy - Configuration
# -----------------------------------------------------------------------

# Enables or disables the proxy
cxdevproxy.enabled=true

# Port on which the proxy will listen
cxdevproxy.server.port=8080

# --- Dynamic Routing Rules (Groovy DSL) ---

# Paths to the Groovy scripts defining the proxy rules.
# Supports 'classpath:' (inside exploded extensions) and 'file:' (absolute path on disk).
cxdevproxy.proxy.frontend.rulefile=classpath:cxdevproxy/rulesets/cxdevproxy-frontend-rules.groovy
cxdevproxy.proxy.backend.rulefile=classpath:cxdevproxy/rulesets/cxdevproxy-backend-rules.groovy

# --- JWT Mocking Configuration ---

# Specifies the base path where the proxy looks for JWT claim templates (JSON files).
cxdevproxy.proxy.jwt.templatepath=classpath:cxdevproxy/jwt

# Defines the validity duration of the generated mock JWT tokens (e.g., 3600s, 60m, 10h, 1d).
cxdevproxy.proxy.jwt.validity=10h
```

---

## 🧩 Building Routing Rules (The Groovy DSL)

Instead of verbose XML, the CX Dev Proxy uses a powerful Groovy Domain Specific Language (DSL). The scripts are hot-reloaded the moment you save them.

To provide the best Developer Experience, our rule engine **automatically imports** all handlers and fluent condition factories (`Conditions.*`), and binds existing Spring beans to the script context.

### 1. Fluent Conditions API
You can build complex routing conditions using our AssertJ-style API.
* `pathStartsWith("/occ")`, `pathMatches("/occ/v2/**")`, `pathRegexMatches(".*")`
* `hasHeader("Authorization")`, `hasCookie("cxdevproxy_user_id")`, `hasParameter("fields")`
* `isMethod("POST")`
* **Logical Operators:** `.and()`, `.or()`, `.not()`

### 2. Pre-configured Spring Variables
The script environment is automatically populated with context-aware variables (derived from your Spring XML) to make routing even easier:
* **Paths:** `isOcc`, `isSmartEdit`, `isBackoffice`, `isAdminConsole`, `isAuthorizationServer`
* **Users:** `hasMockUser`, `hasAuthorizationHeader`
* **Standard Handlers:** `cxForwardedHeadersHandler`, `cxJwtInjectorHandler`, `cxCorsInjectorHandler`

---

## 💡 Usage Examples

To add custom rules, simply edit the `cxdevproxy-backend-rules.groovy` or `cxdevproxy-frontend-rules.groovy` files.

### 1. The Baseline (Default Script)
Every script must return a list of handlers. The most basic setup simply forwards standard proxy headers:

```groovy
def dynamicHandlers = []
dynamicHandlers << cxForwardedHeadersHandler
return dynamicHandlers
```

### 2. Simulating Network Delay
Frontend developers often need to test loading states. You can configure a `NetworkDelayHandler` to simulate a slow backend for specific paths.

```groovy
def dynamicHandlers = []
dynamicHandlers << cxForwardedHeadersHandler

// Simulate a slow backend calculation
dynamicHandlers << ProxyHandler.builder()
.withCondition( pathMatches("/occ/v2/**/heavy-calculation") )
.withHandler( new NetworkDelayHandler(800, 2500) ) // min, max delay in ms
.create()

return dynamicHandlers
```

### 3. Mocking Unfinished APIs (Static Response)
If an API does not exist yet, you can short-circuit the request and return a mocked JSON response.

```groovy
def dynamicHandlers = []
dynamicHandlers << cxForwardedHeadersHandler

dynamicHandlers << ProxyHandler.builder()
.withCondition( pathMatches("/occ/v2/**/new-feature") )
.withHandler( new StaticResponseHandler(200, "application/json", '{"status": "mocked", "data": []}') )
.create()

return dynamicHandlers
```

### 4. Injecting Mock JWT Tokens for Frontend Development
When working with a headless frontend (like Spartacus/Composable Storefront), you often want to bypass the actual login flow. The frontend can simply send a static token like 'secured', and the proxy will replace it with a valid, locally-signed JWT.

```groovy
def dynamicHandlers = []
dynamicHandlers << cxForwardedHeadersHandler

// Combine pre-configured conditions seamlessly!
dynamicHandlers << ProxyHandler.builder()
.withCondition( isOcc.and(hasAuthorizationHeader, hasMockUser) )
.withHandler( cxJwtInjectorHandler )
.create()

return dynamicHandlers
```

---

## 🔑 JWT Mocking Deep Dive

The `CxJwtTokenService` is the heart of the local authentication bypass.
It intercepts requests (when conditions match) and injects a dynamically signed JWT.

> **⚠️ IMPORTANT: OAuth Client ID Requirement**
> By default, our provided B2C and B2B user templates use `storefront` as the `client_id`. This breaks with the SAP Commerce default (which strangely uses `mobile_android` for OCC). To make the mock tokens work, you must ensure an OAuth Client with the ID `storefront` is created in your local SAP Commerce database (via ImpEx).

1. **Native Trust:** It extracts the private key from the platform's `jwkSource` (the exact same one used by the `authorizationserver`). This means the backend trusts the generated tokens implicitly. No extra backend configuration needed!
2. **Templates:** It loads static claims from JSON files. For example, if the cookies are `user_type=customer` and `user_id=john@example.com`, it looks for a template at:
   `classpath:cxdevproxy/jwt/customer/john@example.com.json`
3. **Dynamic Claims:** Claims like `iat` (Issued At) and `exp` (Expiration) are dynamically calculated based on `cxdevproxy.proxy.jwt.validity`.

To customize templates per project, simply set `cxdevproxy.proxy.jwt.templatepath=file:/path/to/your/custom/templates` in your local properties.