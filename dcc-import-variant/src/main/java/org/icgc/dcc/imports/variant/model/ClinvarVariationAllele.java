package org.icgc.dcc.imports.variant.model;

import lombok.*;

import java.io.Serializable;
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
public class ClinvarVariationAllele implements Serializable {
  @NonNull
  private int variationID;
  @NonNull
  private String variationType;
  @NonNull
  private int alleleID;
  @NonNull
  private boolean interpreted;

  public static class Builder implements VariantModelBuilder<ClinvarVariationAllele> {

    @Override
    public ClinvarVariationAllele build(String line) {

      List<String> items = TAB.splitToList(line);
      return new ClinvarVariationAllele(
          Integer.parseInt(items.get(0)),
          items.get(1),
          Integer.parseInt(items.get(2)),
          Boolean.parseBoolean(items.get(3))
      );
    }
  }
}
