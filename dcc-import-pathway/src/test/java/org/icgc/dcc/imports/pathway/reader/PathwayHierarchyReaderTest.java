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
package org.icgc.dcc.imports.pathway.reader;

import static org.apache.commons.lang.StringUtils.repeat;
import static org.icgc.dcc.imports.core.util.Importers.getRemoteReactomeHierarchyUri;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.icgc.dcc.imports.pathway.reader.PathwayHierarchyReader;
import org.junit.Test;
import org.xml.sax.SAXException;

@Slf4j
public class PathwayHierarchyReaderTest {

  @Test
  public void testRead() throws MalformedURLException, IOException, SAXException, ParserConfigurationException,
      URISyntaxException {
    val hierarchyFile = getRemoteReactomeHierarchyUri();
    log.info("Pathway URL {}", hierarchyFile);

    val parser = new PathwayHierarchyReader();
    val results = parser.read(hierarchyFile);

    for (val dbId : results.keySet()) {
      log.info("{}", repeat("-", 80));
      log.info("Pathway dbId: '{}'", dbId);
      log.info("{}", repeat("-", 80));
      val set = results.get(dbId);

      int index = 1;
      for (val list : set) {
        log.info("{}.", index);
        for (int i = 0; i < list.size(); i++) {
          val segment = list.get(i);
          log.info("  {}{} - {}", repeat("  ", i), segment.getReactomeName(), segment.isDiagrammed());
        }

        index++;
      }
    }
  }

}
