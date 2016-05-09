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
package org.icgc.dcc.imports.drug.writer;

import static com.google.common.base.Stopwatch.createStarted;
import static org.icgc.dcc.common.core.model.ReleaseCollection.DRUG_COLLECTION;
import static org.icgc.dcc.common.core.model.ReleaseCollection.GENE_COLLECTION;
import static org.icgc.dcc.imports.drug.util.Drugs.getZincId;

import java.util.List;

import org.icgc.dcc.imports.core.util.AbstractJongoWriter;
import org.jongo.MongoCollection;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClientURI;

import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DrugWriter extends AbstractJongoWriter<List<ObjectNode>> {

  public DrugWriter(@NonNull MongoClientURI mongoUri) {
    super(mongoUri);
  }

  @Override
  public void writeValue(@NonNull List<ObjectNode> drugs) {
    val watch = createStarted();

    log.info("Writing drugs to {}...", mongoUri);
    writeDrugs(drugs);

    log.info("Writing gene gene sets to {}...", mongoUri);
    writeGeneGeneSets(drugs);

    log.info("Finished writing gene sets and gene gene sets in {}", watch);
  }

  private void writeDrugs(List<ObjectNode> drugs) {
    log.info("Dropping current Drug collection...");
    dropCollection();

    log.info("Saving new Drug collection...");
    saveCollection(drugs);
  }

  private void writeGeneGeneSets(List<ObjectNode> drugs) {
    new DrugGeneGeneSetWriter(getCollection(GENE_COLLECTION)).write(drugs);
  }
  
  private void dropCollection() {
    getDrugsCollection().drop();
  }

  private void saveCollection(List<ObjectNode> drugs) {
    val total = drugs.size();
    val drugsCollection = getDrugsCollection();
    
    int current = 1;
    for (val drug : drugs) {
      val zincId = getZincId(drug);
      drug.put("_id", zincId);
      try {
        log.info("Writing {}/{} with id: {}", current, total, zincId);
        drugsCollection.save(drug);
      } catch (Exception e) {
        log.warn(e.getMessage());
      }
      current++;
    }
  }

  private MongoCollection getDrugsCollection() {
    return getCollection(DRUG_COLLECTION);
  }

}
