package org.icgc.dcc.imports.drug.reader;

import java.io.IOException;
import org.junit.Test;

import junit.framework.TestCase;
import lombok.val;

public class DrugReaderTest extends TestCase {
  
  public DrugReader drugReader = new DrugReader();
  
  @Test
  public void testDrugReader1() throws IOException {
    val drugs = drugReader.getDrugs();
    assertNotNull(drugs);
    //drugs.forEachRemaining(drug -> System.out.println(drug));
  }
  
}