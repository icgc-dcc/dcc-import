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
package org.icgc.dcc.imports.drug.reader;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.val;

public class TrialsReader extends Reader {
  
  private final static String trialsUrl = "http://files.docking.org/export/oicr/trials.ldjson";
  private final ObjectMapper MAPPER = new ObjectMapper();
  private MappingIterator<ObjectNode> trials;
  
  public TrialsReader() {
    super(trialsUrl);
  }
  
  public MappingIterator<ObjectNode> getTrials() {
    trials = getJson();
    return trials;
  }

  /**
   * Gets the trials JSON as a HashMap keyed off of the trials codes or speedy access. 
   * @return HashMap of trials keyed off of trial code.
   */
  public Map<String, ObjectNode> getTrialsMap() {
    val trialsMap = new HashMap<String, ObjectNode>();
    val conditions = new ConditionsReader().getConditionsAsMap();
    getJson().forEachRemaining(trial -> {    
      cleanDrugMappings(trial);
      joinConditions(trial, conditions);
      trialsMap.put(trial.get("code").asText(), trial);
    });
    
    return trialsMap;
  }
  
  /**
   * Ensures the drug mappings for the given trial are safe for mongodb
   * @param trial ObjectNode representing trial
   */
  public void cleanDrugMappings(ObjectNode trial) {
    val drugMappings = (ObjectNode) trial.get("drug_mappings");
    val newMappingsArray = MAPPER.createArrayNode();
    
    drugMappings.fields().forEachRemaining(entry -> {
      ObjectNode newMapping = MAPPER.createObjectNode();
      newMapping.put("description", entry.getKey());
      newMapping.put("ids", entry.getValue());
      newMappingsArray.add(newMapping);
    });
    
    trial.remove("drug_mappings");
    trial.put("drug_mappings", newMappingsArray);
  }
  
  /**
   * Conditions are nested under trials. 
   */
  public void joinConditions(ObjectNode trial, Map<String, ObjectNode> conditions) {
    val trialConditions = (ArrayNode) trial.get("condition_short_names");
    val conditionsArray = MAPPER.createArrayNode();
    
    trialConditions.forEach(tCondition -> {
      ObjectNode fullCondition = conditions.get(tCondition.asText());
      if (fullCondition != null) {
        fullCondition.remove("class_name");
        conditionsArray.add(fullCondition);
      }
    });
    
    trial.remove("condition_short_names");
    trial.put("conditions", conditionsArray);
  }
  
}
