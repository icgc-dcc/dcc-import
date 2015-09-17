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

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.mongodb.MongoClientURI;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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

  public void execute(ImportSource... sources) {
    execute(ImmutableList.copyOf(sources));
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

      val message = new ReportMessage(watch, ImmutableList.<Exception> of(e));
      mailer.sendMail(message.getSubject(), message.getBody());

      throw e;
    }

    val message = new ReportMessage(watch, ImmutableList.<Exception> of());
    mailer.sendMail(message.getSubject(), message.getBody());
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

  /**
   * Report email send to recipients.
   */
  @RequiredArgsConstructor
  static class ReportMessage {

    private final Stopwatch watch;
    private final List<Exception> exceptions;

    public String getSubject() {
      return "DCC Importer - " + getStatus();
    }

    public String getBody() {
      val body = new StringBuilder();
      body.append("<html>");
      body.append("<body>");
      body.append("<h1 style='color: " + getColor() + "; border: 3px solid " + getColor()
          + "; border-left: none; border-right: none; padding: 5px 0;'>");
      body.append(getStatus());
      body.append("</h1>");
      body.append("Finished in ").append("<b>").append(watch).append("</b>");
      body.append("<br>");

      if (!isSuccess()) {
        body.append("<h2>Exceptions</h2>");
        body.append("<ol>");
        for (val exception : exceptions) {
          body.append("<h3>Message</h3>");
          body.append("<pre>");
          body.append(exception.getMessage());
          body.append("</pre>");
          body.append("<h3>Stack Trace</h3>");
          body.append("<pre>");
          body.append(Throwables.getStackTraceAsString(exception));
          body.append("</pre>");
          body.append(
              "<div style='border-top: 1px dotted " + getColor() + "; margin-top 4px; margin-bottom: 5px;'></div>");
        }
        body.append("</ol>");
      }

      body.append("</body>");
      body.append("</html>");

      return body.toString();
    }

    private String getColor() {
      return isSuccess() ? "#1a9900" : "red";
    }

    private String getStatus() {
      return isSuccess() ? "SUCCESS" : "ERROR";
    }

    private boolean isSuccess() {
      return exceptions.isEmpty();
    }

  }

}
