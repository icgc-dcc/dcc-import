# ICGC DCC - Import Client

Imports collections of data from external sources into a MongoDB databases which is later imported into release databases during a release run.

## Build

To compile, test and package the module, execute the following from the root of the repository:

```shell
mvn -am -pl dcc-import/dcc-import-core
```

## Run

	$ dcc-import-client.sh [options]
E.g.

	$ dcc-import-client.sh --imports.sources=GENES
	
## Update

Use the `bin/install` script to update the client library.
For example to update to the latest release run:

	$ install -l latest
