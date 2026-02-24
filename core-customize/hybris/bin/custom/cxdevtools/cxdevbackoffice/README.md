# CX DEV Backoffice

The `cxdevbackoffice` extension improves the experience with the CX DEV Backoffice Application.

## FEATURE DESCRIPTION

This extension provides the common backoffice configuration for CX DEV Tools, e.g.
the base node in the tree navigation with the ID `cxdevtools_treenode_main`.

In addition, it contributes two major features to the backoffice application.

### Configurable Backoffice Locale Service

The `ConfigurableBackofficeLocaleService` introduces the configuration possibilities
for activating a sort order based on the ISO code, such that all data locales appear
in the order of their ISO code within the backoffice cockpit.

In addition, the UI locales can be limited by define a comma-separated list of ISO codes.
This way, the login screen will no longer allow to use every available language for login
but only those specified within this property.

### GenericItemSyncRelatedItemsVisitor

Generic implementation of `ItemVisitor` to avoid that new `ItemVisitor` classes need to
be introduced for each and every relation that influences the sync status of an item.
Instead, one can configure the relation by introducing a platform property with the
following convention:

`cxdevbackoffice.sync.relateditems.<ItemType>=<AttributeId>,<AttributeId>,...`

For example, the following relations would be valid:

- `cxdevbackoffice.sync.relateditems.Product=mymedia,prospect,reference`
- `cxdevbackoffice.sync.relateditems.Category=heroProduct`


### How to activate and use

To activate this feature, simply set the `i18n.data.sortbyisocode` property to `true` and/or
specify the UI locales for login as a comma-separated list using the `i18n.ui.locales` property.

For the sync status updates, one need to configure a property using the convention:
`sync.relateditems.<ItemType>=<AttributeId>,<AttributeId>,...`


### Configuration parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| cxdevbackoffice.i18n.data.sortbyisocode | boolean | Order languages of localized fields by their ISO code in backoffice administration (default: false) |
| cxdevbackoffice.i18n.ui.locales         | String  | Limit UI languages for backoffice login. If empty, use all available languages (default: empty) |
| cxdevbackoffice.sync.relateditems.*     | String  | Sync status configuration for items with related items that should update the sync status (one line per type) |


## License

_Licensed under the Apache License, Version 2.0, January 2004_

_Copyright 2026, CX DEV Tools_