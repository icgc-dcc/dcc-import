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
package org.icgc.dcc.imports.cgc.reader;

import org.icgc.dcc.imports.cgc.util.CosmicClient;
import org.junit.Ignore;
import org.junit.Test;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Ignore("For development only")
public class CosmicReaderTest {

  @Test
  public void testReadMultiple() {
    for (int i = 0; i < 10; i++) {
      testRead();
    }
  }

  @Test
  public void testRead() {
    val client = createClient();
    client.login();

    val reader = new CensusReader(client);
    for (val record : reader.read()) {
      log.info("Record:\n{}", record);
    }
  }

  private static CosmicClient createClient() {
    val userName = System.getProperty("cosmic.username");
    val password = System.getProperty("cosmic.password");

    return new CosmicClient(userName, password);
  }

}
