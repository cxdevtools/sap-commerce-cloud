# CX Dev Proxy

The `cxdevproxy` extension improves and simplifies the local development experience for SAP Commerce by providing an
embedded, highly configurable Undertow reverse proxy.

## FEATURE DESCRIPTION

This extension solves common local routing and authentication challenges when developing headless storefronts (like
Spartacus/Angular) against a local SAP Commerce backend. It provides a seamless "all-in-one" endpoint for developers.

In addition, it contributes several major features to the local development lifecycle.

### Embedded Reverse Proxy & Header Forwarding

A lightweight Undertow server that listens on a unified port (e.g., `8080`) and dynamically routes traffic to either the
local frontend dev server (e.g., Angular on `4200`) or the SAP Commerce backend (Tomcat on `9002`).
The `ForwardedHeadersHandler` automatically injects `X-Forwarded-Host`, `X-Forwarded-Proto`, and `X-Forwarded-Port`
headers. This prevents infinite HTTPS redirect loops from Spring Security and ensures that Tomcat generates absolute
URLs correctly.

### Developer Portal & JWT Mocking

Provides a local Developer Portal (accessible via the root or `index.html`) to easily switch between different mocked
user sessions (Employees or Customers).
When a user is selected, the `JwtInjectorHandler` intercepts backend requests, dynamically generates a signed JWT using
the local domain's private key (via Nimbus JOSE+JWT), and injects it as a `Bearer` token. The static JWT claims can be
easily managed via JSON templates.

### Startup Interception

The `StartupPageHandler` listens to the Hybris tenant lifecycle. While the master tenant is starting up or shutting
down, all incoming proxy requests are intercepted, and a localized, auto-refreshing "503 Service Unavailable"
maintenance page is served to prevent hanging requests or backend errors.

### Modular Static Content

The `StaticContentHandler` allows serving static files (HTML, CSS, JS, images) directly from the classpath without
invoking the backend server. Files placed in `resources/cxdevproxy/static-content/` are automatically served.

### Extensible Conditional Handlers

The proxy pipeline is highly customizable. The `ConditionalDelegateHandler` allows executing specific handlers only if a
set of conditions is met (e.g., matching HTTP methods, specific paths, or headers). It supports complex logical
expressions via `AndCondition`, `OrCondition`, and `NotCondition`.

## How to activate and use

To activate this feature, simply set the `cxdevproxy.enabled` property to `true` in your `local.properties`.

**Adding Static Content:**
Other extensions can contribute to the proxy's static files (like adding new pages to the Developer Portal) by simply
placing files inside their own `resources/cxdevproxy/static-content/` directory. The proxy classloader will pick them up
automatically.

**Adding new Mock Users (JWT Templates):**
To add a new mock user, create a JSON file with the static claims in `resources/cxdevproxy/jwt/employee/<id>.json` or
`resources/cxdevproxy/jwt/customer/<id>.json`.

**Adding Custom Handlers and Conditions:**
You can extend the proxy routing by defining new conditions and handlers in your Spring configuration. Simply create
your custom conditions and inject them into a `ConditionalDelegateHandler` via Spring XML:

```xml
<bean id="myCustomPathCondition" class="me.cxdev.commerce.proxy.condition.PathStartsWithCondition">
    <property name="prefix" value="/my-custom-api/" />
</bean>
```

Then add your handler to the `backendHandlers` list of the `UndertowProxyManager` bean.


## Configuration parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| cxdevproxy.enabled | boolean | Feature toggle. Must be set to `true` to start the Undertow proxy server (default: false). |
| cxdevproxy.application-context | String | Specifies the location of the spring context file automatically added to the global platform application context. |
| cxdevproxy.ssl.enabled | boolean | Enables HTTPS for the Undertow server. If false, forces HTTP routing. |
| cxdevproxy.ssl.keystore.path | String | Absolute path to the PKCS12 keystore used for SSL offloading and JWT signing. |
| cxdevproxy.ssl.keystore.password | String | Password for the configured keystore. |
| cxdevproxy.ssl.keystore.alias | String | Alias of the private key within the keystore used for SSL and JWT signing. |
| cxdevproxy.server.bindaddress | String | Network interface the proxy listens on (e.g., `127.0.0.1` or `0.0.0.0`). |
| cxdevproxy.server.protocol | String | The protocol exposed by the proxy (`http` or `https`). |
| cxdevproxy.server.hostname | String | The public hostname of the proxy (e.g., `local.cxdev.me`). Used for the `X-Forwarded-Host` header. |
| cxdevproxy.server.port | int | The port the Undertow proxy listens on (default: `8080`). |
| cxdevproxy.proxy.frontend.protocol | String | Protocol for frontend routing (e.g., `http` or `https`). |
| cxdevproxy.proxy.frontend.hostname | String | Hostname of the local frontend dev server (e.g., `localhost`). |
| cxdevproxy.proxy.frontend.port | int | Port of the local frontend dev server (e.g., `4200`). |
| cxdevproxy.proxy.backend.protocol | String | Protocol for backend routing (e.g., `https`). |
| cxdevproxy.proxy.backend.hostname | String | Hostname of the local SAP Commerce backend (e.g., `localhost`). |
| cxdevproxy.proxy.backend.port | int | Port of the local SAP Commerce backend (e.g., `9002`). |
| cxdevproxy.proxy.backend.contexts | String | Comma-separated list of paths to route to the backend. If empty, uses auto-discovery via webroot properties. |


## License

_Licensed under the Apache License, Version 2.0, January 2004_

_Copyright 2026, SAP CX Tools_

