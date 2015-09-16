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
package org.icgc.dcc.imports.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.model.ReleaseCollection.PROJECT_COLLECTION;
import static org.icgc.dcc.imports.core.util.Importers.getLocalMongoClientUri;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.icgc.dcc.common.client.api.cgp.CGPClient;
import org.icgc.dcc.common.client.api.cgp.CancerGenomeProject;
import org.icgc.dcc.common.client.api.cgp.DataLevelProject;
import org.icgc.dcc.common.test.mongodb.EmbeddedMongo;
import org.icgc.dcc.imports.project.util.ProjectConverter;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mongodb.MongoClientURI;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ProjectImporterTest {

  @Mock
  CGPClient cgpClient;

  /**
   * Constants.
   */
  private static final String MONGO_DB_NAME = "project-importer-test";
  private static final String MONGO_RELEASE_PROJECT_DB_NAME = MONGO_DB_NAME + "." + PROJECT_COLLECTION.getId();

  @Rule
  public final static EmbeddedMongo embeddedMongo = new EmbeddedMongo();

  @Test
  public void testImport() {
    // Test data
    val projectName = "RTBL-FR";
    val nid = "1";
    val details = ImmutableMap.<String, String> of(ProjectConverter._PROJECT_ID_KEY, projectName);
    val dlp = DataLevelProject.builder().details(details).build();
    val dlps = ImmutableList.of(dlp);
    val cgp = CancerGenomeProject.builder().name(projectName).nid(nid).dlps(dlps).details(details).build();
    val cgps = ImmutableList.of(cgp);

    when(cgpClient.getCancerGenomeProjects()).thenReturn(cgps);
    when(cgpClient.getCancerGenomeProject(nid)).thenReturn(cgp);

    val includedProjects = ImmutableSet.<String> of(projectName);

    val mongoClientUri = getLocalMongoClientUri(embeddedMongo.getPort(), MONGO_DB_NAME);

    val importer = new ProjectImporter(mongoClientUri, cgpClient);
    importer.execute();

    assertImport(includedProjects);
  }

  private static void assertImport(Set<String> includedProjects) {
    val projectsCollection =
        getProjectsCollection(getLocalMongoClientUri(embeddedMongo.getPort(), MONGO_RELEASE_PROJECT_DB_NAME));
    val projectsQuantity = projectsCollection.count();

    assertThat(projectsQuantity).isGreaterThan(0);
    log.info("Project: {}", projectsCollection.findOne().as(JsonNode.class));
  }

  private static MongoCollection getProjectsCollection(MongoClientURI mongoClientUri) {
    val jongo = getJongo(mongoClientUri);

    return jongo.getCollection(mongoClientUri.getCollection());
  }

  private static Jongo getJongo(MongoClientURI mongoClientUri) {
    val mongo = embeddedMongo.getMongo();
    val db = mongo.getDB(mongoClientUri.getDatabase());

    return new Jongo(db);
  }

}
