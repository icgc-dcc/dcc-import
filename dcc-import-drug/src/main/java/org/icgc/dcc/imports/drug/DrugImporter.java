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
package org.icgc.dcc.imports.drug;

import static com.google.common.base.Stopwatch.createStarted;
import static org.icgc.dcc.common.core.util.Formats.formatCount;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.icgc.dcc.imports.core.SourceImporter;
import org.icgc.dcc.imports.core.model.ImportSource;
import org.icgc.dcc.imports.drug.core.DrugProcessor;
import org.icgc.dcc.imports.drug.reader.DrugReader;
import org.icgc.dcc.imports.drug.reader.GeneReader;
import org.icgc.dcc.imports.drug.reader.TrialsReader;
import org.icgc.dcc.imports.drug.writer.DrugWriter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClientURI;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DrugImporter implements SourceImporter {

  /**
   * Dependencies
   */
  @NonNull
  private final MongoClientURI mongoUri;

  public DrugImporter(@NonNull MongoClientURI mongoUri) {
    this.mongoUri = mongoUri;
  }

  @Override
  public ImportSource getSource() {
    return ImportSource.DRUGS;
  }

  @Override
  @SneakyThrows
  public void execute() {
    val watch = createStarted();
    log.info("Reading drugs...");
    val drugs = readDrugs();
    log.info("Finished reading {} drugs", formatCount(drugs));

    log.info("Processing drugs...");
    val processed = process(drugs);
    log.info("Finished processing {} drugs", formatCount(processed));
    
    log.info("Writing drugs...");
    writeDrugs(processed);
    log.info("Finished writing {} drugs", formatCount(processed));
    
    log.info("Imported {} drugs in {}.",formatCount(processed), watch);
  }

  private List<ObjectNode> readDrugs() throws IOException {
    return new DrugReader().readDrugs();
  }

  private Map<String, ObjectNode> readTrialMap() {
    return new TrialsReader().getTrialsMap();
  }

  private Map<String, ObjectNode> readGeneMap() {
    return new GeneReader(mongoUri).readGeneMap();
  }

  private List<ObjectNode> process( List<ObjectNode> drugs) {
    val processor = new DrugProcessor(readGeneMap(), readTrialMap());
    return processor.process(drugs);
  }

  @SneakyThrows
  private void writeDrugs(List<ObjectNode> drugs) {
    @Cleanup
    val drugWriter = new DrugWriter(mongoUri);
    drugWriter.writeValue(drugs);
  }

}
