# ICGC DCC - Import

Parent project of the importer modules.

The Import project import reference data, which is used by the [DCC ETL](https://github.com/icgc-dcc/dcc-etl) project, into `MongoDB`.

## Build

To compile, test and package the system, execute the following from the root of the repository:

```shell
mvn
```

## Modules

The Import system is comprised of the following modules.

### Common

This module is the shared understanding of the system.

- [Core](dcc-import-core/README.md)

### Client

This is the main entry point of the application.

- [Client](dcc-import-client/README.md)

### Sources

These modules import various data sources.

#### Gene Model
- [Gene](dcc-import-gene/README.md)

#### Gene Sets
- [Gene Set](dcc-import-geneset/README.md)
- [CGC](dcc-import-cgc/README.md)
- [GO](dcc-import-go/README.md)
- [Drug](dcc-import-drug/README.md)
- [Pathway](dcc-import-pathway/README.md)

#### Other
- [Diagram](dcc-import-diagram/README.md)
- [Project](dcc-import-project/README.md)

## Resources

For information on static resources used by this project, see [RESOURCES.md](RESOURCES.md).

## Installation

For automated deployment and installation of the infrastructure and software components, please consult the [dcc-cm](https://github.com/icgc-dcc/dcc-cm/blob/develop/ansible/README.md) project.

## Changes

Change log for the user-facing system modules may be found in [CHANGES.md](CHANGES.md).

## License

Copyright and license information may be found in [LICENSE.md](LICENSE.md).
