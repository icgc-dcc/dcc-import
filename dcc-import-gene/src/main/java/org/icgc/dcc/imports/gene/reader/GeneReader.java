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

import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Reader for getting gene related maps
 */
@Slf4j
public final class GeneReader {

  /**
   * Constants
   */
  private static final String GENE_URI =
      "ftp://ftp.ensembl.org/pub/grch37/release-82/mysql/homo_sapiens_core_82_37/gene.txt.gz";

  /**
   * Caching
   */
  private static Map<String, String> geneIdMap = null;

  /**
   * Creates mapping of internal gene id to stable gene id
   * @return Map of gene_id -> stable_id
   */
  @SneakyThrows
  public static Map<String, String> geneIdMap() {
    if (geneIdMap != null) {
      log.info("Using cached gene ID map.");
      return geneIdMap;
    }

    geneIdMap = new HashMap<String, String>();
    BaseReader.read(GENE_URI, line -> {
      String id = line[0];
      String geneStableId = line[13];
      geneIdMap.put(id, geneStableId);
    });

    return geneIdMap;
  }

  /**
   * Creates a map of stable_id to the canonical transcript id. Also caches gene Id Map in the process.
   * @return Map of stable_id (ENSG*) -> CanonicalTranscript
   */
  @SneakyThrows
  public static Map<String, String> canonicalMap() {
    val transcriptMap = TransReader.getTranscriptMap();

    geneIdMap = new HashMap<String, String>();
    val retMap = new HashMap<String, String>();
    BaseReader.read(GENE_URI, line -> {
      String id = line[0];
      String geneId = line[13];
      String canonicalTranscript = transcriptMap.get(line[12]);
      retMap.put(geneId, canonicalTranscript);
      geneIdMap.put(id, geneId);
    });

    return retMap;
  }

}
