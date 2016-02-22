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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.icgc.dcc.imports.gene.model.ProteinFeature;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Constructs the protein domains
 */
@Slf4j
public class DomainReader {

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

  private static final Pattern TSV = Pattern.compile("\t");

  @SneakyThrows
  public static Map<String, List<ProteinFeature>> createProteinFeatures(@NonNull Map<String, String> transMap) {

    val gzip = new GZIPInputStream(new URL(PROTEIN_FEATURE_URI).openStream());
    val inputStreamReader = new InputStreamReader(gzip);
    val bufferedReader = new BufferedReader(inputStreamReader);

    val interproMap = interproMap();
    val analysisMap = analysisMap();

    val retMap = new HashMap<String, List<ProteinFeature>>();
    for (String s = bufferedReader.readLine(); null != s; s = bufferedReader.readLine()) {
      s = s.trim();
      if (s.length() > 0) {
        String[] line = TSV.split(s);

        // Check to see if intepro map has this hit name
        if (analysisMap.containsKey(line[7])) {
          if (interproMap.containsKey(line[6])) {
            val pf = interproMap.get(line[6]).getCopy();
            pf.setStart(Integer.parseInt(line[2]));
            pf.setEnd(Integer.parseInt(line[3]));
            pf.setAnalysisId(line[8]);
            pf.setGffSource(analysisMap.get(line[7]));
            val ens = transMap.get(line[1]);

            if (retMap.containsKey(ens)) {
              retMap.get(ens).add(pf);
            } else {
              val al = new ArrayList<ProteinFeature>();
              al.add(pf);
              retMap.put(ens, al);
            }
          }

        }

      }
    }

    return retMap;

  }

  @SneakyThrows
  public static Map<String, ProteinFeature> interproMap() {

    val gzip = new GZIPInputStream(new URL(INTERPRO_URI).openStream());
    val inputStreamReader = new InputStreamReader(gzip);
    val bufferedReader = new BufferedReader(inputStreamReader);

    val descriptionMap = getInterproFromXref();

    val retMap = new HashMap<String, ProteinFeature>();
    for (String s = bufferedReader.readLine(); null != s; s = bufferedReader.readLine()) {
      s = s.trim();
      if (s.length() > 0) {
        String[] line = TSV.split(s);
        val pf = new ProteinFeature(line[0], line[1], descriptionMap.get(line[0]));
        retMap.put(line[1], pf);
      }
    }

    return retMap;
  }

  /**
   * Gets the Interpro id and description from the xref table.
   * @return Hashmap of descriptions keyed to Interpro ids.
   */
  @SneakyThrows
  public static Map<String, String> getInterproFromXref() {

    val gzip = new GZIPInputStream(new URL(XREF_URI).openStream());
    val inputStreamReader = new InputStreamReader(gzip);
    val bufferedReader = new BufferedReader(inputStreamReader);

    val interproDbId = getInterproDB();

    val retMap = new HashMap<String, String>();
    for (String s = bufferedReader.readLine(); null != s; s = bufferedReader.readLine()) {
      s = s.trim();
      if (s.length() > 0) {
        String[] line = TSV.split(s);
        if (interproDbId.equals(line[1])) {
          retMap.put(line[2], line[5]);
        }
      }
    }

    return retMap;
  }

  @SneakyThrows
  private static String getInterproDB() {

    val gzip = new GZIPInputStream(new URL(EXTERNAL_DB_URI).openStream());
    val inputStreamReader = new InputStreamReader(gzip);
    val bufferedReader = new BufferedReader(inputStreamReader);

    for (String s = bufferedReader.readLine(); null != s; s = bufferedReader.readLine()) {
      s = s.trim();
      if (s.length() > 0) {
        String[] line = TSV.split(s);
        if (line[1].equals("Interpro")) {
          log.info("Interpro db id: {}", line[0]);
          return line[0];
        }
      }
    }

    return "";
  }

  @SneakyThrows
  private static Map<String, String> analysisMap() {

    val gzip = new GZIPInputStream(new URL(ANALYSIS_URI).openStream());
    val inputStreamReader = new InputStreamReader(gzip);
    val bufferedReader = new BufferedReader(inputStreamReader);

    val retMap = new HashMap<String, String>();
    for (String s = bufferedReader.readLine(); null != s; s = bufferedReader.readLine()) {
      s = s.trim();
      if (s.length() > 0) {
        String[] line = TSV.split(s);
        val id = line[0];
        val gffSource = line[6];
        if ("pfam".equals(gffSource)) {
          retMap.put(id, gffSource);
        }
      }
    }

    return retMap;
  }

}
