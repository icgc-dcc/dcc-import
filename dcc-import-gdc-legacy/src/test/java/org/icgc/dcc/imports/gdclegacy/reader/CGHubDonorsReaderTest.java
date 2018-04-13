package org.icgc.dcc.imports.gdclegacy.reader;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class CGHubDonorsReaderTest {

    private String esURL = "192.168.0.189";
    private String esIndex = "icgc26-27";

    @Test
    public void testQueryES() {
        CGHubDonorsReader.queryES(esURL, esIndex);
    }
}
