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
package org.icgc.dcc.imports.go.util;

import static org.icgc.dcc.imports.go.reader.GoInferredTreeReader.RELATION_IDS;

import java.io.IOException;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.icgc.dcc.imports.go.GoImporter;
import org.icgc.dcc.imports.go.reader.GoInferredTreeReader;
import org.icgc.dcc.imports.go.util.GoInferredTreeBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

@Slf4j
@Ignore("This is being testing by the GoImporterTest. Useful for debugging though.")
public class GoInferredTreeBuilderTest {

  @Test
  public void testBuild() throws OWLOntologyCreationException, OBOFormatParserException, IOException {
    val graph = new GoInferredTreeReader(GoImporter.DEFAULT_OBO_URL).readGraph();
    val goId = "GO:0005794";
    val goTerm = graph.getOWLClassByIdentifier(goId);

    val inferredTreeBuilder = new GoInferredTreeBuilder(graph, RELATION_IDS);
    val inferredTree = inferredTreeBuilder.build(goTerm);
    log.info("Inferred tree: {}", inferredTree);
  }

}
