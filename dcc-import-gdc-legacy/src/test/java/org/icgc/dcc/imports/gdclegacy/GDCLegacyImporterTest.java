package org.icgc.dcc.imports.gdclegacy;

import com.mongodb.MongoClientURI;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@Ignore("For development only")
public class GDCLegacyImporterTest {

    @Test
    public void testExecute() throws IOException {
//        val importer = createImporter();
//
//        importer.execute();
    }

    private GDCLegacyImporter createImporter() {
        return new GDCLegacyImporter(new MongoClientURI("mongodb://localhost:27017/dcc-gdc-legacy"));
    }
}
