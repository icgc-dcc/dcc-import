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
package org.icgc.dcc.imports.gene.core;

import static org.icgc.dcc.common.core.util.Splitters.SEMICOLON;
import static org.icgc.dcc.common.core.util.Splitters.TAB;
import static org.icgc.dcc.common.json.Jackson.DEFAULT;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asInt;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asText;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.attachDomains;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.computeEndRegion;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.computeStartRegion;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.constructExonNode;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.constructTranscriptNode;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.exonDefaults;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.icgc.dcc.imports.gene.model.Ensembl;
import org.icgc.dcc.imports.gene.model.ProteinFeature;
import org.icgc.dcc.imports.gene.writer.GeneWriter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GeneConstructor {

  /**
   * Constants
   */
  private final static Pattern QUOTES = Pattern.compile("\"");
  private final static Pattern UNKNOWN_WHITESPACE = Pattern.compile("\\s+");

  /**
   * Dependencies
   */
  private final BufferedReader bufferedReader;
  private final Map<String, String> summaryMap;
  private final Ensembl ensembl;
  private final GeneWriter writer;

  /**
   * Method responsible for constructing the skeleton of the gene model. It streams the gtf file, constructing the
   * elements of the gene model, and joining in information from the maps provided to the constructor of the class.
   * Write Gene to mongo as soon as it is constructed and moves onto next one.
   */
  @SneakyThrows
  public void consumeGenes() {
    log.info("CONSUMING GENES");

    ObjectNode geneNode = null;
    ObjectNode curTranscript = null;
    ArrayNode transcripts = DEFAULT.createArrayNode();
    ArrayNode exons = DEFAULT.createArrayNode();

    for (String s = bufferedReader.readLine(); null != s; s = bufferedReader.readLine()) {
      s = s.trim();
      if (s.length() > 0 && s.charAt(0) != '#') {
        val entry = parseLine(s);
        if (entry != null) {

          if (("gene".equals(asText(entry, "type")))) {

            // Finish constructing the current working gene and write to mongo
            if (geneNode != null) {
              // Add the final transcript
              curTranscript.put("exons", exons);
              val trans = postProcessTranscript(curTranscript, asText(geneNode, "strand"),
                  asText(geneNode, "canonical_transcript_id"));
              transcripts.add(trans);
              geneNode.put("transcripts", transcripts);
              geneNode.put("external_db_ids", externalIds().get(asText(geneNode, "_gene_id")));
              writeGene(geneNode);
              curTranscript = null;
              transcripts = DEFAULT.createArrayNode();
              exons = DEFAULT.createArrayNode();
            }

            // Move on to next gene
            geneNode = constructGeneNode(entry);

            // Extra fields for gene object
            geneNode.put("name", getName(asText(geneNode, "symbol")));
            geneNode.put("description", summaryMap.getOrDefault(asText(geneNode, "_gene_id"), ""));
            geneNode.put("synonyms", getDescription(asText(geneNode, "_gene_id")));
            geneNode.put("canonical_transcript_id", canonicalMap().get(asText(geneNode, "_gene_id")));

          } else if ("transcript".equals(asText(entry, "type"))) {
            if (curTranscript != null) {
              curTranscript.put("exons", exons);
              try {
                val trans = postProcessTranscript(curTranscript, geneNode.get("strand").asText(),
                    geneNode.get("canonical_transcript_id").asText());
                transcripts.add(trans);
              } catch (Exception e) {
                log.error(curTranscript.toString());
                throw e;
              }
              exons = DEFAULT.createArrayNode();
            }
            curTranscript = constructTranscriptNode(entry);
          } else if ("exon".equals(asText(entry, "type"))) {
            exons.add(constructExonNode(entry));
          } else if ("CDS".equals(asText(entry, "type"))) {
            ((ObjectNode) exons.get(exons.size() - 1)).put("cds", entry);
          } else if ("start_codon".equals(asText(entry, "type"))) {
            curTranscript.put("start_exon", exons.size() - 1);
          } else if ("stop_codon".equals(asText(entry, "type"))) {
            curTranscript.put("end_exon", exons.size() - 1);
          }

        }
      }
    }
  }

  public void writeGene(@NonNull ObjectNode value) {
    writer.writeFiles(value);
  }

  /**
   * Responsible for parsing the lines of the gtf file and producing generic ObjectNodes
   * @param String representing the current line in the gtf file
   * @return ObjectNode representation of the gtf row.
   */
  private ObjectNode parseLine(@NonNull String s) {
    String[] line = TAB.splitToList(s).stream().toArray(String[]::new);
    val seqname = line[0].trim();
    val source = line[1].trim();
    val type = line[2].trim();
    String locStart = line[3].trim();
    String locEnd = line[4].trim();

    char strand = line[6].trim().charAt(0);
    int locationStart = Integer.parseInt(locStart);
    int locationEnd = Integer.parseInt(locEnd);
    if (locationStart > locationEnd) {
      int location = locationStart;
      locationStart = locationEnd;
      locationEnd = location;
    }

    val negative = locationStart <= 0 && locationEnd <= 0;
    assert strand == '-' == negative;

    val attributes = line[8];
    val attributeMap = parseAttributes(attributes);

    ObjectNode feature = DEFAULT.createObjectNode();

    int strandNumber = 0;
    if (strand == '+') {
      strandNumber = 1;
    } else if (strand == '-') {
      strandNumber = -1;
    }

    feature.put("seqname", seqname);
    feature.put("source", source);
    feature.put("type", type);
    feature.put("locationStart", locationStart);
    feature.put("locationEnd", locationEnd);
    feature.put("strand", strandNumber);

    for (val kv : attributeMap.entrySet()) {
      feature.put(kv.getKey(), kv.getValue());
    }

    return feature;
  }

  private String getName(String symbol) {
    return nameMap().getOrDefault(symbol, "");
  }

  private ArrayNode getDescription(String id) {
    return synMap().getOrDefault(id, DEFAULT.createArrayNode());
  }

  private static Map<String, String> parseAttributes(String attributes) {
    val attributeMap = new HashMap<String, String>();
    val tokens = SEMICOLON.split(attributes);
    for (val token : tokens) {
      String[] kv = UNKNOWN_WHITESPACE.split(QUOTES.matcher(token.trim()).replaceAll(""));
      if (kv.length == 2) {
        attributeMap.put(kv[0], kv[1]);
      }
    }
    return attributeMap;
  }

  private static ObjectNode constructGeneNode(ObjectNode data) {
    val gene = DEFAULT.createObjectNode();
    gene.put("_gene_id", asText(data, "gene_id"));
    gene.put("symbol", asText(data, "gene_name"));
    gene.put("biotype", asText(data, "gene_biotype"));
    gene.put("chromosome", asText(data, "seqname"));
    gene.put("strand", asText(data, "strand"));
    gene.put("start", asInt(data, "locationStart"));
    gene.put("end", asInt(data, "locationEnd"));
    return gene;
  }

  /**
   * This method performs the calculations on transcript and exons to correctly mark coding regions. Heavily based on:
   * https://github.com/icgc-dcc/dcc-heliotrope/blob/working/src/main/scripts/Heliotrope/Update/Ensembl.pm#L885
   * @param transcript ObjectNode representation of transcript with exons if any are present
   * @param strand marks if + or - strand
   * @return Processed Transcript ObjectNode
   */
  private ObjectNode postProcessTranscript(ObjectNode transcript, String strand, String canonical) {
    if (transcript.get("id").asText().equals(canonical)) {
      transcript.put("is_canonical", true);
    } else {
      transcript.put("is_canonical", false);
    }

    val exons = exonDefaults((ArrayNode) transcript.get("exons"));
    transcript.put("number_of_exons", exons.size());

    JsonNode startExon = transcript.path("start_exon");
    JsonNode endExon = transcript.path("end_exon");

    if (exons.size() > 0) {
      transcript.put("length", asText(exons.get(exons.size() - 1), "cdna_end"));
    }

    // In case there is no start codon
    if (startExon.isMissingNode()) {
      for (int i = 0; i < exons.size(); i++) {
        if (!exons.get(i).path("cds").isMissingNode()) {
          transcript.put("start_exon", i);
          startExon = transcript.path("start_exon");
          break;
        }
      }
    }

    // In case there is no end codon
    if (endExon.isMissingNode()) {
      for (int i = exons.size() - 1; i > 0; i--) {
        if (!exons.get(i).path("cds").isMissingNode()) {
          transcript.put("end_exon", i);
          endExon = transcript.path("end_exon");
          break;
        }
      }
    }

    // If there are no start or end exons, we know this transcript is non-coding.
    if (startExon.isMissingNode() && endExon.isMissingNode()) {
      transcript.putNull("start_exon");
      transcript.putNull("end_exon");
      return transcript;
    } else if (startExon.isMissingNode()) {
      transcript.putNull("start_exon");
      return transcript;
    } else if (endExon.isMissingNode()) {
      transcript.putNull("end_exon");
      return transcript;
    }

    double cdsLength = 0.0;
    for (int i = startExon.asInt(); i <= endExon.asInt(); i++) {
      val exon = (ObjectNode) exons.get(i);

      // Initiate the values by assuming the whole exon is coding first
      exon.put("genomic_coding_start", asInt(exon, "start"));
      exon.put("cdna_coding_start", asInt(exon, "cdna_start"));
      exon.put("genomic_coding_end", asInt(exon, "end"));
      exon.put("cdna_coding_end", asInt(exon, "cdna_end"));

      val cds = exon.path("cds");
      if (!cds.isMissingNode()) {

        // Translation id is the protein id of the coding sequence.
        if (transcript.get("translation_id").isNull()) {
          transcript.put("translation_id", asText(cds, "protein_id"));
        }

        cdsLength += asInt(cds, "locationEnd") - asInt(cds, "locationStart") + 1;
      }

      // Start Exon.
      if (i == startExon.asInt()) {
        computeStartRegion(transcript, exon, strand);
      }

      // End Exon. Note: This can be the same exon as the start exon.
      if (i == endExon.asInt()) {
        computeEndRegion(transcript, exon, i, strand);
      }
    }

    transcript.put("start_exon", startExon.asInt());
    transcript.put("end_exon", endExon.asInt());

    val aminoAcidLength = Math.round(cdsLength / 3);
    transcript.put("length_cds", (int) cdsLength);
    transcript.put("length_amino_acid", aminoAcidLength);

    return attachDomains(transcript, pFeatures());
  }

  /*
   * Ensembl Helpers
   */
  private Map<String, String> nameMap() {
    return this.ensembl.getNameMap();
  }

  private Map<String, String> canonicalMap() {
    return this.ensembl.getCanonicalMap();
  }

  private Map<String, ObjectNode> externalIds() {
    return this.ensembl.getExternalIds();
  }

  private Map<String, ArrayNode> synMap() {
    return this.ensembl.getSynonymMap();
  }

  private Map<String, List<ProteinFeature>> pFeatures() {
    return this.ensembl.getPFeatures();
  }

}
