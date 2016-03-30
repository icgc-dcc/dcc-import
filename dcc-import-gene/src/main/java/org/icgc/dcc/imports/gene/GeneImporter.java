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
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.icgc.dcc.common.core.util.Formats.formatCount;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;
import java.util.stream.Stream;

import org.icgc.dcc.imports.core.SourceImporter;
import org.icgc.dcc.imports.core.model.ImportSource;
import org.icgc.dcc.imports.gene.core.GeneIterator;
import org.icgc.dcc.imports.gene.joiner.EnsemblJoiner;
import org.icgc.dcc.imports.gene.joiner.EntrezJoiner;
import org.icgc.dcc.imports.gene.processor.TranscriptProcessor;
import org.icgc.dcc.imports.gene.reader.EntrezReader;
import org.icgc.dcc.imports.gene.reader.EnsemblReader;
import org.icgc.dcc.imports.gene.reader.GeneGtfReader;
import org.icgc.dcc.imports.gene.writer.GeneWriter;

import com.fasterxml.jackson.databind.node.ObjectNode;
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

  /**
   * Configuration.
   */
  @NonNull
  private final URI gtfUri;
  @NonNull
  private final MongoClientURI mongoUri;

  @Override
  public ImportSource getSource() {
    return ImportSource.GENES;
  }

  /**
   * Main pipeline execution for creating gene model. Calls all readers to pre-compute hashmaps of relevant information
   * before streaming GTF file for construction of gene model skeleton.
   */
  @Override
  @SneakyThrows
  public void execute() {
    log.info("Importing genes...");
    val watch = createStarted();

    // Extract
    val genes = readGenes();

    // Transform
    val transformed = transformGenes(genes);

    // Load
    writeGenes(transformed);

    log.info("Finished importing genes in {}", watch);
  }

  private Stream<ObjectNode> readGenes() {
    val gtfReader = new GeneGtfReader(gtfUri.toString());
    val gtfStream = gtfReader.read();
    val iterator = new GeneIterator(gtfStream.iterator());

    return stream(spliteratorUnknownSize(iterator, NONNULL | DISTINCT), false);
  }

  private Stream<ObjectNode> transformGenes(Stream<ObjectNode> genes) {
    return genes
        .map(joinEnsemble())
        .map(joinEntrez())
        .map(TranscriptProcessor::process);
  }

  private void writeGenes(Stream<ObjectNode> genes) throws IOException {
    log.info("Writing genes to {}...", mongoUri);
    @Cleanup
    val writer = new GeneWriter(mongoUri);
    genes.forEach(writer::writeValue);
    log.info("Finished writing genes to {}", mongoUri);
  }

  private static Function<? super ObjectNode, ? extends ObjectNode> joinEnsemble() {
    log.info("Reading Ensembl...");
    val ensemblReader = new EnsemblReader();
    val ensembl = ensemblReader.read();
    log.info("Finished reading Ensembl");

    return new EnsemblJoiner(ensembl)::join;
  }

  private static Function<? super ObjectNode, ? extends ObjectNode> joinEntrez() {
    log.info("Reading NCBI summaries...");
    val asnReader = new EntrezReader();
    val summaryMap = asnReader.readSummary();
    log.info("Finished reading {} NCBI summaries", formatCount(summaryMap.size()));

    return new EntrezJoiner(summaryMap)::join;
  }

}
