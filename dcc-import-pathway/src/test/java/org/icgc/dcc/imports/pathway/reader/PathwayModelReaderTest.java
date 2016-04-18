package org.icgc.dcc.imports.pathway.reader;

import static org.icgc.dcc.imports.pathway.PathwayImporter.REMOTE_REACTOME_PATHWAY_HIER_URL;
import static org.icgc.dcc.imports.pathway.PathwayImporter.REMOTE_REACTOME_PATHWAY_SUMMATION_URL;
import static org.icgc.dcc.imports.pathway.PathwayImporter.REMOTE_REACTOME_UNIPROT_URL;

import java.io.IOException;

import org.icgc.dcc.imports.pathway.core.PathwayModel;
import org.icgc.dcc.imports.pathway.util.PathwayGeneSetBuilder;
import org.junit.Ignore;
import org.junit.Test;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Ignore("For development only")
public class PathwayModelReaderTest {

  @Test
  public void testRead() throws IOException {
    val model = readModel();
    val geneSetBuilder = new PathwayGeneSetBuilder(model);

    for (val reactomeId : model.getReactomeIds()) {
      val geneSet = geneSetBuilder.build(reactomeId);
      log.info("Gene set: {}", geneSet);
    }
  }

  private PathwayModel readModel() throws IOException {
    val reader = new PathwayModelReader();
    return reader.read(
        REMOTE_REACTOME_UNIPROT_URL,
        REMOTE_REACTOME_PATHWAY_SUMMATION_URL,
        REMOTE_REACTOME_PATHWAY_HIER_URL);
  }

}
