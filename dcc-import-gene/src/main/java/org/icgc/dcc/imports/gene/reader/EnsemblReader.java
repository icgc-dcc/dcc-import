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

import org.icgc.dcc.imports.gene.core.TransJoiner;
import org.icgc.dcc.imports.gene.model.Ensembl;

import lombok.val;

public class EnsemblReader {

  public Ensembl read() {
    val transcriptReader = new TranscriptReader().read();

    val geneReader = new GeneReader(transcriptReader).read();
    val canonicalMap = geneReader.getCanonicalMap();

    val externalDBReader = new ExternalDatabaseReader().read();

    val synReader = new SynonymReader(geneReader.getXrefGeneMap()).read();
    val synMap = synReader.getSynMap();

    val translationReader = new TranslationReader(transcriptReader).read();

    val transJoiner = new TransJoiner(translationReader, transcriptReader);
    val transMap = transJoiner.joinTrans();

    val xrefReader = new XrefReader(externalDBReader).read();
    val nameMap = xrefReader.getNameMap();

    val analysisReader = new AnalysisReader().read();
    val interproReader = new InterproReader(xrefReader).read();
    val domainReader = new DomainReader(transMap, interproReader, analysisReader);
    val pFeatures = domainReader.createProteinFeatures();

    val externalReader = new ExternalReader(xrefReader, geneReader, translationReader).read();
    val externalIds = externalReader.getExternalIds();

    val ensembl = Ensembl.builder()
        .nameMap(nameMap)
        .synonymMap(synMap)
        .canonicalMap(canonicalMap)
        .pFeatures(pFeatures)
        .externalIds(externalIds)
        .build();

    return ensembl;
  }

}
