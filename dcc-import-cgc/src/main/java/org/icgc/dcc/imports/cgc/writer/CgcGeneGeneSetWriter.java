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
package org.icgc.dcc.imports.cgc.writer;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static org.icgc.dcc.common.core.model.FieldNames.GENE_ID;
import static org.icgc.dcc.common.core.model.FieldNames.GENE_SYMBOL;
import static org.icgc.dcc.common.core.util.Splitters.COMMA;
import static org.icgc.dcc.imports.cgc.writer.CgcGeneSetWriter.CGS_GENE_SET_ID;
import static org.icgc.dcc.imports.cgc.writer.CgcGeneSetWriter.CGS_GENE_SET_NAME;
import static org.icgc.dcc.imports.geneset.model.GeneSetAnnotation.DIRECT;
import static org.icgc.dcc.imports.geneset.model.GeneSetType.CURATED_SET;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.imports.cgc.model.CgcGene;
import org.icgc.dcc.imports.geneset.model.gene.GeneGeneSet;
import org.icgc.dcc.imports.geneset.writer.AbstractGeneGeneSetWriter;
import org.jongo.MongoCollection;

import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CgcGeneGeneSetWriter extends AbstractGeneGeneSetWriter {

  public CgcGeneGeneSetWriter(@NonNull MongoCollection geneCollection) {
    super(geneCollection, CURATED_SET);
  }

  public void write(@NonNull Iterable<Map<String, String>> cgc) {
    log.info("Clearing gene CGC gene sets...");
    clearGeneGeneSets();

    log.info("Updating gene CGC gene sets...");
    val count = updateGeneGeneSets(cgc, geneCollection);
    log.info("Updated {} gene CGC gene sets", count);

    checkState(count > 0, "Did not update any CGC gene sets. Is the gene collection missing?");
  }

  /**
   * @see https://jira.oicr.on.ca/browse/DCC-4652
   */
  private int updateGeneGeneSets(Iterable<Map<String, String>> cgc, MongoCollection geneCollection) {
    int count = 0;
    for (val cgcGene : cgc) {
      val geneSymbol = resolveGeneSymbol(cgcGene);

      // First try symbol
      int n = updateByGeneSymbol(geneCollection, geneSymbol);
      if (n == 0) {
        log.warn("Could not find with gene symbol {} to associate with CGC gene: {}", geneSymbol, cgcGene);

        // Next try by id
        val geneId = resolveEnsembleId(cgcGene);
        if (geneId == null) {
          log.warn("-> Can not resolve gene id to associate with CGC gene: {}", cgcGene);
          continue;
        }

        n = updateByGeneId(geneCollection, geneId);
        if (n == 0) {
          log.warn("-> Could not find with gene id {} to associate with CGC gene: {}", geneId, cgcGene);
        }
      }

      count += n;
    }

    return count;
  }

  private int updateByGeneSymbol(MongoCollection geneCollection, String geneSymbol) {
    val result = geneCollection.update("{ " + GENE_SYMBOL + ": # }", geneSymbol)
        .multi()
        .with("{ $addToSet: { " + type.getFieldName() + ": # } }", createGeneGeneSet());

    return result.getN();
  }

  private int updateByGeneId(MongoCollection geneCollection, String geneId) {
    val result = geneCollection.update("{ " + GENE_ID + ": # }", geneId)
        .multi()
        .with("{ $addToSet: { " + type.getFieldName() + ": # } }", createGeneGeneSet());

    return result.getN();
  }

  private static String resolveEnsembleId(Map<String, String> cgcGene) {
    val synonyms = resolveSynonyms(cgcGene);
    for (val synonym : synonyms) {
      if (isEnsemblId(synonym)) {
        return synonym;
      }
    }

    return null;
  }

  private static String resolveGeneSymbol(Map<String, String> cgcGene) {
    val value = cgcGene.get(CgcGene.CGC_GENE_SYMBOL_FIELD_NAME);
    checkState(!isNullOrEmpty(value), "Gene symbol missing: %s, %s", value, cgcGene);

    return value;
  }

  private static List<String> resolveSynonyms(Map<String, String> cgcGene) {
    val value = cgcGene.get(CgcGene.CGC_SYNONYMS_FIELD_NAME);
    if (isNullOrEmpty(value)) {
      return emptyList();
    }

    return COMMA.splitToList(value);
  }

  private static boolean isEnsemblId(String synonym) {
    return synonym.startsWith("ENSG");
  }

  private static GeneGeneSet createGeneGeneSet() {
    return GeneGeneSet.builder()
        .id(CGS_GENE_SET_ID)
        .name(CGS_GENE_SET_NAME)
        .type(CURATED_SET)
        .annotation(DIRECT)
        .build();
  }

}
