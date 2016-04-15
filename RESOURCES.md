# ICGC DCC - Import Resources

This details the updating of static resources used by the importer modules. This is done offline because these files have been known to change schemas and break the import process. Doing it this way leads to greater runtime stability

### Last Updated:

- `cancer_gene_census.tsv`: *Oct 9th, 2015*.
- `pathway_hier.txt`: *Oct 9th, 2015*.
- `uniprot_2_reactome.txt`: *Oct 9th, 2015*.
- `pathway_2_summation.txt`: *Oct 9th, 2015*.

#### 2. Update the files.

##### 2.1. Reactome Pathway Resources

Refer to this [wiki page](https://wiki.oicr.on.ca/display/DCCSOFT/Reactome+Pathway+Update+-+Nov+2014) for complete information.

```shell
curl http://www.reactome.org/ReactomeRESTfulAPI/RESTfulWS/pathwayHierarchy/homo+sapiens > pathway_hierarchy.txt
curl http://www.reactome.org/download/current/UniProt2Reactome.txt > uniprot_2_reactome.txt
curl http://www.reactome.org/download/current/pathway2summation.txt > pathway_2_summation.txt
```

##### 2.2. Cancer Gene Census

Download Cancer Gene Census file from [COSMIC](https://cancer.sanger.ac.uk/census). Save it as `cancer_gene_census.tsv`. You need to register and login to download the file.

The file might be available on the `csv` format. It can be converted to the `tsv` one with command:

```shell
tr ',' '\t' < cancer_gene_census.csv > cancer_gene_census.tsv
```

### 2. Verify Resources.

Unfortunately, the updated files are usually not in the right format or consistency. So some manual work is needed to make them compatible with ETL component. Based on previous experiences, these are some items to look out for:

- `cancer_gene_census.tsv` file might have csv header. Just replace the commas with tab character in a text editor.

- Reactome names are present in `pathway_hierarchy.txt` but missing from `pathway_2_summation.txt`. You'd need to resolve them using `uniprot_2_reactome.txt`. Start by copying the lines with '???' from the end of previous `pathway_2_summation.txt` to the new verison. For each one of those, search for the REACT_[id] in the file to see if the data is provided in the current version. If so, delete the lines.

Currently, the following reactome names are inconsistent between the reactome data files and have been resolved with other methods:

- The following reactome names are present in `pathway_hierarchy.txt` but missing from `pathway_2_summation.txt` and have been resolved using `uniprot_2_reactome.txt`:
  - PI3K Cascade
  - RNA Polymerase II Transcription
  - S6K1-mediated signalling
  - Switching of origins to a post-replicative state
  - mTOR signalling

- The following reactome names are present in `pathway_hierarchy.txt` but missing from `pathway_2_summation.txt` and `uniprot_2_reactome.txt` have been resolved using reactome.org website:
  - Acetylcholine Binding And Downstream Events
  - Cell Cycle
  - Cell junction organization
  - Mitotic G1-G1/S phases
  - Mitotic G2-G2/M phases
  - RNA Polymerase II Transcription
  - Regulation of mitotic cell cycle
  - Transmembrane transport of small molecules
  - mTORC1-mediated signalling
  - Infectious disease
  - Vesicle-mediated transport

- The following reactome ids are present in `uniprot_2_reactome.txt` but missing from the other 2 files.
  - REACT_790
  - REACT_1451
  - REACT_330
  - REACT_2204
  - REACT_1156
  - REACT_329
  - REACT_22107
  - REACT_22201
  - REACT_1178
  - REACT_63
  - REACT_6772
  - REACT_1993
  - REACT_1156

### 3. Update the contents at the following paths

- [dcc-import-pathway/src/main/resources/](dcc-import-cgc/src/main/resources/)
- [dcc-import-pathway/src/main/resources/](dcc-import-pathway/src/main/resources/)


#### 4.3. Run `dcc-import` tests
`dcc-import` modules heavily depends on the jar resource, so running the unit tests is the first step to catch issues with updates bundle. Run the tests and try to resolve the issues. You might get an error similar to following:

```
java.lang.NullPointerException: Cannot find reactome id for pathway segment with reactome name 'Infectious disease' and segment 'PathwaySegment(reactomeId=null, reactomeName=Infectious disease, diagrammed=true)'
```
In which case, you might need to go to [reactome website](http://www.reactome.org/) and search for the missing reactome name and finding the corresponding reactome id such as `REACT_355497` and add the combination to the buttom of `pathway_2_summation.txt`.


#### 4.5. Run all Import tests
```shell
cd dcc-import
mvn clean package
```

### 6. Update this document.
Reflect the changes and their date.
