# CX Dev Proxy

The **CX Dev Proxy** is a powerful, Undertow-based local development proxy extension for SAP Commerce. It acts as a
transparent reverse proxy in front of your local SAP Commerce Tomcat instance, solving the most common frontend and
headless development pain points out-of-the-box.

## 🚀 Key Features

* **Dynamic Groovy DSL & Hot-Reloading:** Define routing rules and HTTP modifications using a highly readable, fluent
  Groovy DSL. Save the script and the proxy updates instantly with **zero downtime**—no server restarts required!
* **Clean Interceptor Architecture:** A strict separation between Request Routing (Handlers) and Request Modification (
  Interceptors) ensures predictable, thread-safe request manipulation.
* **Zero-Config JWT Mocking:** Automatically injects valid JWT tokens for local development. It uses the platform's
  native `jwkSource`, meaning tokens are fully trusted by SAP Commerce without any backend configuration.
* **Developer Auth Portal:** An out-of-the-box, bilingual (English/German) UI portal (`/proxy/login.html`) to easily
  switch between mock Employee, B2C, and B2B user contexts.
* **Auto-CORS:** Automatically handles Cross-Origin Resource Sharing (CORS) preflight requests, echoing the incoming
  Origin header. Perfect for local Angular/React/Vue apps running on different ports (e.g., `localhost:4200`).
* **Conflict-Free Configuration:** Securely injects Spring backend properties into frontend templates using a custom
  `%{property:default}` syntax, eliminating collisions with modern JavaScript.

---

## 🛠 Configuration & Properties

You can configure the core behavior via your `local.properties` (or `project.properties`).

```properties
# -----------------------------------------------------------------------
# CX Dev Proxy - Core Configuration
# -----------------------------------------------------------------------
# Enables or disables the proxy
cxdevproxy.enabled=true

# Port on which the proxy will listen
cxdevproxy.server.port=8080

# --- Dynamic Routing Rules (Groovy DSL) ---
# Paths to the Groovy scripts defining the proxy rules.
# Supports 'classpath:' (inside exploded extensions) and 'file:' (absolute path on disk).
cxdevproxy.proxy.frontend.rules=classpath:cxdevproxy/rulesets/cxdevproxy-frontend-rules.groovy
cxdevproxy.proxy.backend.rules=classpath:cxdevproxy/rulesets/cxdevproxy-backend-rules.groovy

# --- UI & Auth Portal Configuration ---
# Toggle visibility of customer tabs in the /proxy/login.html portal
cxdevproxy.proxy.ui.login.showB2C=false
cxdevproxy.proxy.ui.login.showB2B=true

# --- JWT Mocking Configuration ---
# Specifies the base path where the proxy looks for JWT claim templates (JSON files).
cxdevproxy.proxy.jwt.templatepath=classpath:cxdevproxy/jwt

# Defines the validity duration of the generated mock JWT tokens (e.g., 3600s, 60m, 10h, 1d).
cxdevproxy.proxy.jwt.validity=10h
```

---

## 🖥 Developer Auth Portal & Safe Properties

The extension provides a built-in UI to set mock user cookies. You can access it via `/proxy/login.html`.

To prevent syntax collisions between Spring property resolution and modern JavaScript template literals (`${...}`), the
HTML templates utilize a custom, robust placeholder syntax: **`%{property.key:defaultValue}`**.

This allows you to safely toggle UI elements based on your backend configuration without breaking frontend scripts:

```javascript
// Safely resolved by the proxy's ConfigurationService before reaching the browser
const showB2C = %{cxdevproxy.proxy.ui.login.showB2C:false};
const showB2B = %{cxdevproxy.proxy.ui.login.showB2B:false};
```

---

## 🧩 Building Routing Rules (The Groovy DSL)

Instead of verbose XML, the CX Dev Proxy uses a powerful Groovy Domain Specific Language (DSL). The scripts are
hot-reloaded the moment you save them.

To provide the best Developer Experience, our rule engine **automatically imports** all interceptors and fluent
condition factories (`Conditions.*`), and binds existing Spring beans to the script context.

### 1. Fluent API

You can build complex interceptor conditions using our functional, AssertJ-style API.

* `isMethod("GET")`, `pathStartsWith("/occ")`, `pathMatches("/occ/v2/**")`
* `hasHeader("Authorization")`, `hasCookie("cxdevproxy_user_id")`, `hasParameter("username")`
* **Logical Operators:** `and(...)`, `or(...)`, `not(...)` — which can be chained (`isOcc.and(hasMockUser)`) or nested (
  `and(isOcc, hasMockUser)`).

### 2. Pre-configured Spring Variables

The script environment is automatically populated with context-aware variables (derived from your Spring XML) to make
routing even easier:

* **Paths:** `isOcc`, `isSmartEdit`, `isBackoffice`, `isAdminConsole`, `isAuthorizationServer`
* **State:** `hasMockUser`, `hasAuthorizationHeader`
* **Available Interceptors:** `cxForwardedHeadersInterceptor`, `cxJwtInjectorInterceptor`, `cxCorsInjectorInterceptor`

---

## 💡 Usage Examples

To add custom rules for your project, first override the default rule paths in your `local.properties` to point to your custom project directory:

```properties
cxdevproxy.proxy.backend.rules=classpath:path/to/your/project/my-backend-rules.groovy
cxdevproxy.proxy.frontend.rules=classpath:path/to/your/project/my-frontend-rules.groovy
```

Then, simply create and edit these Groovy files. Every script must return a list of interceptors.

### 1. The Baseline (Default Script)

The most basic setup simply applies standard proxy headers unconditionally:

```groovy
return [
        cxForwardedHeadersInterceptor
]
```

### 2. Conditional Execution (The Builder Pattern)

You should never mutate the state of injected Spring beans directly. Instead, wrap them using the stateless
`interceptor()` builder to apply them conditionally.

```groovy
// Combine pre-configured conditions seamlessly
def jwtCondition = isOcc.or(isSmartEdit)
        .and(hasMockUser)
        .and(not(hasAuthorizationHeader))

return [
        cxForwardedHeadersInterceptor, // Always execute

        // Execute JWT Injector only if the complex condition is met
        interceptor()
                .constrainedBy(jwtCondition)
                .perform(cxJwtInjectorInterceptor),

        // Execute CORS Injector only for OCC requests
        interceptor()
                .constrainedBy(isOcc)
                .perform(cxCorsInjectorInterceptor)
]
```

---

## 🔑 JWT Mocking Deep Dive

The `cxJwtInjectorInterceptor` is the heart of the local authentication bypass.
It intercepts requests (when conditions match) and injects a dynamically signed JWT.

> **⚠️ IMPORTANT: OAuth Client ID Requirement**
> By default, our provided B2C and B2B user templates use `storefront` as the `client_id`. This breaks with the SAP
> Commerce default (which uses `mobile_android` for OCC).
> To make the mock tokens work, you must ensure an OAuth Client with the ID `storefront` is created in your local SAP
> Commerce database (via ImpEx).
> We understand that this might be questionable, but we are convinced that using a self-explaining `client_id` is a
> best-practise and should be enforced anyway. If you don't agree, feel free to change the templates to your needs.

1. **Native Trust:** It extracts the private key from the platform's `jwkSource` (the exact same one used by the
   `authorizationserver`). This means the backend trusts the generated tokens implicitly. No extra backend configuration
   needed!
2. **Templates:** It loads static claims from JSON files. For example, if the cookies are `user_type=customer` and
   `user_id=john@example.com`, it looks for a template at:
   `classpath:cxdevproxy/jwt/customer/john@example.com.json`
3. **Dynamic Claims:** Claims like `iat` (Issued At) and `exp` (Expiration) are dynamically calculated based on
   `cxdevproxy.proxy.jwt.validity`.

To customize templates per project, simply set `cxdevproxy.proxy.jwt.templatepath=path/to/your/custom/templates`
in your local properties.
