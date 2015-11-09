package org.icgc.dcc.imports.drug.reader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.SneakyThrows;
import lombok.val;

public class ConditionsReader extends Reader {
  
  private final static String conditionsUrl = "http://files.docking.org/export/oicr/conditions.ldjson";
  
  public ConditionsReader() {
    super(conditionsUrl);
  }
  
  public MappingIterator<ObjectNode> getConditions() {
    val conditions = getJson();
    return conditions;
  }
  
  @SneakyThrows
  public List<ObjectNode> getConditionsAsList() {
    return getConditions().readAll();
  }
  
  public Map<String, ObjectNode> getConditionsAsMap() {
    val conditionsMap = new HashMap<String, ObjectNode>();
    getConditions().forEachRemaining(condition -> {
      conditionsMap.put(condition.get("short_name").asText(), condition);
    });
    
    return conditionsMap;
  }

}
