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
package org.icgc.dcc.imports.client.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.model.ReleaseCollection.GENE_COLLECTION;
import static org.icgc.dcc.common.core.model.ReleaseCollection.GENE_SET_COLLECTION;
import static org.icgc.dcc.common.core.model.ReleaseCollection.PROJECT_COLLECTION;
import static org.icgc.dcc.imports.core.util.Importers.getLocalMongoClientUri;
import static org.icgc.dcc.imports.core.util.Jongos.createJongo;

import org.icgc.dcc.common.client.api.cgp.CGPClient;
import org.icgc.dcc.common.core.mail.Mailer;
import org.icgc.dcc.imports.client.ClientMain;
import org.icgc.dcc.imports.core.model.ImportSource;
import org.jongo.Jongo;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.MongoClientURI;

import lombok.val;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ClientMain.class, inheritLocations = true)
@Ignore("This is tested in ETLIntegration. This is useful for development though")
public class ImporterTest {

  @Autowired
  CGPClient cgpClient;

  /**
   * Test environment.
   */
  private final MongoClientURI mongoUri = getLocalMongoClientUri("dcc-import");
  private final Jongo jongo = createJongo(mongoUri);

  @Test
  public void testExecute() {
    val importer = createImporter();
    importer.execute(ImportSource.PROJECTS);

    assertThat(getCollectionSize(PROJECT_COLLECTION.getId())).isGreaterThan(0);
    assertThat(getCollectionSize(GENE_COLLECTION.getId())).isGreaterThan(0);
    assertThat(getCollectionSize(GENE_SET_COLLECTION.getId())).isGreaterThan(0);
  }

  private Importer createImporter() {
    val userName = System.getProperty("cosmic.username");
    val password = System.getProperty("cosmic.password");

    return new Importer(mongoUri, createMailer(), cgpClient, userName, password);
  }

  private Mailer createMailer() {
    return Mailer.builder().enabled(false).build();
  }

  private long getCollectionSize(String collectionName) {
    return jongo.getCollection(collectionName).count();
  }

}
