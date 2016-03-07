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

import static org.icgc.dcc.imports.gene.core.Sources.GENE_URI;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Reader for getting gene related maps
 */
@Data
@NoArgsConstructor
public class GeneReader {

  /**
   * State
   */
  private final Map<String, String> geneIdMap = new HashMap<>();
  private final Map<String, String> canonicalMap = new HashMap<>();
  private final Map<String, String> xrefGeneMap = new HashMap<>();

  @SneakyThrows
  public void read() {
    val transcriptMap = TransReader.getTranscriptMap();

    BaseReader.read(GENE_URI, line -> {
      String id = line[0];
      String displayXrefId = line[7];
      String geneId = line[13];
      String canonicalTranscript = transcriptMap.get(line[12]);
      canonicalMap.put(geneId, canonicalTranscript);
      geneIdMap.put(id, geneId);
      xrefGeneMap.put(displayXrefId, geneId);
    });
  }

}
