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

import static org.icgc.dcc.imports.gene.core.Sources.ANALYSIS_URI;
import static org.icgc.dcc.imports.gene.core.Sources.EXON_URI;
import static org.icgc.dcc.imports.gene.core.Sources.EXTERNAL_DB_URI;
import static org.icgc.dcc.imports.gene.core.Sources.EXTERNAL_SYN_URI;
import static org.icgc.dcc.imports.gene.core.Sources.GENE_URI;
import static org.icgc.dcc.imports.gene.core.Sources.INTERPRO_URI;
import static org.icgc.dcc.imports.gene.core.Sources.OBJECT_XREF_URI;
import static org.icgc.dcc.imports.gene.core.Sources.PROTEIN_FEATURE_URI;
import static org.icgc.dcc.imports.gene.core.Sources.TRANSCRIPT_URI;
import static org.icgc.dcc.imports.gene.core.Sources.TRANSLATION_URI;
import static org.icgc.dcc.imports.gene.core.Sources.XREF_URI;

import org.icgc.dcc.imports.gene.core.TransJoiner;
import org.icgc.dcc.imports.gene.model.Ensembl;

import lombok.val;

public class EnsemblReader {

  public Ensembl read() {
    val transcriptMapping = new TranscriptReader(TRANSCRIPT_URI).read();
    val translationMapping = new TranslationReader(TRANSLATION_URI, transcriptMapping).read();
    val transJoiner = new TransJoiner(translationMapping, transcriptMapping);
    val transMap = transJoiner.joinTrans();

    val interproDBId = new ExternalDatabaseReader(EXTERNAL_DB_URI).read();
    val analysisMap = new AnalysisReader(ANALYSIS_URI).read();
    val exonMap = new ExonReader(EXON_URI).read();
    val geneMapping = new GeneMappingReader(GENE_URI, transcriptMapping).read();
    val synMap = new SynonymReader(EXTERNAL_SYN_URI, geneMapping.getXrefGeneMap()).read();

    val xrefMapping = new XrefReader(XREF_URI, interproDBId).read();

    val interproMap = new InterproReader(INTERPRO_URI, xrefMapping).read();
    val pFeatures = new DomainReader(PROTEIN_FEATURE_URI, transMap, interproMap, analysisMap).read();

    val externalIds = new ExternalReader(OBJECT_XREF_URI, xrefMapping, geneMapping, translationMapping).read();

    val ensembl = Ensembl.builder()
        .nameMap(xrefMapping.getNameMap())
        .synonymMap(synMap)
        .exonPhaseMap(exonMap)
        .canonicalMap(geneMapping.getCanonicalMap())
        .pFeatures(pFeatures)
        .externalIds(externalIds)
        .build();

    return ensembl;
  }

}
