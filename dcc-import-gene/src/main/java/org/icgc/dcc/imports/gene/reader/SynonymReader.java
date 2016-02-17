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
package org.icgc.dcc.imports.gene.reader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.SneakyThrows;
import lombok.val;

/**
 * 
 */
public class SynonymReader {

  /**
   * Constants
   */
  private static final String URI =
      "ftp://ftp.ensembl.org/pub/grch37/release-83/mysql/homo_sapiens_core_83_37/external_synonym.txt.gz";
  private static final Pattern TSV = Pattern.compile("\t");
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Dependencies
   */
  private final Map<String, String> idMap;

  public SynonymReader(Map<String, String> idMap) {
    this.idMap = idMap;
  }

  @SneakyThrows
  public Map<String, ArrayNode> getSynonymMap() {
    val gzip = new GZIPInputStream(new URL(URI).openStream());
    val inputStreamReader = new InputStreamReader(gzip);
    val bufferedReader = new BufferedReader(inputStreamReader);

    val map = new HashMap<String, ArrayNode>();

    for (String s = bufferedReader.readLine(); null != s; s = bufferedReader.readLine()) {
      s = s.trim();
      if (s.length() > 0) {
        String[] line = TSV.split(s);
        val eId = idMap.get(line[0]);
        if (eId != null) {
          if (map.containsKey(eId)) {
            map.get(eId).add(line[1]);
          } else {
            val newList = MAPPER.createArrayNode();
            newList.add(line[1]);
            map.put(eId, newList);
          }
        }
      }
    }

    return map;
  }

}
