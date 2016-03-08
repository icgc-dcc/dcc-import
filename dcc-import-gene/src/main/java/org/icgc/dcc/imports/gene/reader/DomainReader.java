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

import static org.icgc.dcc.imports.gene.core.Sources.PROTEIN_FEATURE_URI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.icgc.dcc.imports.gene.model.ProteinFeature;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Constructs the protein domains
 */
@RequiredArgsConstructor
public final class DomainReader {

  private final Map<String, String> transMap;
  private final InterproReader interproReader;
  private final AnalysisReader analysisReader;

  public Map<String, List<ProteinFeature>> createProteinFeatures() {
    val analysisMap = analysisReader.getAnalysisMap();
    val interproMap = interproReader.getInterproMap();

    val retMap = new HashMap<String, List<ProteinFeature>>();

    BaseReader.read(PROTEIN_FEATURE_URI, line -> {
      if (analysisMap.containsKey(line[7])) {
        if (interproMap.containsKey(line[6])) {
          ProteinFeature pf = interproMap.get(line[6]).copy();
          pf.setStart(Integer.parseInt(line[2]));
          pf.setEnd(Integer.parseInt(line[3]));
          pf.setAnalysisId(line[8]);
          pf.setGffSource(analysisMap.get(line[7]));
          String ens = transMap.get(line[1]);

          if (retMap.containsKey(ens)) {
            retMap.get(ens).add(pf);
          } else {
            List<ProteinFeature> al = new ArrayList<ProteinFeature>();
            al.add(pf);
            retMap.put(ens, al);
          }
        }

      }
    });

    return retMap;
  }

}