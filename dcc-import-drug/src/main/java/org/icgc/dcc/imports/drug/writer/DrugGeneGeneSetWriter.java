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
 * TO, PROCUREMENT OF SUBSTITUTE drugODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.imports.drug.writer;

import static org.icgc.dcc.common.core.util.Formats.formatCount;
import static org.icgc.dcc.imports.core.util.Genes.getGeneId;
import static org.icgc.dcc.imports.drug.util.Drugs.getDrugGeneId;
import static org.icgc.dcc.imports.drug.util.Drugs.getDrugGenes;
import static org.icgc.dcc.imports.drug.util.Drugs.getName;
import static org.icgc.dcc.imports.drug.util.Drugs.getZincId;
import static org.icgc.dcc.imports.geneset.model.GeneSetAnnotation.DIRECT;
import static org.icgc.dcc.imports.geneset.model.GeneSetType.DRUG;

import java.util.List;
import java.util.Set;

import org.elasticsearch.common.collect.Sets;
import org.icgc.dcc.imports.geneset.model.GeneSetType;
import org.icgc.dcc.imports.geneset.model.gene.GeneGeneSet;
import org.icgc.dcc.imports.geneset.writer.AbstractGeneGeneSetWriter;
import org.jongo.MongoCollection;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DrugGeneGeneSetWriter extends AbstractGeneGeneSetWriter {

  public DrugGeneGeneSetWriter(@NonNull MongoCollection geneCollection) {
    super(geneCollection, DRUG);
  }

  public void write(@NonNull List<ObjectNode> drugs) {
    log.info("Clearing gene drug gene sets...");
    clearGeneGeneSets();

    log.info("Adding drugs to gene documents...");
    int updateGeneCount = 0;
    
    for (val gene : getGeneWithUniprots()) {
      val geneId = getGeneId(gene);

      val geneSets = resolveGeneSets(drugs, geneId);
      if (!geneSets.isEmpty()) {
        addGeneSets(gene, geneSets);
        saveGene(gene);
        
        updateGeneCount++;
        val status = updateGeneCount % 100 == 0;
        if (status) {
          log.info("Updated drugs for {} genes", formatCount(updateGeneCount));
        }
      }
    }
    
    log.info("Finished writing drugs for {} genes total", formatCount(updateGeneCount));
  }

  private Set<GeneGeneSet> resolveGeneSets(List<ObjectNode> drugs, String geneId) {
    val geneSets = Sets.<GeneGeneSet> newHashSet();
    for (val drug : drugs) {
      for (val drugGene : getDrugGenes(drug)) {
        val drugGeneId = getDrugGeneId(drugGene);
        if (drugGeneId.equals(geneId)) {
          geneSets.add(createGeneSet(drug));
        }
      }
    }

    return geneSets;
  }

  private GeneGeneSet createGeneSet(ObjectNode drug) {
    return GeneGeneSet.builder()
        .id(getZincId(drug))
        .name(getName(drug))
        .type(GeneSetType.DRUG)
        .annotation(DIRECT)
        .build();
  }

}
