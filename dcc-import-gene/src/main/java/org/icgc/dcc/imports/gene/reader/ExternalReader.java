/*
 * Copyright (c) 2016 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.imports.gene.reader;

import static org.icgc.dcc.common.json.Jackson.DEFAULT;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.icgc.dcc.imports.gene.model.GeneMapping;
import org.icgc.dcc.imports.gene.model.TranslationMapping;
import org.icgc.dcc.imports.gene.model.XrefMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import lombok.val;

/**
 * Responsible for creating a map of gene to external db ids
 */
public final class ExternalReader extends TsvReader {

  /**
   * Dependencies
   */
  private final XrefMapping xrefMapping;
  private final Map<String, String> getIdMap;
  private final Map<String, String> translationToGene;

  public ExternalReader(String uri,
      @NonNull XrefMapping xrefMapping,
      @NonNull GeneMapping geneMapping,
      @NonNull TranslationMapping translationMapping) {
    super(uri);
    this.xrefMapping = xrefMapping;
    this.getIdMap = geneMapping.getGeneIdMap();
    this.translationToGene = translationMapping.getTranslationToGene();

  }

  public Map<String, ObjectNode> read() {
    val externalIds = new HashMap<String, ObjectNode>();

    readRecords().forEach(record -> {
      if (isGene(record)) {
        handleExternalGeneIds(record, externalIds);
      } else if (isTranslation(record)) {
        handleUniprotIds(record, externalIds);
      }
    });

    return ImmutableMap.<String, ObjectNode> copyOf(externalIds);
  }

  private void handleExternalGeneIds(List<String> record, Map<String, ObjectNode> externalIds) {
    val xrefId = getXrefId(record);
    String geneId = getIdMap.get(getItemId(record));

    ObjectNode externalDbs = getExternalDbs(geneId, externalIds);

    if (xrefMapping.getEntrezMap().containsKey(xrefId)) {
      ArrayNode arrayNode = (ArrayNode) externalDbs.get("entrez_gene");
      arrayNode.add(xrefMapping.getEntrezMap().get(xrefId));
    } else if (xrefMapping.getHgncMap().containsKey(xrefId)) {
      ArrayNode arrayNode = (ArrayNode) externalDbs.get("hgnc");
      arrayNode.add(xrefMapping.getHgncMap().get(xrefId));
    } else if (xrefMapping.getMimMap().containsKey(xrefId)) {
      ArrayNode arrayNode = (ArrayNode) externalDbs.get("omim_gene");
      arrayNode.add(xrefMapping.getMimMap().get(xrefId));
    }
  }

  private void handleUniprotIds(List<String> record, Map<String, ObjectNode> externalIds) {
    // Uniprot Ids are for proteins, which means we match them to translations and eventually work up to a gene/
    String geneId = getIdMap.get(translationToGene.get(getItemId(record)));
    ObjectNode externalDbs = getExternalDbs(geneId, externalIds);

    String xrefId = getXrefId(record);
    if (xrefMapping.getUniprotMap().containsKey(xrefId)) {
      ArrayNode arrayNode = (ArrayNode) externalDbs.get("uniprotkb_swissprot");
      String uniprot = xrefMapping.getUniprotMap().get(xrefId);

      Iterator<JsonNode> iter = arrayNode.elements();
      boolean contained = false;
      while (iter.hasNext()) {
        contained = iter.next().asText().equals(uniprot);
      }

      if (!contained) {
        arrayNode.add(uniprot);
      }
    }
  }

  private String getXrefId(List<String> record) {
    return record.get(3);
  }

  private String getType(List<String> record) {
    return record.get(2);
  }

  private String getItemId(List<String> record) {
    return record.get(1);
  }

  private boolean isGene(List<String> record) {
    return "Gene".equals(getType(record));
  }

  private boolean isTranslation(List<String> record) {
    return "Translation".equals(getType(record));
  }

  private ObjectNode getExternalDbs(String geneId, Map<String, ObjectNode> externalIds) {
    if (externalIds.containsKey(geneId)) {
      return externalIds.get(geneId);
    } else {
      val externalDbs = DEFAULT.createObjectNode();
      externalDbs.put("entrez_gene", DEFAULT.createArrayNode());
      externalDbs.put("hgnc", DEFAULT.createArrayNode());
      externalDbs.put("omim_gene", DEFAULT.createArrayNode());
      externalDbs.put("uniprotkb_swissprot", DEFAULT.createArrayNode());
      externalIds.put(geneId, externalDbs);
      return externalDbs;
    }
  }

}