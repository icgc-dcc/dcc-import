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

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;
import static org.icgc.dcc.common.core.util.Formats.formatCount;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.icgc.dcc.imports.core.util.AbstractTsvMapReader;
import org.icgc.dcc.imports.pathway.model.PathwaySummation;

import com.google.common.collect.ImmutableList;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PathwaySummationReader extends AbstractTsvMapReader {

  /**
   * Constants.
   */
  private static final String REACTOME_ID = "reactome_id";
  private static final String REACTOME_NAME = "reactome_name";
  private static final String SUMMATION = "summation";
  private static final String[] FIELD_NAMES = { REACTOME_ID, REACTOME_NAME, SUMMATION
  };

  @SneakyThrows
  public Iterable<PathwaySummation> read(URL summationFile) {
    log.info("Reading summations from {}...", summationFile);

    val summations = ImmutableList.<PathwaySummation> builder();
    int counter = 0;
    val records = readRecords(summationFile);
    for (val record : records) {
      val summation = PathwaySummation.builder()
          .reactomeId(getReactomeId(record))
          .reactomeName(getReactomeName(record))
          .summation(getSummation(record))
          .build();

      summations.add(summation);
      counter++;
    }

    log.info("Finished reading {} summations", formatCount(counter));

    return summations.build();
  }

  private static String getReactomeId(Map<String, String> record) {
    return record.get(REACTOME_ID);
  }

  private static String getSummation(Map<String, String> record) {
    return record.get(SUMMATION);
  }

  private static String getReactomeName(Map<String, String> record) {
    return unescapeHtml4(record.get(REACTOME_NAME));
  }

  private Iterable<Map<String, String>> readRecords(URL summationFile) throws IOException, MalformedURLException {
    return readRecords(FIELD_NAMES, summationFile.openStream());
  }

}
