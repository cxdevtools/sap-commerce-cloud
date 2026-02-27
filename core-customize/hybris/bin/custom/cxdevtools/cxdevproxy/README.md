# CX Dev Proxy

The **CX Dev Proxy** is a powerful, Undertow-based local development proxy extension for SAP Commerce. It acts as a transparent reverse proxy in front of your local SAP Commerce instance, solving the most common frontend and headless development pain points out-of-the-box.



## 🚀 Key Features

* **Zero-Config JWT Mocking:** Automatically injects valid JWT tokens for local development. It uses the platform's native `jwkSource` so tokens are fully trusted by SAP Commerce without any backend configuration.
* **Auto-CORS:** Automatically handles Cross-Origin Resource Sharing (CORS) preflight requests, echoing the incoming Origin header. Perfect for local Angular/React/Vue apps running on different ports (e.g., `localhost:4200`).
* **Latency Simulation:** Artificially delay API responses to test frontend loading states (spinners, skeletons) locally.
* **Endpoint Mocking:** Short-circuit requests to return static JSON responses for APIs that are not yet implemented in the backend.
* **Spring Extensibility:** Fully configurable and extensible via Spring XML.

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

# --- JWT Mocking Configuration ---

# Specifies the base path where the proxy looks for JWT claim templates (JSON files).
# The final path is resolved as: <templatepath>/<userType>/<userId>.json
# Supports Spring ResourceLoader prefixes such as 'classpath:' or 'file:'.
# Example for absolute local path: file:/opt/hybris/config/jwt-templates
cxdevproxy.proxy.jwt.templatepath=classpath:cxdevproxy/jwt

# Defines the validity duration of the generated mock JWT tokens.
# Supports smart time units: 's' (seconds), 'm' (minutes), 'h' (hours), 'd' (days).
cxdevproxy.proxy.jwt.validity=10h
```

---

## 🧩 Building Routing Rules

The proxy routing is built using a combination of **Conditions** (when should a rule apply?) and **Handlers** (what should happen?).

### 1. Available Conditions (Abstract Beans)
We provide a comprehensive set of abstract Spring beans that you can use as `parent` definitions to build your own routing logic.

**Logical Conditions:**
* `cxNotCondition`, `cxAndCondition`, `cxOrCondition`

**Path-Based Conditions:**
* `cxPathStartsWithCondition`: Matches the exact prefix.
* `cxPathRegexCondition`: Matches paths using Regular Expressions.
* `cxPathAntMatcherCondition`: Matches using Spring's Ant-style path patterns (e.g., `/occ/v2/**`).

**Request-Based Conditions:**
* `cxHttpMethodCondition`, `cxHeaderExistsCondition`, `cxCookieExistsCondition`, `cxQueryParameterExistsCondition`

**Pre-configured Standard Paths:**
For convenience, the extension already defines conditions for standard SAP Commerce paths:
* `cxIsAuthorizationServer`
* `cxIsAdminConsole`
* `cxIsBackoffice`
* `cxIsOcc`
* `cxIsSmartEdit`

**Pre-configured User Conditions:**
* `cxHasAuthorizationHeader` (Checks if `Authorization` header is present).
* `cxHasMockUser` (Checks for both `cxdevproxy_user_id` and `cxdevproxy_user_type` cookies).

### 2. Available Handlers

* `cxJwtInjectorHandler`: Injects a dynamically generated, natively trusted JWT token into the `Authorization` header.
* `cxCorsInjectorHandler`: Acts as an Auto-CORS responder. By default, `allowCredentials` is set to `false`.
* `cxForwardedHeadersHandler`: Injects standard proxy headers (`X-Forwarded-For`, etc.).
* `cxStaticContentHandler`: Serves local files and assets.

---

## 💡 Advanced Usage & Custom Handlers

While the standard handlers are pre-registered, you can define project-specific handlers in your custom extension.

### Simulating Network Delay
Frontend developers often need to test loading states. You can configure a `NetworkDelayHandler` to simulate a slow backend.

Tip: We utilize the SAP Commerce `listMergeDirective` to seamlessly inject our custom handler into the existing proxy pipeline without overriding the default handlers.*

```xml
<bean id="myConditionalSlowApiHandler" class="me.cxdev.commerce.proxy.handler.ConditionalDelegateHandler">
    <property name="conditions">
        <bean parent="cxPathAntMatcherCondition">
            <property name="pattern" value="/occ/v2/**/heavy-calculation" />
        </bean>
    </property>
    <property name="delegates">
        <bean id="mySlowApiHandler" class="me.cxdev.commerce.proxy.handler.NetworkDelayHandler">
            <property name="minDelayInMillis" value="800" />
            <property name="maxDelayInMillis" value="2500" />
        </bean>
    </property>
</bean>

<bean depends-on="cxDefaultBackendProxyHandlers" parent="listMergeDirective">
    <property name="add" ref="myConditionalSlowApiHandler"/>
</bean>
```

### Mocking Unfinished APIs (Static Response)
If an API does not exist yet, you can short-circuit the request and return a mocked JSON response.

```xml
<bean id="myConditionalMockHandler" class="me.cxdev.commerce.proxy.handler.ConditionalDelegateHandler">
    <property name="conditions">
        <bean id="isMyMockApiCondition" parent="cxPathAntMatcherCondition">
            <property name="pattern" value="/occ/v2/**/new-feature" />
        </bean>
    </property>
    <property name="delegates">
        <bean id="myMockResponseHandler" class="me.cxdev.commerce.proxy.handler.StaticResponseHandler">
            <property name="statusCode" value="200" />
            <property name="contentType" value="application/json" />
            <property name="responseBody" value='{"status": "mocked", "data": []}' />
        </bean>
    </property>
</bean>

<bean depends-on="cxDefaultBackendProxyHandlers" parent="listMergeDirective">
    <property name="add" ref="myConditionalMockHandler"/>
</bean>
```

---

## 🔑 JWT Mocking Deep Dive

The `CxJwtTokenService` is the heart of the local authentication bypass.
It intercepts requests (e.g., via `cxHasMockUser` condition) and injects a signed JWT.

⚠️ **IMPORTANT: OAuth Client ID Requirement**:
By default, our provided B2C and B2B user templates use `storefront` as the client_id. This breaks with the SAP Commerce default (which strangely uses `mobile_android` for OCC). To make the mock tokens work, you must ensure an OAuth Client with the ID storefront is created in your local SAP Commerce database (via ImpEx). We recommend to change the default ID to storefront as this communicates better the intent of the client.

1. **Native Trust:** It extracts the private key from the platform's `jwkSource` (the same one used by the `authorizationserver`). This means the backend trusts the generated tokens implicitly.
2. **Templates:** It loads static claims from JSON files. For example, if the cookies are `user_type=customer` and `user_id=john@example.com`, it looks for a template at:
   `classpath:cxdevproxy/jwt/customer/john@example.com.json`
3. **Dynamic Claims:** Claims like `iat` (Issued At) and `exp` (Expiration) are dynamically calculated based on `cxdevproxy.proxy.jwt.validity`.

To customize templates per project, simply set `cxdevproxy.proxy.jwt.templatepath=file:/path/to/your/custom/templates` in your local properties.

---

## 🏗 Extending the Proxy in Your Project

**Do not modify the core `cxdevproxy-spring.xml`!** Instead, add your custom rules, conditions, and handlers to your project-specific extensions.

The `cxdevproxy` extension intentionally leaves an import at the end of its context:
```xml
<import resource="classpath*:/cxdevproxy/config/cxdevproxy-additions-spring.xml"/>
```

To add custom handlers, simply create a file named `cxdevproxy-additions-spring.xml` inside `resources/cxdevproxy/config/` in your own extension. Since the proxy uses Spring `<util:list>` aliases (`cxLocalRouteHandlers`, `cxFrontendProxyHandlers`, `cxBackendProxyHandlers`), you can easily override or append to these lists in your custom XML.

---

### Injecting Mock JWT Tokens for Frontend Development
When working with a headless frontend (like Spartacus/Composable Storefront), you often want to bypass the actual login flow. You can use the `cxJwtInjectorHandler` to automatically replace the `Authorization` header with a valid, locally signed JWT token. With this in place, the frontend can simply send a static token like 'secured' and it will automatically be replaced by the proxy with a valid JWT.

To ensure this only happens when appropriate, we combine three pre-configured conditions: it must be an OCC call, the request must have an Authorization header, and the developer must have the mock cookies set.

```xml
<bean id="myJwtInjectorForOcc" class="me.cxdev.commerce.proxy.handler.ConditionalDelegateHandler">
    <property name="conditions">
       <list>
          <ref bean="cxIsOcc" />
          <ref bean="cxHasAuthorizationHeader" />
          <ref bean="cxHasMockUser" />
       </list>
    </property>
    <property name="delegate" ref="cxJwtInjectorHandler" />
</bean>

<bean depends-on="cxDefaultBackendProxyHandlers" parent="listMergeDirective">
    <property name="add" ref="myJwtInjectorForOcc"/>
</bean>
```