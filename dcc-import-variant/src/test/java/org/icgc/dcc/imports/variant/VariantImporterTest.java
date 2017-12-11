package org.icgc.dcc.imports.variant;

import com.mongodb.MongoClientURI;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import lombok.val;

//@Ignore("For development only")
public class VariantImporterTest {
    @Test
    public void testExecute() throws IOException {
        val importer = createImporter();;

        importer.execute();
    }

    private VariantImporter createImporter() {
        return new VariantImporter(new MongoClientURI("mongodb://localhost:27017/dcc-import"));
    }
}
