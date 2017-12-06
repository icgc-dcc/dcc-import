package org.icgc.dcc.imports.variant.processor.impl.clinvar;

import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.imports.variant.model.ClinvarVariant;
import org.icgc.dcc.imports.variant.model.ClinvarVariantSummary;
import org.icgc.dcc.imports.variant.model.ClinvarVariationAllele;
import org.icgc.dcc.imports.variant.processor.api.Downloader;
import org.icgc.dcc.imports.variant.processor.api.FileReader;
import org.icgc.dcc.imports.variant.processor.api.UnCompressor;
import org.icgc.dcc.imports.variant.processor.api.VariantDataProcessor;

import java.util.ArrayList;
import java.util.List;

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
@RequiredArgsConstructor
public class ClinvarVariantProcessor implements VariantDataProcessor {

  @NonNull
  private Downloader summaryDownloader;
  @NonNull private UnCompressor summaryUnzipper;
  @NonNull private FileReader<ClinvarVariantSummary> summaryReader;
  @NonNull private Downloader alleleDownloader;
  @NonNull private UnCompressor alleleUnzipper;
  @NonNull private FileReader<ClinvarVariationAllele> alleleReader;
  @NonNull private ClinvarVariantWriter writer;

  @Override
  public void process() {

      Single.zip(
        summaryDownloader.download().compose(summaryUnzipper::unzip).compose(summaryReader::extract).toMultimap(summary -> summary.getAlleleID()),
        alleleDownloader.download().compose(alleleUnzipper::unzip).compose(alleleReader::extract).toList(),
        (summaries, alleles) -> {

          return
            alleles.stream().<List<ClinvarVariant>>reduce(
                new ArrayList<ClinvarVariant>(),
                (list, allele) -> {
                  Iterable<ClinvarVariantSummary> iter = summaries.get(allele.getAlleleID());
                  if(iter != null) {
                    for(ClinvarVariantSummary summary: iter){
                      list.add(ClinvarVariant.Builder.build(summary, allele));
                    }
                  }
                  return list;
                },
                (list1, list2) -> {
                  List<ClinvarVariant> newList = new ArrayList<>();
                  newList.addAll(list1);
                  newList.addAll(list2);
                  return newList;
                }
            );
        }
      ).toObservable().flatMap(list -> Observable.<ClinvarVariant>fromIterable(list)).compose(writer::write).subscribe();

  }
}
