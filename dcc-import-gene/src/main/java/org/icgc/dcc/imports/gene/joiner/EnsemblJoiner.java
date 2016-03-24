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
package org.icgc.dcc.imports.gene.joiner;

import static org.icgc.dcc.common.core.json.Jackson.DEFAULT;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asText;

import java.util.Map;

import org.icgc.dcc.imports.gene.model.Ensembl;
import org.icgc.dcc.imports.gene.model.ProteinFeature;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Multimap;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class EnsemblJoiner implements GeneJoiner {

  private final Ensembl ensembl;

  @Override
  public ObjectNode join(ObjectNode gene) {
    gene.put("name", getName(gene));
    gene.put("synonyms", getSynonyms(asText(gene, "_gene_id")));
    gene.put("canonical_transcript_id", canonicalMap().get(asText(gene, "_gene_id")));
    gene.put("external_db_ids", externalIds().get(asText(gene, "_gene_id")));

    val transcripts = (ArrayNode) gene.get("transcripts");
    transcripts.forEach(transcript -> attachProteinFeatures((ObjectNode) transcript));

    return gene;
  }

  private void attachProteinFeatures(ObjectNode transcript) {
    val pfs = pFeatures().get(asText(transcript, "id"));
    val domains = DEFAULT.createArrayNode();

    if (pfs != null) {
      for (val p : pfs) {
        val domain = DEFAULT.createObjectNode();
        domain.put("interpro_id", p.getInterproId());
        domain.put("hit_name", p.getHitName());
        domain.put("gff_source", p.getGffSource());
        domain.put("description", p.getDescription());
        domain.put("start", p.getStart());
        domain.put("end", p.getEnd());
        domains.add(domain);
      }
    }

    transcript.put("domains", domains);
  }

  private String getName(ObjectNode gene) {
    return nameMap().getOrDefault(asText(gene, "symbol"), asText(gene, "symbol"));
  }

  private ArrayNode getSynonyms(String id) {
    return synMap().getOrDefault(id, DEFAULT.createArrayNode());
  }

  /*
   * Ensembl Helpers
   */
  private Map<String, String> nameMap() {
    return this.ensembl.getNameMap();
  }

  private Map<String, String> canonicalMap() {
    return this.ensembl.getCanonicalMap();
  }

  private Map<String, ObjectNode> externalIds() {
    return this.ensembl.getExternalIds();
  }

  private Map<String, ArrayNode> synMap() {
    return this.ensembl.getSynonymMap();
  }

  private Multimap<String, ProteinFeature> pFeatures() {
    return this.ensembl.getPFeatures();
  }

}
