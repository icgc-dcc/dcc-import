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
package org.icgc.dcc.imports.gene.core;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class Sources {

  /**
   * Versions.
   */
  private static final int ASSEMBLY_VERSION = 38;
  private static final int ENSEMBL_RELEASE = 80;

  /**
   * Ensembl files.
   */
  private static final String ENSEMBL_RELEASE_URI =
      "ftp://ftp.ensembl.org/pub/release-" + ENSEMBL_RELEASE + "/";

  public static final String GTF_URI =
      ENSEMBL_RELEASE_URI + "gtf/homo_sapiens/Homo_sapiens.GRCh" + ASSEMBLY_VERSION + "." + ENSEMBL_RELEASE + ".gtf.gz";

  private static final String ENSEMBL_MYSQL_URI =
      ENSEMBL_RELEASE_URI + "mysql/homo_sapiens_core_" + ENSEMBL_RELEASE + "_" + ASSEMBLY_VERSION + "/";

  public static final String GENE_URI = ENSEMBL_MYSQL_URI + "gene.txt.gz";
  public static final String XREF_URI = ENSEMBL_MYSQL_URI + "xref.txt.gz";
  public static final String EXTERNAL_DB_URI = ENSEMBL_MYSQL_URI + "external_db.txt.gz";
  public static final String OBJECT_XREF_URI = ENSEMBL_MYSQL_URI + "object_xref.txt.gz";
  public static final String EXTERNAL_SYN_URI = ENSEMBL_MYSQL_URI + "external_synonym.txt.gz";
  public static final String TRANSCRIPT_URI = ENSEMBL_MYSQL_URI + "transcript.txt.gz";
  public static final String TRANSLATION_URI = ENSEMBL_MYSQL_URI + "translation.txt.gz";
  public static final String PROTEIN_FEATURE_URI = ENSEMBL_MYSQL_URI + "protein_feature.txt.gz";
  public static final String INTERPRO_URI = ENSEMBL_MYSQL_URI + "interpro.txt.gz";
  public static final String ANALYSIS_URI = ENSEMBL_MYSQL_URI + "analysis.txt.gz";
  public static final String EXON_URI = ENSEMBL_MYSQL_URI + "exon.txt.gz";

  /**
   * NCBI files.
   */
  public static final String NCBI_URI =
      "ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/ASN_BINARY/Mammalia/Homo_sapiens.ags.gz";

}
