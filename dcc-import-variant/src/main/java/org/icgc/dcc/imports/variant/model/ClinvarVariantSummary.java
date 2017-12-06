package org.icgc.dcc.imports.variant.model;

import lombok.*;

import java.util.List;

import static org.icgc.dcc.common.core.util.Splitters.TAB;

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
public class ClinvarVariantSummary {
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
  @NonNull private String start;
  @NonNull private String stop;
  @NonNull private String referenceAllele;
  @NonNull private String alternateAllele;
  @NonNull private String cytogenetic;
  @NonNull private String reviewStatus;
  @NonNull private String numberSubmitters;
  @NonNull private String guidelines;
  @NonNull private String testedInGTR;
  @NonNull private String otherIDs;
  @NonNull private String submitterCategories;

  public static class Builder implements VariantModelBuilder<ClinvarVariantSummary> {

    @Override
    public ClinvarVariantSummary build(String line) {
      List<String> items = TAB.splitToList(line);
      return new ClinvarVariantSummary(
          Integer.parseInt(items.get(0)),
          items.get(1),
          items.get(2),
          items.get(3),
          items.get(4),
          items.get(5),
          items.get(6),
          items.get(7),
          items.get(8),
          items.get(9),
          items.get(10),
          items.get(11),
          items.get(12),
          items.get(13),
          items.get(14),
          items.get(15),
          items.get(16),
          items.get(17),
          items.get(18),
          items.get(19),
          items.get(20),
          items.get(21),
          items.get(22),
          items.get(23),
          items.get(24),
          items.get(25),
          items.get(26),
          items.get(27),
          items.get(28),
          items.get(29)
      );
    }
  }
}
