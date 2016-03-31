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

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.imports.gene.model.TranscriptMapping;
import org.icgc.dcc.imports.gene.model.TranslationMapping;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import lombok.val;

public class TranslationReader extends TsvReader {

  /**
   * Dependencies
   */
  private final Map<String, String> transcriptToGene;

  public TranslationReader(String uri, @NonNull TranscriptMapping transcriptMapping) {
    super(uri);
    this.transcriptToGene = transcriptMapping.getTranscriptToGene();
  }

  public TranslationMapping read() {
    val translationMap = readRecords()
        .collect(collectingAndThen(
            toMap(this::getTranscriptId, this::getTranslationId),
            ImmutableMap::copyOf));

    val translationToGeneBuilder = ImmutableMap.<String, String> builder();
    for (val entry : translationMap.entrySet()) {
      val translationId = entry.getValue();
      val geneId = transcriptToGene.get(entry.getKey());
      translationToGeneBuilder.put(translationId, geneId);
    }

    val translationToGene = translationToGeneBuilder.build();

    return TranslationMapping.builder()
        .translationMap(translationMap)
        .translationToGene(translationToGene)
        .build();
  }

  private String getTranscriptId(List<String> record) {
    return record.get(1);
  }

  private String getTranslationId(List<String> record) {
    return record.get(0);
  }

}
