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

import static com.google.common.collect.Maps.immutableEntry;
import static java.lang.Integer.parseInt;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.icgc.dcc.common.core.util.stream.Collectors;

public class ExonReader extends TsvReader {

  public ExonReader(String uri) {
    super(uri);
  }

  public Map<String, Entry<Integer, Integer>> read() {
    return readRecords().collect(Collectors.toImmutableMap(r -> getStableId(r), r -> getPhaseTuple(r)));
  }

  private String getStableId(List<String> record) {
    return record.get(9);
  }

  /**
   * Returns phase tuple in order of (start phase, end phase)
   */
  private Entry<Integer, Integer> getPhaseTuple(List<String> record) {
    return immutableEntry(parseInt(record.get(5)), parseInt(record.get(6)));
  }

}
