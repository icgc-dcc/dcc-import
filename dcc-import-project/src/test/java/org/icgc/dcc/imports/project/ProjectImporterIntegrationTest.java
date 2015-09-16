/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
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

import lombok.val;

import org.icgc.dcc.common.client.api.ICGCClient;
import org.icgc.dcc.common.client.api.ICGCClientConfig;
import org.icgc.dcc.common.client.api.cgp.CGPClient;
import org.junit.Ignore;
import org.junit.Test;

import com.mongodb.MongoClientURI;

public class ProjectImporterIntegrationTest {

  private static final String SERVICE_URL = "https://***REMOVED***/ud_oauth/1/search";
  private static final String CONSUMER_KEY = "***REMOVED***";
  private static final String CONSUMER_SECRET = "***REMOVED***";
  private static final String ACCESS_TOKEN = "***REMOVED***";
  private static final String ACCESS_SECRET = "***REMOVED***";

  ProjectImporter projectImporter = new ProjectImporter(getMongoUri(), createCgpClient());

  @Test
  @Ignore("Run manually this integration test")
  public void executeTest() {
    projectImporter.execute();
  }

  private static MongoClientURI getMongoUri() {
    return new MongoClientURI("mongodb://localhost/project-importer-test");
  }

  private static CGPClient createCgpClient() {
    val config = ICGCClientConfig.builder()
        .cgpServiceUrl(SERVICE_URL)
        .consumerKey(CONSUMER_KEY)
        .consumerSecret(CONSUMER_SECRET)
        .accessToken(ACCESS_TOKEN)
        .accessSecret(ACCESS_SECRET)
        .requestLoggingEnabled(true)
        .strictSSLCertificates(false)
        .build();

    return ICGCClient.create(config).cgp();
  }

}
