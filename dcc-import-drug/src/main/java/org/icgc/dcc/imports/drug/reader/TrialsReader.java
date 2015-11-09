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
      joinConditions(trial, conditions);
      trialsMap.put(trial.get("code").asText(), trial);
    });
    
    return trialsMap;
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
