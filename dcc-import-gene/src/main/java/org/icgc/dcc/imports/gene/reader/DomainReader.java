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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.icgc.dcc.imports.gene.model.ProteinFeature;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

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

  public Multimap<String, ProteinFeature> read() {
    val retMap = ImmutableMultimap.<String, ProteinFeature> builder();
    readRecords().filter(this::hasInterproAnalysis).map(this::createProteinFeature).forEach(retMap::put);

    return retMap.build();
  }

  private Entry<String, ProteinFeature> createProteinFeature(List<String> record) {
    ProteinFeature pf = interproMap.get(getInterproId(record)).copy();
    pf.setStart(getStart(record));
    pf.setEnd(getEnd(record));
    pf.setAnalysisId(getPfAnalysisId(record));
    pf.setGffSource(getGffSource(record));

    String ens = transMap.get(getTranscriptId(record));
    return new SimpleEntry<String, ProteinFeature>(ens, pf);
  }

  private boolean hasInterproAnalysis(List<String> record) {
    return analysisMap.containsKey(getAnalysisId(record)) && interproMap.containsKey(getInterproId(record));
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