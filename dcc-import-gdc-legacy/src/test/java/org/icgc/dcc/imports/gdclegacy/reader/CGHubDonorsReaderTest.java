package org.icgc.dcc.imports.gdclegacy.reader;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

@Slf4j
public class CGHubDonorsReaderTest {

    private String esURL = "10.30.129.4"; // dev ES cluster
    private String esIndex = "icgc26-27"; // dev ES index

    @Test
    public void testRead() {
        CGHubDonorsReader.read(esURL, esIndex, 10);
    }
}
