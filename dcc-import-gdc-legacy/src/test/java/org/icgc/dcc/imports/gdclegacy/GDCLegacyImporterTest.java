package org.icgc.dcc.imports.gdclegacy;

import com.mongodb.MongoClientURI;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import lombok.val;

@Ignore("For development only")
public class GDCLegacyImporterTest {
    @Test
    public void testExecute() throws IOException {
        val importer = createImporter();

        importer.execute();
    }

    private GDCLegacyImporter createImporter() {
        return new GDCLegacyImporter(new MongoClientURI("mongodb://localhost:27017/dcc-gdc-legacy"));
    }
}
