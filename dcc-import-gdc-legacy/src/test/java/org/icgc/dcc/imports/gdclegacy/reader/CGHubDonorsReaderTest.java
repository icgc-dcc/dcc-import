package org.icgc.dcc.imports.gdclegacy.reader;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

@Slf4j
public class CGHubDonorsReaderTest {

    private String esURL = "10.30.129.4"; // dev ES cluster
    private String esIndex = "icgc26-27"; // dev ES index

    @Test
    public void testRead() {
        val sequenceIds = CGHubDonorsReader.read(esURL, esIndex, 10);
        assertThat(sequenceIds, not(IsEmptyCollection.empty()));
    }
}
