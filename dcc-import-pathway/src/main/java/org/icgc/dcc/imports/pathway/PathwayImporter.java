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
package org.icgc.dcc.imports.pathway;

import static com.google.common.base.Stopwatch.createStarted;
import static org.icgc.dcc.common.core.util.URLs.getUrl;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

import org.icgc.dcc.imports.core.SourceImporter;
import org.icgc.dcc.imports.core.model.ImportSource;
import org.icgc.dcc.imports.pathway.core.PathwayModel;
import org.icgc.dcc.imports.pathway.reader.PathwayModelReader;
import org.icgc.dcc.imports.pathway.writer.PathwayWriter;

import com.google.common.io.Resources;
import com.mongodb.MongoClientURI;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Create Pathway collection in mongodb, and subsequently embed pathway information into the gene-collection
 */
@Slf4j
@RequiredArgsConstructor
public class PathwayImporter implements SourceImporter {

  /**
   * Constants.
   */

  public static final boolean REMOTE = true;

  public static final URL REMOTE_REACTOME_UNIPROT_URL =
      getUrl("http://www.reactome.org/download/current/UniProt2Reactome_All_Levels.txt"); // DCC-4656
  public static final URL REMOTE_REACTOME_PATHWAY_SUMMATION_URL =
      getUrl("http://www.reactome.org/download/current/pathway2summation.txt");
  public static final URL REMOTE_REACTOME_PATHWAY_HIER_URL =
      getUrl("http://www.reactome.org/ReactomeRESTfulAPI/RESTfulWS/pathwayHierarchy/homo+sapiens");

  public static final URL LOCAL_REACTOME_UNIPROT_URL =
      Resources.getResource("uniprot_2_reactome.txt");
  public static final URL LOCAL_REACTOME_PATHWAY_SUMMATION_URL =
      Resources.getResource("pathway_2_summation.txt");
  public static final URL LOCAL_REACTOME_PATHWAY_HIER_URL =
      Resources.getResource("pathway_hierarchy.txt");

  public static final URL DEFAULT_REACTOME_UNIPROT_URL =
      REMOTE ? REMOTE_REACTOME_UNIPROT_URL : LOCAL_REACTOME_UNIPROT_URL;
  public static final URL DEFAULT_REACTOME_PATHWAY_SUMMATION_URL =
      REMOTE ? REMOTE_REACTOME_PATHWAY_SUMMATION_URL : LOCAL_REACTOME_PATHWAY_SUMMATION_URL;
  public static final URL DEFAULT_REACTOME_PATHWAY_HIER_URL =
      REMOTE ? REMOTE_REACTOME_PATHWAY_HIER_URL : LOCAL_REACTOME_PATHWAY_HIER_URL;

  /**
   * Configuration.
   */
  @NonNull
  private final URL uniprotFile;
  @NonNull
  private final URL hierarchyFile;
  @NonNull
  private final URL summationFile;
  @NonNull
  private final MongoClientURI mongoUri;

  public PathwayImporter(@NonNull MongoClientURI mongoUri) {
    this(
        DEFAULT_REACTOME_UNIPROT_URL,
        DEFAULT_REACTOME_PATHWAY_HIER_URL,
        DEFAULT_REACTOME_PATHWAY_SUMMATION_URL,
        mongoUri);

    log.info("*** Using [{}] input resources", REMOTE ? "REMOTE" : "LOCAL");
  }

  @Override
  public ImportSource getSource() {
    return ImportSource.PATHWAYS;
  }

  @Override
  @SneakyThrows
  public void execute() {
    val watch = createStarted();

    log.info("Reading pathway model...");
    val model = readPathwayModel(uniprotFile, summationFile, hierarchyFile);

    log.info("Writing pathway model to {}...", mongoUri);
    writePathwayModel(model);

    log.info("Finished importing pathways in {}", watch);
  }

  private PathwayModel readPathwayModel(URL uniprotFile, URL summationFile, URL hierarchyFile) throws IOException {
    return new PathwayModelReader().read(uniprotFile, summationFile, hierarchyFile);
  }

  private void writePathwayModel(PathwayModel model) throws UnknownHostException, IOException {
    @Cleanup
    val writer = new PathwayWriter(mongoUri);
    writer.writeValue(model);
  }

}
