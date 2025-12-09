# SAP CX Single-Sign-On (Auth0 Replication)

The `sapcxssoauth0` extension provides core replication mechanism for synchronizing customer data with
external service and identity providers, in this case Auth0 by Okta.

## FEATURE DESCRIPTION

The extension ships a customer replication strategy, that can be activated to create users within
Auth0, whenever a customer object is created or changed within SAP commerce cloud.

### How to activate and use

To activate the functionality, one needs to set the configuration parameters accordingly for each environment,
especially the flags `sapcxsinglesignon.replicate.creation.enabled`, and
`sapcxsinglesignon.replicate.removal.enabled` which are set to `false` by default.

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

_Copyright 2025, SAP CX Tools_