/*
 * Copyright (c) 2014 The Ontario Institute for Cancer Research. All rights reserved.                             
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
package org.icgc.dcc.imports.client.core;

import static com.google.common.base.Stopwatch.createStarted;
import static com.google.common.base.Throwables.getStackTraceAsString;
import static com.google.common.collect.Maps.uniqueIndex;
import static org.icgc.dcc.imports.core.util.Importers.getRemoteCgsUri;
import static org.icgc.dcc.imports.core.util.Importers.getRemoteGenesBsonUri;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.icgc.dcc.common.client.api.ICGCClientConfig;
import org.icgc.dcc.common.core.mail.Mailer;
import org.icgc.dcc.imports.cgc.CgcImporter;
import org.icgc.dcc.imports.core.CollectionName;
import org.icgc.dcc.imports.core.SourceImporter;
import org.icgc.dcc.imports.diagram.DiagramImporter;
import org.icgc.dcc.imports.gene.GeneImporter;
import org.icgc.dcc.imports.go.GoImporter;
import org.icgc.dcc.imports.pathway.PathwayImporter;
import org.icgc.dcc.imports.project.ProjectImporter;

import com.google.common.collect.ImmutableList;
import com.mongodb.MongoClientURI;

import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Importer {

  /**
   * Processing order.
   */
  private static List<CollectionName> COLLECTION_ORDER = ImmutableList.of(
      CollectionName.PROJECTS,
      CollectionName.GENES,
      CollectionName.CGC,
      CollectionName.PATHWAYS,
      CollectionName.GO,
      CollectionName.DIAGRAMS);

  /**
   * Configuration
   */
  @NonNull
  private final MongoClientURI mongoUri;
  @NonNull
  private final String esUri = "es://localhost:9300";
  @NonNull
  private final ICGCClientConfig icgcConfig;
  @NonNull
  private final Map<CollectionName, SourceImporter> importers;

  public Importer(@NonNull String mongoUri, @NonNull ICGCClientConfig icgcConfig) {
    this.mongoUri = new MongoClientURI(mongoUri);
    this.icgcConfig = icgcConfig;
    this.importers = createImporters();
  }

  public void execute(@NonNull Collection<CollectionName> collectionNames) {
    val watch = createStarted();
    try {
      for (val collectionName : COLLECTION_ORDER) {
        if (collectionNames.contains(collectionName)) {
          val importer = importers.get(collectionName);

          val timer = createStarted();
          log.info("Importing '{}'...", collectionName);
          importer.execute();
          log.info("Finished importing '{}' in {}", collectionName, timer);
        }
      }
    } catch (Exception e) {
      log.error("Unknown error:", e);
      new Mailer().sendMail("DCC - Importer - FAILED", getStackTraceAsString(e));

      throw e;
    }

    val subject = "DCC - Importer - SUCCESS";
    val body = "Finished in " + watch + "\n\n";
    new Mailer().sendMail(subject, body);
  }

  private Map<CollectionName, SourceImporter> createImporters() {
    val importers = ImmutableList.<SourceImporter> of(
        new ProjectImporter(icgcConfig, mongoUri),
        new GeneImporter(mongoUri, getRemoteGenesBsonUri()),
        new CgcImporter(mongoUri, getRemoteCgsUri()),
        new PathwayImporter(mongoUri),
        new GoImporter(mongoUri),
        new DiagramImporter(mongoUri));

    return uniqueIndex(importers, (SourceImporter importer) -> importer.getCollectionName());
  }

}
