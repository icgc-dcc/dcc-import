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

import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Constructs the protein domains
 */
@Slf4j
public final class DomainReader {

  private static final String XREF_URI =
      "ftp://ftp.ensembl.org/pub/grch37/release-82/mysql/homo_sapiens_core_82_37/xref.txt.gz";
  private static final String EXTERNAL_DB_URI =
      "ftp://ftp.ensembl.org/pub/grch37/release-82/mysql/homo_sapiens_core_82_37/external_db.txt.gz";
  private static final String PROTEIN_FEATURE_URI =
      "ftp://ftp.ensembl.org/pub/grch37/release-82/mysql/homo_sapiens_core_82_37/protein_feature.txt.gz";
  private static final String INTERPRO_URI =
      "ftp://ftp.ensembl.org/pub/grch37/release-82/mysql/homo_sapiens_core_82_37/interpro.txt.gz";
  private static final String ANALYSIS_URI =
      "ftp://ftp.ensembl.org/pub/grch37/release-82/mysql/homo_sapiens_core_82_37/analysis.txt.gz";

  public static Map<String, List<ProteinFeature>> createProteinFeatures(@NonNull Map<String, String> transMap) {

    val interproMap = interproMap();
    val analysisMap = analysisMap();

    val retMap = new HashMap<String, List<ProteinFeature>>();

    BaseReader.read(PROTEIN_FEATURE_URI, line -> {
      if (analysisMap.containsKey(line[7])) {
        if (interproMap.containsKey(line[6])) {
          ProteinFeature pf = interproMap.get(line[6]).getCopy();
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

  /**
   * Returns a map of protein features
   * @return HashMap of id -> protein feature
   */
  public static Map<String, ProteinFeature> interproMap() {

    val descriptionMap = getInterproFromXref();
    val retMap = new HashMap<String, ProteinFeature>();

    BaseReader.read(INTERPRO_URI, line -> {
      ProteinFeature pf = new ProteinFeature(line[0], line[1], descriptionMap.get(line[0]));
      retMap.put(line[1], pf);
    });

    return retMap;
  }

  /**
   * Gets the Interpro id and description from the xref table.
   * @return Hashmap of descriptions keyed to Interpro ids.
   */
  public static Map<String, String> getInterproFromXref() {

    val interproDbId = getInterproDB();

    val retMap = new HashMap<String, String>();
    BaseReader.read(XREF_URI, line -> {
      if (interproDbId.equals(line[1])) {
        String description = line[5];
        String id = line[2];
        retMap.put(id, description);
      }
    });

    return retMap;
  }

  private static String getInterproDB() {

    val interproId = new StringBuilder();
    BaseReader.read(EXTERNAL_DB_URI, line -> {
      if (line.length > 1 && line[1].equals("Interpro")) {
        interproId.append(line[0]);
      }
    });

    log.info("Interpro db id: {}", interproId.toString());
    return interproId.toString();
  }

  private static Map<String, String> analysisMap() {

    val retMap = new HashMap<String, String>();

    BaseReader.read(ANALYSIS_URI, line -> {
      String id = line[0];
      String gffSource = line[6];

      // We can support additional programs/algorithms for protein domains here
      if ("pfam".equals(gffSource)) {
        retMap.put(id, gffSource);
      }
    });

    return retMap;
  }

}
