package org.icgc.dcc.imports.variant;

import com.mongodb.MongoClientURI;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icgc.dcc.imports.core.SourceImporter;
import org.icgc.dcc.imports.core.model.ImportSource;
import org.icgc.dcc.imports.core.util.Jongos;
import org.icgc.dcc.imports.variant.model.CivicClinicalEvidenceSummary;
import org.icgc.dcc.imports.variant.model.ClinvarVariantSummary;
import org.icgc.dcc.imports.variant.model.ClinvarVariationAllele;
import org.icgc.dcc.imports.variant.processor.api.ContentWriter;
import org.icgc.dcc.imports.variant.processor.api.Downloader;
import org.icgc.dcc.imports.variant.processor.api.FileReader;
import org.icgc.dcc.imports.variant.processor.api.UnCompressor;
import org.icgc.dcc.imports.variant.processor.impl.civic.CivicClinicalEvidenceSummaryFileReader;
import org.icgc.dcc.imports.variant.processor.impl.civic.CivicClinicalEvidenceSummaryProcessor;
import org.icgc.dcc.imports.variant.processor.impl.civic.CivicClinicalEvidenceSummaryWriter;
import org.icgc.dcc.imports.variant.processor.impl.clinvar.*;
import org.icgc.dcc.imports.variant.processor.impl.common.GzipFileUnCompressor;
import org.icgc.dcc.imports.variant.processor.impl.common.ShellCommandDownloader;
import org.jongo.Jongo;

import java.io.File;


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

@Slf4j
@RequiredArgsConstructor
public class VariantImporter implements SourceImporter {

  @NonNull
  private MongoClientURI mongoUri;
  private String civicCollectionName = "Civic";
  private String clinvarCollectionName = "Clinvar";

  @Override
  public ImportSource getSource() {
    return ImportSource.VARIANT;
  }

  @Override
  public void execute() {
    // VariantImporter is playing an injector role
    String systemDir = System.getProperty("java.io.tmpdir");
    String tmpPath = (systemDir.endsWith("/")?systemDir.substring(0, systemDir.length()-1):systemDir) + "/dcc/import/variant";

    File targetDir = new File(tmpPath);
    if(targetDir.exists()) targetDir.delete();

    Jongo jongo = Jongos.createJongo(mongoUri);

    String civicFilename = "nightly-ClinicalEvidenceSummaries.tsv";
    Downloader civicDownloader = new ShellCommandDownloader("https://civic.genome.wustl.edu/downloads/nightly/" + civicFilename, tmpPath, civicFilename);
    FileReader<CivicClinicalEvidenceSummary> civicReader = new CivicClinicalEvidenceSummaryFileReader();
    ContentWriter<CivicClinicalEvidenceSummary> civicWriter = new CivicClinicalEvidenceSummaryWriter(jongo, civicCollectionName);

    String clinvarSummaryFilename = "variant_summary.txt.gz";
    Downloader clinvarSummaryDownloader = new ShellCommandDownloader("ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/" + clinvarSummaryFilename, tmpPath, clinvarSummaryFilename);
    UnCompressor clinvarSummaryUnzipper = new GzipFileUnCompressor("variant_summary.txt");
    FileReader<ClinvarVariantSummary> clinvarSummaryReader = new ClinvarVariantSummaryFileReader(new ClinvarSummaryFilter());

    String clinvarAlleleFilename = "variation_allele.txt.gz";
    Downloader clinvarAlleleDownloader = new ShellCommandDownloader("ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/" + clinvarAlleleFilename, tmpPath, clinvarAlleleFilename);
    UnCompressor clinvarAlleleUnzipper = new GzipFileUnCompressor("variation_allele.txt");
    FileReader<ClinvarVariationAllele> clinvarAlleleReader = new ClinvarVariationAlleleFileReader();

    ClinvarVariantWriter clinvarWriter = new ClinvarVariantWriter(jongo, clinvarCollectionName);
    clinvarWriter.cleanCollection();

    new CivicClinicalEvidenceSummaryProcessor(civicDownloader, civicReader, civicWriter).process();

    new ClinvarVariantProcessor(clinvarSummaryDownloader, clinvarSummaryUnzipper, clinvarSummaryReader, clinvarAlleleDownloader, clinvarAlleleUnzipper, clinvarAlleleReader, clinvarWriter).process();
  }
}
