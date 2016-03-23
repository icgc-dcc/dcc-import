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

  private static final String BASE_URI = "ftp://ftp.ensembl.org/pub/grch37/release-83/mysql/homo_sapiens_core_83_37/";
  public static final String GENE_URI = BASE_URI + "gene.txt.gz";
  public static final String XREF_URI = BASE_URI + "xref.txt.gz";
  public static final String EXTERNAL_DB_URI = BASE_URI + "external_db.txt.gz";
  public static final String OBJECT_XREF_URI = BASE_URI + "object_xref.txt.gz";
  public static final String EXTERNAL_SYN_URI = BASE_URI + "external_synonym.txt.gz";
  public static final String TRANSCRIPT_URI = BASE_URI + "transcript.txt.gz";
  public static final String TRANSLATION_URI = BASE_URI + "translation.txt.gz";
  public static final String PROTEIN_FEATURE_URI = BASE_URI + "protein_feature.txt.gz";
  public static final String INTERPRO_URI = BASE_URI + "interpro.txt.gz";
  public static final String ANALYSIS_URI = BASE_URI + "analysis.txt.gz";

  public static final String NCBI_URI =
      "ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/ASN_BINARY/Mammalia/Homo_sapiens.ags.gz";
}
