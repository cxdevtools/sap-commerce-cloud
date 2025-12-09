# SAP CX Search

The `sapcxsearch` extension improves the solr facet search functions.

## FEATURE DESCRIPTION

- Configurable version of Solr Indexer Job allowing to specify the indexed type
- A `CxIndexer` interface that can be used to trigger the indexing process
- A `CxIndexerService` that can be used to resolve indexers and trigger the indexing process
- A list of different search providers and resolvers that can be used for indexing

### How to activate and use

- the configurable version of the solr indexer job can be used within the backoffice
- add a bean configuration for the providers and resolvers within your project
- the `CxIndexerService` and `CxIndexer`can be used to trigger the indexing process

## License

_Licensed under the Apache License, Version 2.0, January 2004_

_Copyright 2025, SAP CX Tools_