/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
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
package org.icgc.dcc.imports.drug.reader;

import java.util.HashMap;
import java.util.Map;

import org.icgc.dcc.common.core.model.ReleaseCollection;
import org.jongo.Jongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClient;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneReader extends Reader{
  
  private final static String geneUrl = "http://files.docking.org/export/oicr/genes.ldjson";
  
  public GeneReader() {
    super(geneUrl);
  }
  
  public MappingIterator<ObjectNode> getGenes() {
    val genes = getJson();
    return genes;
  }
  
  @SneakyThrows
  public Map<String, ObjectNode> getGeneMap() {
    
    log.info("Loading ICGC Gene Info");
    val db = new MongoClient().getDB("dcc-genome");
    Jongo jongo = new Jongo(db);
    val geneCollection = jongo.getCollection(ReleaseCollection.GENE_COLLECTION.getId());
    
    val geneMap = new HashMap<String, ObjectNode>();
    getJson().forEachRemaining(gene -> {
      JsonNode icgcGene = geneCollection.findOne("{external_db_ids.uniprotkb_swissprot:#}", gene.get("uniprot").asText()).as(JsonNode.class);
      if (icgcGene!=null) {
        gene.put("ensembl_gene_id", icgcGene.get("_gene_id").asText());
      } else {
        log.info("Could not find matching Gene for uniprot value: {}", gene.get("uniprot").asText());
        gene.put("ensembl_gene_id", "");
      }
      geneMap.put(gene.get("gene_name").asText(), gene);
    });
    
    return geneMap;
  } 
}