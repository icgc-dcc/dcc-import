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

import static org.icgc.dcc.common.core.json.Jackson.DEFAULT;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Multimap;

import lombok.NonNull;
import lombok.val;

/**
 * Class responsible for reading and mapping Gene Synonyms
 */
public class SynonymReader extends TsvReader {

  /**
   * Dependencies
   */
  private final Multimap<String, String> idMap;

  public SynonymReader(String uri, @NonNull Multimap<String, String> idMap) {
    super(uri);
    this.idMap = idMap;
  }

  public Map<String, ArrayNode> read() {
    val synMap = new HashMap<String, ArrayNode>();

    readRecords().forEach(record -> {
      getExternalIds(record)
          .forEach(id -> synMap.computeIfAbsent(id, x -> DEFAULT.createArrayNode()).add(getSynonym(record)));
    });

    return synMap;
  }

  private Collection<String> getExternalIds(List<String> record) {
    return idMap.get(record.get(0));
  }

  private String getSynonym(List<String> record) {
    return record.get(1);
  }

}
