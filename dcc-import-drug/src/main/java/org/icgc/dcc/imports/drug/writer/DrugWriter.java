/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
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

import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.icgc.dcc.common.core.model.ReleaseCollection;
import org.icgc.dcc.imports.core.util.AbstractJongoWriter;
import org.jongo.MongoCollection;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClientURI;

@Slf4j
public class DrugWriter extends AbstractJongoWriter<List<ObjectNode>> {

  private MongoCollection drugCollection;

  public DrugWriter(@NonNull MongoClientURI mongoUri) {
    super(mongoUri);
  }

  @Override
  public void writeFiles(@NonNull List<ObjectNode> values) {
    drugCollection = getCollection(ReleaseCollection.DRUG_COLLECTION);
    
    log.info("Dropping current Drug collection...");
    dropCollection();

    log.info("Saving new Drug collection...");
    saveCollection(values);
  }

  private void dropCollection() {
    drugCollection.drop();
  }

  private void saveCollection(List<ObjectNode> drugs) {
    val total = drugs.size();
    int current = 1;
    log.info("Number to save: {}", total);
    for (val drug : drugs) {
      drug.put("_id", drug.get("zinc_id").asText());
      val genes = (ArrayNode) drug.get("genes");
      val drugClass = drug.get("drug_class").asText();
      if (genes.size() > 0 && (drugClass.equalsIgnoreCase("fda") || drugClass.equalsIgnoreCase("world"))) {
        try {
          log.info("Writing {}/{} with id: {}", current, total, drug.get("zinc_id").asText());
          drugCollection.save(drug);
        } catch (Exception e) {
          log.warn(e.getMessage());
        }
        current++;
      }
    }
  }

}
