# CX DEV CPI

The `cxdevcpi` extension provides typed outbound calls from SAP Commerce to SAP
Cloud Integration (CPI). It uses the SAP `IntegrationRestTemplateFactory` and
`ConsumedDestination` configuration, while accepting Java request DTOs and
returning typed response DTOs through Spring's Jackson message converter.

## Scope

The reusable API is in `me.cxdev.cpi.generic`:

| Class | Purpose |
|---|---|
| `outbound.AbstractScpiOutboundStrategy` | Base class with an injected remote client factory and destination ID. |
| `remoteclient.ScpiRemoteClientFactory` | Resolves a destination by ID or accepts a `ConsumedDestinationModel`, then creates a client using SAP outbound services. |
| `remoteclient.ScpiRemoteClient` | REST operations without a URL argument, with typed responses and URI variables. |
| `remoteclient.RestTemplateScpiRemoteClient` | Delegates requests to the SAP-created `RestOperations` instance. |

The extension contains no business strategies, request/response DTOs, inbound
hooks, item types, credentials, endpoint data or automatic ImpEx imports.
It has no dependency on another CX DEV extension or on a project extension.
The two `postForLocation` overloads with URI variables forward both the request
body and the variables; this corrects a delegation error in the source implementation.

## Installation in a consuming project

1. Copy this directory to `hybris/bin/custom/cxdevtools/cxdevcpi` in the target project.
2. Add `<extension name="cxdevcpi"/>` to that project's `localextensions.xml` and
   ensure the extension is within a configured extension search path.
3. Add `<requires-extension name="cxdevcpi"/>` to the consuming extension's
   `extensioninfo.xml`.
4. Run the target project's normal SAP Commerce build and update procedure.

`cxdevcpi.application-context=cxdevcpi-spring.xml` registers the Spring context
automatically. The direct dependencies are `apiregistryservices` and
`outboundservices`; the platform resolves their transitive dependencies.
No additional Maven libraries are required.

## Configure outbound interfaces

Create these standard SAP items in the consuming project's configuration, using
Backoffice or that project's ImpEx setup:

| Item | Required configuration |
|---|---|
| `DestinationTarget` | A project-specific target ID. |
| `ConsumedOAuthCredential` or `BasicCredential` | Authentication for the CPI interface; for OAuth configure `clientId`, `clientSecret` and `oAuthUrl`. |
| `Endpoint` | An ID, version and the interface's URL/metadata. |
| `ConsumedDestination` | An ID, URL, endpoint, destination target, credential and `active=true`. |

The strategy's `destinationId` is the **ConsumedDestination ID**, not the
Endpoint ID. Lookup by ID requires a unique matching destination. If IDs are
reused across destination targets, resolve the intended model in the consuming
project and call `clientForDestination(ConsumedDestinationModel)` instead.

The optional destination additional property `urlQuery` is appended to the URL
with `?`. Store it without a leading `?` and keep the destination URL free of an
existing query. Both the URL and the query may contain Spring URI template
variables such as `{documentId}`; pass their values as a map or positional
arguments to the client operation. Null, empty or blank `urlQuery` values are
ignored.

Credentials, hosts and interface paths belong to the consuming project's
environment configuration. The factory retains the original lookup behavior;
it does not add an `active` check or an endpoint-specific validation layer.

## Implement consuming strategies

Extend `me.cxdev.cpi.generic.outbound.AbstractScpiOutboundStrategy` in the
consuming extension. Define the concrete bean in that extension's Spring context:

```xml
<bean id="myCpiStrategy" parent="abstractScpiOutboundStrategy"
      class="com.example.integration.MyCpiStrategy">
    <property name="destinationId" value="${myproject.cpi.destination.id}"/>
</bean>
```

The parent injects `scpiRemoteClientFactory`. The concrete strategy must supply
`destinationId`. Within its implementation, request a typed response:

```java
ScpiRemoteClient client = getRemoteClientFactory().clientForDestination(getDestinationId());
ResponseEntity<ResponseDto> response = client.postForEntity(requestDto, ResponseDto.class);
```

`RequestDto` and `ResponseDto` belong to the consuming project. Define them in
its `*-beans.xml` or as Jackson-compatible Java classes. Jackson handles nested
objects, collections and JSON property annotations. For generic response types,
use the `exchange` overload with a `ParameterizedTypeReference`:

```java
ResponseEntity<List<ResponseDto>> response = client.exchange(
        HttpMethod.GET, null, new ParameterizedTypeReference<List<ResponseDto>>() {});
```

The client delegates HTTP error handling to the configured RestTemplate. The
consuming strategy decides how to handle empty bodies, status codes and
`RestClientException`, and whether to apply business mapping or caching.

## Spring configuration and mapping

The public bean aliases follow the CX DEV naming convention:

| Alias | Implementation bean |
|---|---|
| `scpiRemoteClientFactory` | `cxScpiRemoteClientFactory` |
| `abstractScpiOutboundStrategy` | `cxAbstractScpiOutboundStrategy` |
| `abstractRestTemplateCreator` | `cxAbstractRestTemplateCreator` |

The extracted converter configuration replaces the shared SAP
`abstractRestTemplateCreator` alias. It therefore also applies to other SAP
RestTemplate creators that use this parent. It accepts JSON responses served as
`application/json` or `application/octet-stream`, while keeping SAP's
`outboundClientHttpRequestFactory`, `restTemplateHeaderInterceptors` and
`outboundRestTemplateCache`. This matches the source implementation and the
standard converter configuration in the referenced SAP release. Binary
responses still depend on the converters supported by the SAP-created template.

Authentication and available credential types are determined by the installed
SAP RestTemplate creators. The extension calls the template directly: it does
not invoke the `OutboundServiceFacade` decorator pipeline, generate Integration
Object payloads, fetch CSRF tokens through facade decorators or create outbound
monitoring records. The SAP template's interceptors remain attached; monitoring
records require the corresponding SAP monitoring context supplied by the caller.

When a target project already overrides the shared aliases, reconcile those
overrides in its Spring configuration. DTO conversion here uses Jackson HTTP
message conversion; it does not use the OCC `DataMapper`.

`resources/localization/cxdevcpi-locales_en.properties` and
`cxdevcpi-locales_de.properties` reserve the standard localization locations.
They contain no keys because this extension adds no item types or UI labels.

## Tests

`testsrc` contains isolated unit tests for nested DTO conversion, generic lists,
URI variables, JSON served as an octet stream, request bodies, HTTP errors,
destination lookup and Spring aliases/parent bean configuration. They use mock
HTTP responses and do not require a CPI account.

After registering and building the extension in a target project, run from
`hybris/bin/platform` with the platform Ant environment loaded:

```sh
ant unittests -Dtestclasses.extensions=cxdevcpi
```

## License

Licensed under the Apache License, Version 2.0. See [LICENSE.md](LICENSE.md).

Copyright 2026, CX DEV Tools.
