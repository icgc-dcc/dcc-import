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
package org.icgc.dcc.imports.gene.writer;

import static org.icgc.dcc.common.core.util.Formats.formatCount;

import org.icgc.dcc.common.core.model.ReleaseCollection;
import org.icgc.dcc.imports.core.util.AbstractJongoWriter;
import org.jongo.MongoCollection;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClientURI;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneWriter extends AbstractJongoWriter<ObjectNode> {

  /**
   * Constants
   */
  private static final int STATUS_GENE_COUNT = 10000;

  /**
   * State
   */
  private int counter = 0;
  private final MongoCollection geneCollection;

  public GeneWriter(MongoClientURI mongoUri) {
    super(mongoUri);
    this.geneCollection = getCollection(ReleaseCollection.GENE_COLLECTION);

    log.info("Dropping Gene collection...");
    geneCollection.drop();
  }

  @Override
  public void writeValue(ObjectNode value) {
    if (++counter % STATUS_GENE_COUNT == 0) {
      log.info("Writing {}", formatCount(counter));
    }

    geneCollection.insert(value);
  }

}
