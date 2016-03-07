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

import static org.icgc.dcc.imports.gene.core.Sources.TRANSCRIPT_URI;
import static org.icgc.dcc.imports.gene.core.Sources.TRANSLATION_URI;

import java.util.HashMap;
import java.util.Map;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Class responsible for reading Transcript and Translation files and mapping Stable Transcript Id to Translation Id
 */
@Slf4j
public final class TransReader {

  /**
   * Caching
   */
  private static Map<String, String> transcriptMap = null;
  private static Map<String, String> translationMap = null;
  private static Map<String, String> transcriptToGene = null;

  /**
   * Method for constructing a map of translations to genes.
   * @return Map of translation_id -> gene_id
   */
  public static Map<String, String> translationToGene() {
    val translationMap = getTranslationMap();

    val retMap = new HashMap<String, String>();
    for (val entry : translationMap.entrySet()) {
      val translationId = entry.getValue();
      val geneId = transcriptToGene.get(entry.getKey());

      retMap.put(translationId, geneId);
    }

    return retMap;
  }

  /**
   * Joins transcripts with translations.
   * @return Map of translation_id to transcript stable id (ENST*)
   */
  public static Map<String, String> joinTrans() {
    val transcriptMap = getTranscriptMap();
    val translationMap = getTranslationMap();

    val retMap = new HashMap<String, String>();
    for (val entry : translationMap.entrySet()) {
      retMap.put(entry.getValue(), transcriptMap.get(entry.getKey()));
    }

    return retMap;
  }

  /**
   * Creates mapping between transcript id and stable transcript id. Caches a map of transcript_id to gene_id.
   * @return a Map of transcript_id -> stable_id (ENST*)
   */
  public static Map<String, String> getTranscriptMap() {
    if (transcriptMap != null) {
      log.info("Using cached trancript map.");
      return transcriptMap;
    }

    transcriptMap = new HashMap<String, String>();
    transcriptToGene = new HashMap<String, String>();
    BaseReader.read(TRANSCRIPT_URI, line -> {
      String id = line[0];
      String stableId = line[14];
      String geneId = line[1];
      transcriptMap.put(id, stableId);
      transcriptToGene.put(id, geneId);
    });

    return transcriptMap;
  }

  /**
   * Creates a mapping between translations and transcripts
   * @return A map of transcript id to translation id.
   */
  private static Map<String, String> getTranslationMap() {
    if (translationMap != null) {
      log.info("Using cached trancript map.");
      return translationMap;
    }

    translationMap = new HashMap<String, String>();
    BaseReader.read(TRANSLATION_URI, line -> {
      String transcriptId = line[1];
      String translationId = line[0];
      translationMap.put(transcriptId, translationId);
    });
    return translationMap;
  }

}
