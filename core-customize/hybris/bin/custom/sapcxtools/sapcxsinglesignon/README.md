# SAP CX Single-Sign-On

The `sapcxsinglesignon` extension provides core implementations for SSO integration with external
service and identity providers, such as Auth0 by Okta.

## FEATURE DESCRIPTION

The functionality covers a filter for the OCC layer, that handles the validation of the access tokens.
The filter will be executed before the actual spring security chain of the OCC extension, creating a
valid token in the local token storage for the authenticated user.

The extension also ships a customer replication strategy, that can be activated to create users within
Auth0, whenever a customer object is created or changed within SAP commerce cloud.

### How to activate and use

To activate the functionality, one needs to set the configuration parameters accordingly for each environment,
especially the flags `sapcxsinglesignon.filter.enabled`, `sapcxsinglesignon.replicate.creation.enabled`, and
`sapcxsinglesignon.replicate.removal.enabled` which are set to `false` by default.

Also, the IDP should be configured to use the SAP Commerce OCC endpoint as audience, and provide the following
information within the access token, as they are required by the filter:

- email and/or username (whatever field is configured as `idfield`)
- given_name (first name for customer creation)
- family_name (last name for customer creation)

For example, using Auth0, the following post login handler is required:

```
/**
* Handler that will be called during the execution of a PostLogin flow.
*
* @param {Event} event - Details about the user and the context in which they are logging in.
* @param {PostLoginAPI} api - Interface whose methods can be used to change the behavior of the login.
*/
exports.onExecutePostLogin = async (event, api) => {
  api.accessToken.setCustomClaim('email', event.user.email);
  api.accessToken.setCustomClaim('username', event.user.username);
  api.accessToken.setCustomClaim('given_name', event.user.given_name);
  api.accessToken.setCustomClaim('family_name', event.user.family_name);
};
```

In addition to the IDP and backend configuration, the composable storefront needs to be extended with
configuration settings as within the following example:

```typescript
export const authCodeFlowConfig: AuthConfig = {
	authentication: {
		client_id: '<client id>',
		client_secret: '<client secret>',
		baseUrl: 'https://<your-auth0-domain>',
		tokenEndpoint: '/oauth/token',
		loginUrl: '/authorize',
		revokeEndpoint: '/oauth/revoke',
        logoutUrl: '/oidc/logout',
		userinfoEndpoint: '/userinfo',
		OAuthLibConfig: {
			redirectUri: 'https://www.<your-domain>.com',
            postLogoutRedirectUri: 'https://www.<your-domain>.com',
			responseType: 'code',
			scope: 'openid profile email',
			showDebugInformation: true,
			disablePKCE: false,
            customQueryParams: {
                audience: 'https://api.<your-domain>.com/occ/v2/'
            }
		},
	},
};
```

To avoid failing requests during the logout sequence, we also strongly recommend to overlay the standard logout
guard from the Spartacus project, with an implementation as follows here:

```typescript
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import {
    AuthRedirectService,
    AuthService,
    CmsService,
    OccEndpointsService,
    ProtectedRoutesService,
    SemanticPathService,
} from '@spartacus/core';
import { LogoutGuard } from '@spartacus/storefront';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap, take } from 'rxjs/operators';

@Injectable()
export class CustomLogoutGuard extends LogoutGuard {
    constructor(
        protected auth: AuthService,
        protected cms: CmsService,
        protected semanticPathService: SemanticPathService,
        protected protectedRoutes: ProtectedRoutesService,
        protected router: Router,
        protected http: HttpClient,
        protected endpointsService: OccEndpointsService
    ) {
        super(auth, cms, semanticPathService, protectedRoutes, router);
    }

	canActivate(): Observable<boolean | UrlTree> {
        /**
         * Note:
         * We must wait until the access token was revoked from the backend before
         * performing the call to this.logout(), otherwise racing conditions may
         * terminate the call before the backend took note of it.
         *
         * But we do not care whether the action was successful or not, because the
         * user shall walk through the logout process in any case.
         *
         * In rare situations, it may occur that a token was not successfully
         * removed from the backend, but those cases all have in common, that the
         * accesss token is not longer valid, e.g. has expired, or already replaced
         * by a different one.
         */
        return this.revokeAccessToken().pipe(
            take(1), // wait until call finished
            switchMap(() => this.logout()), // perform standard oauth2 logout
            map(() => true)
        );
    }

    revokeAccessToken(): Observable<boolean | any> {
        let endpoint = this.getRevocationEndpoint();
        return this.http.post(endpoint, '').pipe(
            map(() => of(true)),
            catchError((err: HttpErrorResponse) => {
                return of(false);
            })
        );
    }
    
	getRevocationEndpoint(): string {
		return this.endpointsService.buildUrl('/users/current/revokeAccessToken');
	}
}
```

For the customer replication, one can add additional populators to the `auth0CustomerConverter` converter bean.
This can be easily done using the `modifyPopulatorList` bean notation:

```xml
<bean id="myCustomerPopulator" class="com.acme.cx.MyCustomerPopulator"/>
<bean parent="modifyPopulatorList">
    <property name="list" ref="auth0CustomerConverter"/>
    <property name="add" ref="myCustomerPopulator"/>
</bean>
```

### Configuration parameters

| Parameter                                                    | Type | Description                                                                              |
|--------------------------------------------------------------|------|------------------------------------------------------------------------------------------|
| sapcxsinglesignon.filter.enabled                             | Boolean | specifies whether the filter is active or not (default: false)                           |
| sapcxsinglesignon.filter.login.userClientId                  | String  | the SAP Commerce client ID for your single page application (required)                   |
| sapcxsinglesignon.filter.idp.issuer                          | String  | the registered issuer, eg. https://dev-1234.eu.auth0.com/ (required)                     |
| sapcxsinglesignon.filter.idp.jwksUrl                         | String  | if issuer is non-OIDC conform, use this URL for JWKS (optional)                          |
| sapcxsinglesignon.filter.idp.audience                        | String  | the registered API, eg. https://localhost:9002/occ/v2/ (required)                        |
| sapcxsinglesignon.filter.idp.scope                           | String  | the required scopeof the API, if any, eg. shop (optional)                                |
| sapcxsinglesignon.filter.idp.requiredClaims                  | String  | comma-separated list of required claims for a valid token (optional)                     |
| sapcxsinglesignon.filter.idp.clientid                        | String  | the client ID of the application (required)                                              |
| sapcxsinglesignon.filter.idp.claim.id                        | String  | claim name used for user ID mapping (default: email)                                     |
| sapcxsinglesignon.replicate.enabled                          | Boolean | specifies whether the replication is active or not (default: false)                      |
| sapcxsinglesignon.replicate.creation.enabled                 | Boolean | specifies whether the user creation is enabled or not (default: false)                   |
| sapcxsinglesignon.replicate.removal.enabled                  | Boolean | specifies whether the user removal is enabled or not (default: false)                    |
| sapcxsinglesignon.auth0.management.api.audience              | String  | the audience for your machine-to-machine application (required)                          |
| sapcxsinglesignon.auth0.management.api.clientid              | String  | the auth0 client ID for your machine-to-machine application (required)                   |
| sapcxsinglesignon.auth0.management.api.clientsecret          | String  | the auth0 client secret for your machine-to-machine application (required)               |
| sapcxsinglesignon.auth0.customer.connection                  | String  | the authentication connection for customers (default: "Username-Password-Authentication") |
| customer.metadata.prefix                                     | String  | the prefix for application metadata for customers (required, default: commerce)          |
| sapcxsinglesignon.auth0.customer.role                        | String  | the role to assign to newly created customer accounts                                    |
| sapcxsinglesignon.auth0.customer.requireemailverification    | String | specifies if the user needs to verify their email (default: false)                       |
| sapcxsinglesignon.auth0.customer.requirepasswordverification | String | specifies if the user needs to verify their password (default: false)                    |
| sapcxsinglesignon.auth0.customer.useblockedstatus            | Boolean | specifies if the user shall be blocked when disabled in SAP Commerce (default: false)    |

## License

_Licensed under the Apache License, Version 2.0, January 2004_

_Copyright 2023, SAP CX Tools_