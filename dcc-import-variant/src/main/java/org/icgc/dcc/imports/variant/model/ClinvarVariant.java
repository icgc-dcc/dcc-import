package org.icgc.dcc.imports.variant.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * <p>
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
@Data
public class ClinvarVariant {
  @NonNull private int alleleID;
  @NonNull private String variantType;
  @NonNull private String name;
  @NonNull private String geneID;
  @NonNull private String geneSymbol;
  @NonNull private String hgncID;
  @NonNull private String clinicalSignificance;
  @NonNull private String clinSigSimple;
  @NonNull private String lastEvaluated;
  @NonNull private String rsNumber;
  @NonNull private String nsvEsv;
  @NonNull private String rcVaccession;
  @NonNull private String phenotypeIDS;
  @NonNull private String phenotypeList;
  @NonNull private String origin;
  @NonNull private String originSimple;
  @NonNull private String assembly;
  @NonNull private String chromosomeAccession;
  @NonNull private String chromosome;
  @NonNull private long start;
  @NonNull private long stop;
  @NonNull private String referenceAllele;
  @NonNull private String alternateAllele;
  @NonNull private String cytogenetic;
  @NonNull private String reviewStatus;
  @NonNull private int numberSubmitters;
  @NonNull private String guidelines;
  @NonNull private String testedInGTR;
  @NonNull private String otherIDs;
  @NonNull private String submitterCategories;
  @NonNull private int variationID;
  @NonNull private String variationType;
  @NonNull private boolean interpreted;

  public static class Builder {
    public static ClinvarVariant build(ClinvarVariantSummary summary, ClinvarVariationAllele allele) {
      return new ClinvarVariant(
          summary.getAlleleID(),
          summary.getVariantType(),
          summary.getName(),
          summary.getGeneID(),
          summary.getGeneSymbol(),
          summary.getHgncID(),
          summary.getClinicalSignificance(),
          summary.getClinSigSimple(),
          summary.getLastEvaluated(),
          summary.getRsNumber(),
          summary.getNsvEsv(),
          summary.getRcVaccession(),
          summary.getPhenotypeIDS(),
          summary.getPhenotypeList(),
          summary.getOrigin(),
          summary.getOriginSimple(),
          summary.getAssembly(),
          summary.getChromosomeAccession(),
          summary.getChromosome(),
          summary.getStart(),
          summary.getStop(),
          summary.getReferenceAllele(),
          summary.getAlternateAllele(),
          summary.getCytogenetic(),
          summary.getReviewStatus(),
          summary.getNumberSubmitters(),
          summary.getGuidelines(),
          summary.getTestedInGTR(),
          summary.getOtherIDs(),
          summary.getSubmitterCategories(),
          allele.getVariationID(),
          allele.getVariationType(),
          allele.isInterpreted()
      );
    }
  }
}
