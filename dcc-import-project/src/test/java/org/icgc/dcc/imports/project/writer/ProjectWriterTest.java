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
package org.icgc.dcc.imports.project.writer;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import static org.icgc.dcc.imports.core.util.Importers.getLocalMongoClientUri;

import org.icgc.dcc.common.core.model.ReleaseCollection;
import org.icgc.dcc.common.test.mongodb.EmbeddedMongo;
import org.icgc.dcc.imports.project.model.Project;
import org.icgc.dcc.imports.project.writer.ProjectWriter;
import org.jongo.Jongo;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.mongodb.MongoClientURI;

@Slf4j
@Ignore
public class ProjectWriterTest {

  /**
   * Constants.
   */
  private static final String MONGO_DB_NAME = "project-importer-test";
  private static final String MONGO_RELEASE_PROJECT_DB_NAME = MONGO_DB_NAME + "."
      + ReleaseCollection.PROJECT_COLLECTION.getId();

  @Rule
  public final static EmbeddedMongo embeddedMongo = new EmbeddedMongo();

  @Test
    @SneakyThrows
    public void testWriteFiles() {
      val mongoClientUri = getLocalMongoClientUri(embeddedMongo.getPort(), MONGO_RELEASE_PROJECT_DB_NAME);
      @Cleanup
      val writer = new ProjectWriter(mongoClientUri);
  
      writer.writeFiles(ImmutableList.of(Project.builder().__project_id("test").build()));
  
      log.info("Project: {}", getJongo(mongoClientUri).getCollection(ReleaseCollection.PROJECT_COLLECTION.getId())
          .findOne()
          .as(JsonNode.class));
    }

  private static Jongo getJongo(MongoClientURI mongoClientUri) {
    val mongo = embeddedMongo.getMongo();
    val db = mongo.getDB(mongoClientUri.getDatabase());

    return new Jongo(db);
  }

}
