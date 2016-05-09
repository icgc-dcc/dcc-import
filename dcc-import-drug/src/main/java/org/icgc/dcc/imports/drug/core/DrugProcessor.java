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
package org.icgc.dcc.imports.drug.core;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DrugProcessor  {

  /**
   * Constants
   */
  private final static ObjectMapper MAPPER = new ObjectMapper();
  private final static List<String> EXCLUDED_GENE_FIELDS = ImmutableList.<String> of(
      "chembl",
      "description",
      "gene_name",
      "name");
  private final static List<String> INCLUDED_DRUG_FIELDS = ImmutableList.<String> of(
      "inchikey",
      "name",
      "genes",
      "trials",
      "zinc_id",
      "drug_class",
      "drug_classes",
      "external_references",
      "synonyms",
      "atc_codes",
      "small_image_url",
      "large_image_url",
      "cancer_trial_count",
      "_id");
  
  /**
   * Dependencies.
   */
  @NonNull
  private final Map<String, ObjectNode> geneMap;
  @NonNull
  private final Map<String, ObjectNode> trialMap;
  
  public List<ObjectNode> process(List<ObjectNode> drugs) {
    return drugs.stream()
    .map(this::expandImageUrls)
    .map(this::joinGenes)
    .map(this::joinTrials)
    .map(this::denormalizeAtcCodes)
    .map(this::cleanSynonyms)
    .map(this::cleanDrug)
    .filter(DrugFilter::filter)
    .collect(toImmutableList());
  }

  /**
   * Creates nodes that contain URLs for the small and large versions of the molecule image
   */
  private ObjectNode expandImageUrls(ObjectNode drug) {
    val imageUrl = drug.get("image_url").asText();
    val largeImageUrl = imageUrl.replace(".png", "-large.png");

    drug.put("small_image_url", imageUrl);
    drug.put("large_image_url", largeImageUrl);
    drug.remove("image_url");

    return drug;
  }

  /**
   * Joins Genes to Drug by gene name. We include ensembl ids as part of gene node.
   */
  private ObjectNode joinGenes(ObjectNode drug) {
    val drugGenes = drug.get("genes");
    val geneArray = MAPPER.createArrayNode();

    if (drugGenes.isArray()) {
      for (val geneName : drugGenes) {
        val gene = geneMap.get(geneName.asText());
        if (gene != null) {
          geneArray.add(gene.remove(EXCLUDED_GENE_FIELDS));
        } else {
          log.warn("Gene missing on join: {}", geneName.asText());
        }
      }
    }

    drug.set("genes", geneArray);

    return drug;
  }

  /**
   * Joins trials to Drugs by trial code. Trials will be already joined with conditions.
   */
  private  ObjectNode joinTrials(ObjectNode drug) {
    val drugTrials = drug.get("trials");
    val trialsArray = MAPPER.createArrayNode();

    if (drugTrials.isArray()) {
      for (val trialCode : drugTrials) {
        val trial = trialMap.get(trialCode.asText());
        if (trial != null) {
          trialsArray.add(trial);
        } else {
          log.warn("Trail missing on join: {}", trialCode.asText());
        }
      }
    }

    drug.put("cancer_trial_count", trialsArray.size());
    drug.set("trials", trialsArray);

    return drug;
  }

  /**
   * Removes synonyms that match drug name
   */
  private ObjectNode cleanSynonyms(ObjectNode drug) {
    val synonyms = (ArrayNode) drug.get("synonyms");
    val cleaned = MAPPER.createArrayNode();

    if (synonyms != null) {
      synonyms.forEach(entry -> {
        if (!entry.asText().equalsIgnoreCase(drug.get("name").asText())) {
          cleaned.add(entry);
        }
      });
      drug.remove("synonyms");
      drug.put("synonyms", cleaned);
    }

    return drug;
  }

  /**
   * Moves level5 ATC codes into the main ATC code JSON node.
   */
  private ObjectNode denormalizeAtcCodes(ObjectNode drug) {
    val atcCodes = (ArrayNode) drug.get("atc_codes");
    val level5 = (ArrayNode) drug.get("atc_level5_codes");

    if (atcCodes != null) {
      atcCodes.forEach(atc -> {
        for (JsonNode code : level5) {
          if (code.asText().indexOf(atc.get("code").asText()) >= 0) {
            ((ObjectNode) atc).put("atc_level5_codes", code.asText());
            break;
          }
        }
      });
      drug.remove("atc_level5_codes");
    } else {
      val atcClasses = (ArrayNode) drug.get("atc_classifications");
      if (atcClasses != null) {
        ArrayNode newAtcCodes = MAPPER.createArrayNode();
        atcClasses.forEach(atcClass -> {
          ObjectNode newAtcEntry = MAPPER.createObjectNode();
          newAtcEntry.put("code", atcClass.get("level4").asText());
          newAtcEntry.put("atc_level5_codes", atcClass.get("level5").asText());
          newAtcEntry.put("description", atcClass.get("level4_description").asText());
          newAtcCodes.add(newAtcEntry);
        });
        drug.set("atc_codes", newAtcCodes);
        drug.remove("atc_classifications");
      }
    }

    return drug;
  }

  private  ObjectNode cleanDrug(ObjectNode drug) {
    drug.retain(INCLUDED_DRUG_FIELDS);
    return drug;
  }

}
