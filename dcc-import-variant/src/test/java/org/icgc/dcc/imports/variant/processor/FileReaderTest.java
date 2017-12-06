package org.icgc.dcc.imports.variant.processor;

import io.reactivex.Observable;
import org.icgc.dcc.imports.variant.model.CivicClinicalEvidenceSummary;
import org.icgc.dcc.imports.variant.model.ClinvarVariantSummary;
import org.icgc.dcc.imports.variant.model.ClinvarVariationAllele;
import org.icgc.dcc.imports.variant.processor.impl.civic.CivicClinicalEvidenceSummaryFileReader;
import org.icgc.dcc.imports.variant.processor.impl.clinvar.ClinvarSummaryFilter;
import org.icgc.dcc.imports.variant.processor.impl.clinvar.ClinvarVariantSummaryFileReader;
import org.icgc.dcc.imports.variant.processor.impl.clinvar.ClinvarVariationAlleleFileReader;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

public class FileReaderTest extends FileOperation{

  @Test
  public void civicFileReaderTest() {
    CivicClinicalEvidenceSummaryFileReader reader = new CivicClinicalEvidenceSummaryFileReader();
    Observable<CivicClinicalEvidenceSummary> civic_data =  reader.extract(Observable.just(files[0]));
    Stream<CivicClinicalEvidenceSummary> stream = StreamSupport.stream(civic_data.blockingIterable().spliterator(), false);
    Assert.assertEquals(
    stream.filter(summary -> summary.getGene().equals("JAK2") && summary.getOdID().equals("10747")).findFirst().get().getGeneCivicUrl(), "https://civic.genome.wustl.edu/links/genes/28");
  }

  @Test
  public void clinvarSummaryFileReaderTest() {
    ClinvarVariantSummaryFileReader reader = new ClinvarVariantSummaryFileReader(new ClinvarSummaryFilter());
    Observable<ClinvarVariantSummary> summaries = reader.extract(Observable.just(files[2]));
    Stream<ClinvarVariantSummary> stream = StreamSupport.stream(summaries.blockingIterable().spliterator(), false);
    Assert.assertEquals(
        stream.filter(summary -> summary.getAlleleID() == 15041 && summary.getAssembly().equals("GRCh37")).findFirst().get().getChromosomeAccession(),
        "NC_000007.13"
    );
  }

  @Test
  public void clinvarAlleleFileReaderTest() {
    ClinvarVariationAlleleFileReader reader = new ClinvarVariationAlleleFileReader();
    Observable<ClinvarVariationAllele> alleles = reader.extract(Observable.just(files[1]));
    Stream<ClinvarVariationAllele> stream = StreamSupport.stream(alleles.blockingIterable().spliterator(), false);
    Assert.assertEquals(
        stream.filter(allele -> allele.getAlleleID() == 15041).findFirst().get().getVariationID(),
        2
    );
  }
}
