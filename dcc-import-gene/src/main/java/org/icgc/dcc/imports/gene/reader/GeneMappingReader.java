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

import org.icgc.dcc.imports.gene.model.GeneMapping;
import org.icgc.dcc.imports.gene.model.TranscriptMapping;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;

import lombok.NonNull;
import lombok.val;

/**
 * Reader for getting gene related maps
 */
public class GeneMappingReader extends TsvReader {

  /**
   * Dependencies
   */
  private final TranscriptMapping transcriptMapping;

  public GeneMappingReader(String uri, @NonNull TranscriptMapping transcriptMapping) {
    super(uri);
    this.transcriptMapping = transcriptMapping;
  }

  public GeneMapping read() {
    val geneIdMap = ImmutableMap.<String, String> builder();
    val xrefGeneMap = ImmutableMultimap.<String, String> builder();
    val canonicalMap = ImmutableMap.<String, String> builder();
    readRecords().forEach(record -> {
      geneIdMap.put(getId(record), getGeneId(record));
      xrefGeneMap.put(getDisplayXrefId(record), getGeneId(record));
      canonicalMap.put(getGeneId(record), getCanonicalTranscript(record));
    });

    return GeneMapping.builder()
        .geneIdMap(geneIdMap.build())
        .xrefGeneMap(xrefGeneMap.build())
        .canonicalMap(canonicalMap.build())
        .build();
  }

  private String getId(List<String> record) {
    return record.get(0);
  }

  private String getDisplayXrefId(List<String> record) {
    return record.get(7);
  }

  private String getGeneId(List<String> record) {
    return record.get(13);
  }

  private String getCanonicalTranscript(List<String> record) {
    return transcriptMapping
        .getTranscriptMap()
        .get(getTranscriptId(record));
  }

  private String getTranscriptId(List<String> record) {
    return record.get(12);
  }

}
