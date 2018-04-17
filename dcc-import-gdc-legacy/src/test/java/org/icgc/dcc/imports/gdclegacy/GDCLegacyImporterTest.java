package org.icgc.dcc.imports.gdclegacy;

import com.mongodb.MongoClientURI;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.icgc.dcc.imports.gdclegacy.model.CGHubSequenceRepo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

@Ignore("For development only")
public class GDCLegacyImporterTest {

    GDCLegacyImporter importer;

    @Before
    public void setUp() {
        importer = createImporter();
    }

    @Test
    public void testExecute() throws IOException {
        importer.execute();
    }

    @Test
    public void testFormatGDCLegacyURL() {
        val gdcLegacyUrl = importer.formatGDCLegacyURL("82f4c181-e022-4799-9f7f-01abcdc3803e");
        Assert.assertEquals(gdcLegacyUrl, "https://portal.gdc.cancer.gov/legacy-archive/search/f?filters=%7B%22op%22:%22and%22,%22content%22:%5B%7B%22op%22:%22in%22,%22content%22:%7B%22field%22:%22cases.case_id%22,%22value%22:%5B%2282f4c181-e022-4799-9f7f-01abcdc3803e%22%5D%7D%7D%5D%7D");
    }

    @Test
    public void testMakeRepoItem() {
        ImmutablePair itemIds = new ImmutablePair<>("TCGA-XF-AAMQ-01A","82f4c181-e022-4799-9f7f-01abcdc3803e");
        CGHubSequenceRepo repo = importer.makeRepoItem(itemIds);
        Assert.assertEquals(repo.getSpecimenId(), "TCGA-XF-AAMQ-01A");
        Assert.assertEquals(repo.getGdcId(), "82f4c181-e022-4799-9f7f-01abcdc3803e");
        Assert.assertEquals(repo.getGdcLegacyUrl(), "https://portal.gdc.cancer.gov/legacy-archive/search/f?filters=%7B%22op%22:%22and%22,%22content%22:%5B%7B%22op%22:%22in%22,%22content%22:%7B%22field%22:%22cases.case_id%22,%22value%22:%5B%2282f4c181-e022-4799-9f7f-01abcdc3803e%22%5D%7D%7D%5D%7D");
    }

    private GDCLegacyImporter createImporter() {
        return new GDCLegacyImporter(new MongoClientURI("mongodb://localhost:27017/dcc-gdc-legacy"));
    }
}
