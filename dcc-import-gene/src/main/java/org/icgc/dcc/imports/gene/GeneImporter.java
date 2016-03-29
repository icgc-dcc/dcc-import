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
package org.icgc.dcc.imports.gene;

import static com.google.common.base.Stopwatch.createStarted;
import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.NONNULL;

import java.net.URI;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.icgc.dcc.imports.core.SourceImporter;
import org.icgc.dcc.imports.core.model.ImportSource;
import org.icgc.dcc.imports.gene.core.GeneIterator;
import org.icgc.dcc.imports.gene.joiner.EnsemblJoiner;
import org.icgc.dcc.imports.gene.joiner.EntrezJoiner;
import org.icgc.dcc.imports.gene.processor.TranscriptProcessor;
import org.icgc.dcc.imports.gene.reader.ASNReader;
import org.icgc.dcc.imports.gene.reader.EnsemblReader;
import org.icgc.dcc.imports.gene.reader.GeneGtfReader;
import org.icgc.dcc.imports.gene.writer.GeneWriter;

import com.mongodb.MongoClientURI;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GeneImporter implements SourceImporter {

  @NonNull
  private final URI gtfUri;
  private final MongoClientURI mongoUri;

  @Override
  public ImportSource getSource() {
    return ImportSource.GENES;
  }

  /**
   * Main pipeline execution for creating gene model. Calls all readers to pre-compute hashmaps of relevant information
   * before streaming GTF file for construction of gene model skeleton.
   */
  @SneakyThrows
  @Override
  public void execute() {
    val watch = createStarted();

    log.info("Doing Ensembl Data Joining...");
    val ensemblReader = new EnsemblReader();
    val ensembleJoiner = new EnsemblJoiner(ensemblReader.read());
    log.info("... Done Ensemble Data Joining!");

    log.info("Starting ASN.1 Import from NCBI.");
    val asnReader = new ASNReader();
    val summaryMap = asnReader.readSummary();
    val entrezJoiner = new EntrezJoiner(summaryMap);
    log.info("Staged {} Summaries from NCBI.", summaryMap.size());

    log.info("Writing genes to {}...", mongoUri);
    val gtfReader = new GeneGtfReader(gtfUri.toString());

    val gtfStream = gtfReader.read();
    val genes = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(new GeneIterator(gtfStream.iterator()), NONNULL | DISTINCT), false);

    @Cleanup
    val writer = new GeneWriter(mongoUri);
    genes
        .map(ensembleJoiner::join)
        .map(entrezJoiner::join)
        .map(TranscriptProcessor::process)
        .forEach(writer::writeValue);

    log.info("Finished writing genes in {}", watch);
  }

}
