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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.zip.GZIPInputStream;

import org.icgc.dcc.imports.core.SourceImporter;
import org.icgc.dcc.imports.core.model.ImportSource;
import org.icgc.dcc.imports.gene.reader.ASNReader;
import org.icgc.dcc.imports.gene.reader.DomainReader;
import org.icgc.dcc.imports.gene.reader.IdReader;
import org.icgc.dcc.imports.gene.reader.SynonymReader;
import org.icgc.dcc.imports.gene.reader.TransReader;
import org.icgc.dcc.imports.gene.writer.GeneWriter;

import com.mongodb.MongoClientURI;

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
  @NonNull
  private final MongoClientURI mongoUri;

  public GeneImporter(@NonNull MongoClientURI mongoUri, @NonNull URI gtfUri) {
    this.gtfUri = gtfUri;
    this.mongoUri = mongoUri;
  }

  @Override
  public ImportSource getSource() {
    return ImportSource.GENES;
  }

  @SneakyThrows
  @Override
  public void execute() {
    val watch = createStarted();

    log.info("Doing Ensembl Data Joining...");
    val idReader = new IdReader();
    val idMap = idReader.getIdMap();

    val synReader = new SynonymReader(idMap);
    val synMap = synReader.getSynonymMap();

    val transMap = TransReader.joinTrans();
    val pfeatures = DomainReader.createProteinFeatures(transMap);
    log.info("Parsed {} transcripts for protein features.", pfeatures.size());

    log.info("Done");

    log.info("Starting ASN.1 Import from NCBI.");
    val asnReader = new ASNReader();
    val summaryMap = asnReader.callGene2Xml();
    log.info("Staged {} Summaries from NCBI.", summaryMap.size());

    log.info("Reading genes gzip stream from {}...", gtfUri);
    val geneReader = getReader();

    log.info("Writing genes to {}...", mongoUri);
    val writer = new GeneWriter(mongoUri, geneReader, summaryMap, synMap, pfeatures);
    writer.consumeGenes();
    writer.close();

    log.info("Finished writing genes in {}", watch);
  }

  @SneakyThrows
  private BufferedReader getReader() {
    val gzip = new GZIPInputStream(gtfUri.toURL().openStream());
    val inputStreamReader = new InputStreamReader(gzip);
    val bufferedReader = new BufferedReader(inputStreamReader);

    return bufferedReader;
  }

}
