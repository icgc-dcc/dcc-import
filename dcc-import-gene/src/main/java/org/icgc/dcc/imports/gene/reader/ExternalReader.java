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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.val;

/**
 * Responsible for creating a map of gene to external db ids
 */
public final class ExternalReader {

  /**
   * Constants
   */
  private static final String OBJECT_XREF_URI =
      "ftp://ftp.ensembl.org/pub/grch37/release-82/mysql/homo_sapiens_core_82_37/object_xref.txt.gz";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static Map<String, ObjectNode> externalIds() {
    val geneIdMap = GeneReader.geneIdMap();
    val transToGeneMap = TransReader.translationToGene();

    val retMap = new HashMap<String, ObjectNode>();
    BaseReader.read(OBJECT_XREF_URI, line -> {
      if ("Gene".equals(line[2])) {
        String geneId = geneIdMap.get(line[1]);

        ObjectNode externalDbs;
        if (retMap.containsKey(geneId)) {
          externalDbs = retMap.get(geneId);
        } else {
          externalDbs = MAPPER.createObjectNode();
          externalDbs.put("entrez_gene", MAPPER.createArrayNode());
          externalDbs.put("hgnc", MAPPER.createArrayNode());
          externalDbs.put("omim_gene", MAPPER.createArrayNode());
          externalDbs.put("uniprotkb_swissprot", MAPPER.createArrayNode());
          retMap.put(geneId, externalDbs);
        }

        String xrefId = line[3];
        if (NameReader.entrezMap.containsKey(xrefId)) {
          ArrayNode arrayNode = (ArrayNode) externalDbs.get("entrez_gene");
          arrayNode.add(NameReader.entrezMap.get(xrefId));

        } else if (NameReader.hgncMap.containsKey(xrefId)) {
          ArrayNode arrayNode = (ArrayNode) externalDbs.get("hgnc");
          arrayNode.add(NameReader.hgncMap.get(xrefId));

        } else if (NameReader.mimMap.containsKey(xrefId)) {
          ArrayNode arrayNode = (ArrayNode) externalDbs.get("omim_gene");
          arrayNode.add(NameReader.mimMap.get(xrefId));
        }

      } else if ("Translation".equals(line[2])) {
        // Uniprot Ids are for proteins, which means we match them to translations and eventually work up to a gene/

        String geneId = geneIdMap.get(transToGeneMap.get(line[1]));

        ObjectNode externalDbs;
        if (retMap.containsKey(geneId)) {
          externalDbs = retMap.get(geneId);
        } else {
          externalDbs = MAPPER.createObjectNode();
          externalDbs.put("entrez_gene", MAPPER.createArrayNode());
          externalDbs.put("hgnc", MAPPER.createArrayNode());
          externalDbs.put("omim_gene", MAPPER.createArrayNode());
          externalDbs.put("uniprotkb_swissprot", MAPPER.createArrayNode());
          retMap.put(geneId, externalDbs);
        }

        String xrefId = line[3];
        if (NameReader.uniprotMap.containsKey(xrefId)) {
          ArrayNode arrayNode = (ArrayNode) externalDbs.get("uniprotkb_swissprot");
          String uniprot = NameReader.uniprotMap.get(xrefId);

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
    });

    return retMap;

  }

}
