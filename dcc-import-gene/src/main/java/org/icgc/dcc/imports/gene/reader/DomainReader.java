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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.icgc.dcc.imports.gene.model.ProteinFeature;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import lombok.val;

/**
 * Constructs the protein domains
 */
public final class DomainReader extends TsvReader {

  /**
   * Dependencies
   */
  private final Map<String, String> transMap;
  private final Map<String, ProteinFeature> interproMap;
  private final Map<String, String> analysisMap;

  public DomainReader(String uri,
      @NonNull Map<String, String> transMap,
      @NonNull Map<String, ProteinFeature> interproMap,
      @NonNull Map<String, String> analysisMap) {
    super(uri);
    this.transMap = transMap;
    this.interproMap = interproMap;
    this.analysisMap = analysisMap;
  }

  public Map<String, List<ProteinFeature>> read() {
    val retMap = new HashMap<String, List<ProteinFeature>>();

    readRecords().forEach(record -> {
      if (analysisMap.containsKey(getAnalysisId(record))) {
        if (interproMap.containsKey(getInterproId(record))) {
          ProteinFeature pf = interproMap.get(getInterproId(record)).copy();
          pf.setStart(getStart(record));
          pf.setEnd(getEnd(record));
          pf.setAnalysisId(getPfAnalysisId(record));
          pf.setGffSource(getGffSource(record));
          String ens = transMap.get(getTranscriptId(record));

          if (retMap.containsKey(ens)) {
            retMap.get(ens).add(pf);
          } else {
            List<ProteinFeature> al = new ArrayList<>();
            al.add(pf);
            retMap.put(ens, al);
          }
        }
      }
    });

    return ImmutableMap.<String, List<ProteinFeature>> copyOf(retMap);
  }

  private String getAnalysisId(List<String> record) {
    return record.get(7);
  }

  private String getInterproId(List<String> record) {
    return record.get(6);
  }

  private int getStart(List<String> record) {
    return Integer.parseInt(record.get(2));
  }

  private int getEnd(List<String> record) {
    return Integer.parseInt(record.get(3));
  }

  private String getPfAnalysisId(List<String> record) {
    return record.get(8);
  }

  private String getGffSource(List<String> record) {
    return analysisMap.get(getAnalysisId(record));
  }

  private String getTranscriptId(List<String> record) {
    return record.get(1);
  }

}