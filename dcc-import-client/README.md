ICGC DCC - Import Client
===

Imports collections of data from external sources into a MongoDB databases which is later imported into release databases during a release run.


Build
---

From the command line:

	mvn package

Run
---

	$ dcc-import-client.sh [options]
E.g.

	$ dcc-import-client.sh --imports.sources=GENES
	
Update
---
Use the `bin/install` script to update the client library.
For example to update to the latest release run:

	$ install -l