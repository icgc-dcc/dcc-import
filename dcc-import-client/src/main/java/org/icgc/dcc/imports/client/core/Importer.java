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

import org.icgc.dcc.common.client.api.cgp.CGPClient;
import org.icgc.dcc.common.core.mail.Mailer;
import org.icgc.dcc.imports.cgc.CgcImporter;
import org.icgc.dcc.imports.core.SourceImporter;
import org.icgc.dcc.imports.core.model.ImportSource;
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
  private static List<ImportSource> SOURCE_ORDER = ImmutableList.of(
      ImportSource.PROJECTS,
      ImportSource.GENES,
      ImportSource.CGC,
      ImportSource.PATHWAYS,
      ImportSource.GO,
      ImportSource.DIAGRAMS);

  /**
   * Configuration
   */
  @NonNull
  private final MongoClientURI mongoUri;
  @NonNull
  private final Mailer mailer;
  @NonNull
  private final Map<ImportSource, SourceImporter> importers;

  public Importer(@NonNull MongoClientURI mongoUri, @NonNull Mailer mailer, @NonNull CGPClient cgpClient) {
    this.mongoUri = mongoUri;
    this.mailer = mailer;
    this.importers = createImporters(cgpClient);
  }

  public void execute() {
    execute(ImportSource.all());
  }

  public void execute(@NonNull Collection<ImportSource> sources) {
    val watch = createStarted();
    try {
      for (val source : SOURCE_ORDER) {
        if (sources.contains(source)) {
          val importer = importers.get(source);

          val timer = createStarted();
          log.info("Importing '{}'...", source);
          importer.execute();
          log.info("Finished importing '{}' in {}", source, timer);
        }
      }
    } catch (Exception e) {
      log.error("Unknown error:", e);
      mailer.sendMail("DCC Importer - FAILED", getStackTraceAsString(e));

      throw e;
    }

    val subject = "DCC Importer - SUCCESS";
    val body = "Finished in " + watch + "\n\n";
    mailer.sendMail(subject, body);
  }

  private Map<ImportSource, SourceImporter> createImporters(CGPClient cgpClient) {
    val importers = ImmutableList.<SourceImporter> of(
        new ProjectImporter(mongoUri, cgpClient),
        new GeneImporter(mongoUri, getRemoteGenesBsonUri()),
        new CgcImporter(mongoUri, getRemoteCgsUri()),
        new PathwayImporter(mongoUri),
        new GoImporter(mongoUri),
        new DiagramImporter(mongoUri));

    return uniqueIndex(importers, (SourceImporter importer) -> importer.getSource());
  }

}
