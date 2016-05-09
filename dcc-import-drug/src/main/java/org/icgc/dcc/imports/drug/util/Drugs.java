package org.icgc.dcc.imports.drug.util;

import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class Drugs {
  
  public static String getZincId(ObjectNode drug) {
    return drug.get("zinc_id").asText();
  }

  public static String getName(ObjectNode drug) {
    return drug.get("name").asText();
  }

  public  static String getDrugClass(ObjectNode drug) {
    return drug.get("drug_class").asText();
  }

  public static ArrayNode getDrugGenes(ObjectNode drug) {
    return (ArrayNode) drug.get("genes");
  }
  
  public static String getDrugGeneId(JsonNode drugGene) {
    return drugGene.get("ensembl_gene_id").textValue();
  }

}
