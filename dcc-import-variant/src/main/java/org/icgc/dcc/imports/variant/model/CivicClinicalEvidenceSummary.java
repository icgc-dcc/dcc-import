package org.icgc.dcc.imports.variant.model;

import lombok.Data;
import lombok.NonNull;

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
public class CivicClinicalEvidenceSummary {
  @NonNull private String gene;
  @NonNull private String entrezID;
  @NonNull private String variant;
  @NonNull private String disease;
  @NonNull private String doid;
  @NonNull private String drugs;
  @NonNull private String evidenceType;
  @NonNull private String evidenceDirection;
  @NonNull private String evidenceLevel;
  @NonNull private String clinicalImpact;
  @NonNull private String evidenceStatement;
  @NonNull private String pubmedID;
  @NonNull private String citation;
  @NonNull private String rating;
  @NonNull private String evidenceStatus;
  @NonNull private String evidenceID;
  @NonNull private String civicID;
  @NonNull private String geneID;
  @NonNull private String chromosome;
  @NonNull private String chromosomeStart;
  @NonNull private String chromosomeEnd;
  @NonNull private String mutation;
  @NonNull private String representativeTranscript;
  @NonNull private String chromosome2;
  @NonNull private String start2;
  @NonNull private String stop2;
  @NonNull private String representativeTranscript2;
  @NonNull private String ensemblVersion;
  @NonNull private String referenceBuild;
  @NonNull private String variantSummary;
  @NonNull private String variantOrigin;
  @NonNull private String lastViewDate;
  @NonNull private String evidenceCivicUrl;
  @NonNull private String variantCivicUrl;
  @NonNull private String geneCivicUrl;

  public static class Builder implements VariantModelBuilder<CivicClinicalEvidenceSummary>{

    @Override
    public CivicClinicalEvidenceSummary build(String line) {
      List<String> items = TAB.splitToList(line);
      return new CivicClinicalEvidenceSummary(
          items.get(0),
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
          String.format("%s>%s", items.get(21), items.get(22)),
          items.get(23),
          items.get(24),
          items.get(25),
          items.get(26),
          items.get(27),
          items.get(28),
          items.get(29),
          items.get(30),
          items.get(31),
          items.get(32),
          items.get(33),
          items.get(34),
          items.get(35)
      );
    }
  }

}
