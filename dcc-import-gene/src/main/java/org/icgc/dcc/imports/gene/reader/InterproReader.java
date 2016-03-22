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

import java.util.List;
import java.util.Map;

import org.icgc.dcc.imports.gene.model.ProteinFeature;
import org.icgc.dcc.imports.gene.model.XrefMapping;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import lombok.val;

public class InterproReader extends TsvReader {

  /**
   * Dependencies
   */
  private final XrefMapping xrefMapping;

  public InterproReader(String uri, @NonNull XrefMapping xrefMapping) {
    super(uri);
    this.xrefMapping = xrefMapping;
  }

  /**
   * Returns a map of protein features
   */
  public Map<String, ProteinFeature> read() {

    val interproMap = ImmutableMap.<String, ProteinFeature> builder();
    readRecords().forEach(record -> {
      ProteinFeature pf = new ProteinFeature(getInterproId(record), getHitName(record), getDescription(record));
      interproMap.put(getHitName(record), pf);
    });

    return interproMap.build();
  }

  private String getInterproId(List<String> record) {
    return record.get(0);
  }

  private String getHitName(List<String> record) {
    return record.get(1);
  }

  private String getDescription(List<String> record) {
    return xrefMapping.getInterproMap().get(getInterproId(record));
  }

}