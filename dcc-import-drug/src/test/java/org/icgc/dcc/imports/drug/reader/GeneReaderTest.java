package org.icgc.dcc.imports.drug.reader;

import java.io.IOException;
import org.junit.Test;

import junit.framework.TestCase;
import lombok.val;

public class GeneReaderTest extends TestCase {
  
  public GeneReader geneReader = new GeneReader();
  
  @Test
  public void testDrugReader1() throws IOException {
    val genes = geneReader.getGenes();
    genes.forEachRemaining(drug -> System.out.println(drug));
  }
  
}