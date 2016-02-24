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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.SneakyThrows;
import lombok.val;

/**
 * Responsible for creating a map of gene to external db ids
 */
public class ExternalReader {

  /**
   * Constants
   */
  private static final String OBJECT_XREF_URI =
      "ftp://ftp.ensembl.org/pub/grch37/release-82/mysql/homo_sapiens_core_82_37/object_xref.txt.gz";
  private static final Pattern TSV = Pattern.compile("\t");
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @SneakyThrows
  public static Map<String, ObjectNode> externalIds() {
    val gzip = new GZIPInputStream(new URL(OBJECT_XREF_URI).openStream());
    val inputStreamReader = new InputStreamReader(gzip);
    val bufferedReader = new BufferedReader(inputStreamReader);

    val geneIdMap = GeneReader.geneIdMap();
    val transToGeneMap = TransReader.translationToGene();

    val retMap = new HashMap<String, ObjectNode>();
    for (String s = bufferedReader.readLine(); null != s; s = bufferedReader.readLine()) {
      s = s.trim();
      if (s.length() > 0) {
        String[] line = TSV.split(s);
        if ("Gene".equals(line[2])) {
          val geneId = geneIdMap.get(line[1]);

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

          val xrefId = line[3];
          if (NameReader.entrezMap.containsKey(xrefId)) {
            val arrayNode = (ArrayNode) externalDbs.get("entrez_gene");
            arrayNode.add(NameReader.entrezMap.get(xrefId));

          } else if (NameReader.hgncMap.containsKey(xrefId)) {
            val arrayNode = (ArrayNode) externalDbs.get("hgnc");
            arrayNode.add(NameReader.hgncMap.get(xrefId));

          } else if (NameReader.mimMap.containsKey(xrefId)) {
            val arrayNode = (ArrayNode) externalDbs.get("omim_gene");
            arrayNode.add(NameReader.mimMap.get(xrefId));
          }

        } else if ("Translation".equals(line[2])) {
          val geneId = geneIdMap.get(transToGeneMap.get(line[1]));

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

          val xrefId = line[3];
          if (NameReader.uniprotMap.containsKey(xrefId)) {
            val arrayNode = (ArrayNode) externalDbs.get("uniprotkb_swissprot");
            val uniprot = NameReader.uniprotMap.get(xrefId);

            val iter = arrayNode.elements();
            boolean contained = false;
            while (iter.hasNext()) {
              contained = iter.next().asText().equals(uniprot);
            }

            if (!contained) {
              arrayNode.add(uniprot);
            }

          }

        }

      }
    }

    return retMap;

  }

}
