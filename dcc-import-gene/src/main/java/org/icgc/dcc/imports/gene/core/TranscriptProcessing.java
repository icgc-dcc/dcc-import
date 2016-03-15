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

import static org.icgc.dcc.common.json.Jackson.DEFAULT;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.imports.gene.model.ProteinFeature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.NonNull;
import lombok.val;

/**
 * Collection of stateless helpers used for processing transcripts and exons
 */
public final class TranscriptProcessing {

  /**
   * Helper for constructing the Transcript JSON object
   * @param data ObjectNode holding raw json data
   * @return ObjectNode representing a transcript with default values.
   */
  public static ObjectNode constructTranscriptNode(ObjectNode data) {
    val transcript = DEFAULT.createObjectNode();
    transcript.put("id", asText(data, "transcript_id"));
    transcript.put("name", asText(data, "transcript_name"));
    transcript.put("biotype", asText(data, "transcript_biotype"));
    transcript.put("start", asInt(data, "locationStart"));
    transcript.put("end", asInt(data, "locationEnd"));
    transcript.putNull("translation_id");
    transcript.put("coding_region_start", 0);
    transcript.put("coding_region_end", 0);
    transcript.put("cdna_coding_start", 0);
    transcript.put("cdna_coding_end", 0);
    transcript.putNull("seq_exon_start");
    transcript.putNull("seq_exon_end");
    transcript.putNull("length");
    transcript.putNull("length_amino_acid");
    transcript.putNull("length_cds");
    transcript.put("domains", DEFAULT.createArrayNode());
    return transcript;
  }

  /**
   * Helper for constructing the Exon JSON object
   * @param data ObjectNode holding raw json data
   * @return ObjectNode represnting an exon with default values.
   */
  public static ObjectNode constructExonNode(ObjectNode data) {
    val exon = DEFAULT.createObjectNode();
    exon.put("start", asInt(data, "locationStart"));
    exon.put("end", asInt(data, "locationEnd"));
    return exon;
  }

  /**
   * Helper for setting the default calculated values for coding regions in an exon.
   * @param exons Array of exons.
   * @return The Array of exons with the initial calculated values set.
   */
  public static ArrayNode exonDefaults(ArrayNode exons) {
    int preExonCdnaEnd = 0;
    for (val exon : exons) {
      ObjectNode exonNode = (ObjectNode) exon;

      val exonLength = asInt(exon, "end") - asInt(exon, "start");
      exonNode.put("cdna_start", preExonCdnaEnd + 1);
      exonNode.put("cdna_end", asInt(exonNode, "cdna_start") + exonLength);
      preExonCdnaEnd = asInt(exonNode, "cdna_end");

      exonNode.put("genomic_coding_start", 0);
      exonNode.put("genomic_coding_end", 0);
      exonNode.put("cdna_coding_start", 0);
      exonNode.put("cdna_coding_end", 0);
    }
    return exons;
  }

  /**
   * Helper for computing the values of the beginning of the coding region of a transcript and start exon, both with the
   * coordinate system of the DNA and the coordinate system of the transcript sequence (cDNA).
   * @param transcript ObjectNode representation of a transcript.
   * @param exon ObjectNode representation of an exon.
   * @param strand Flag which determines if we are working on a positive or negative strand.
   */
  public static void computeStartRegion(@NonNull ObjectNode transcript, @NonNull ObjectNode exon,
      @NonNull String strand) {
    val cds = exon.get("cds");
    val cdsStart = asInt(cds, "locationStart");

    if (strand.equals("-1")) {
      val end = asInt(cds, "locationEnd");
      transcript.put("coding_region_end", end);
      exon.put("genomic_coding_end", end);
      exon.put("cdna_coding_start", asInt(exon, "end") - end + asInt(exon, "cdna_start"));
    } else {
      transcript.put("coding_region_start", cdsStart);
      exon.put("genomic_coding_start", cdsStart);
      exon.put("cdna_coding_start", asInt(exon, "cdna_start") + (cdsStart - asInt(exon, "start") + 1));
    }
    transcript.put("cdna_coding_start", asInt(exon, "cdna_coding_start"));
    transcript.put("seq_exon_start", seqExonStart(exon, strand));
  }

  /**
   * Helper for computing the values of the end of the coding region of a transcript and end exon, both with the
   * coordinate system of the DNA and the coordinate system of the transcript sequence (cDNA). This helper covers the
   * edge case where the first three bases of an exon are the stop codon and thus the end exon does not contain a coding
   * region itself.
   * @param transcript ObjectNode representation of a transcript.
   * @param exon ObjectNode representation of a the end exon.
   * @param i The position of the end exon within the exons of the transcript.
   * @param strand Flag which determines if we are working on a positive or negative strand.
   */
  public static void computeEndRegion(@NonNull ObjectNode transcript, @NonNull ObjectNode exon, int i,
      @NonNull String strand) {
    val cds = exon.path("cds");
    val exons = (ArrayNode) transcript.get("exons");

    if (strand.equals("-1")) {
      if (cds.isMissingNode()) {
        val start = asInt(exons.get(i - 1).path("cds"), "locationStart");
        transcript.put("coding_region_start", start);
      } else {
        val start = asInt(cds, "locationStart");
        transcript.put("coding_region_start", start);
        exon.put("genomic_coding_start", start);
        exon.put("cdna_coding_end", asInt(exon, "cdna_coding_start") + asInt(exon, "genomic_coding_end")
            - asInt(exon, "genomic_coding_start") + 1);
      }
    } else {
      if (cds.isMissingNode()) {
        val end = asInt(exons.get(i - 1).path("cds"), "locationEnd");
        transcript.put("coding_region_end", end);
      } else {
        val end = asInt(cds, "locationEnd");
        transcript.put("coding_region_end", end);
        exon.put("genomic_coding_end", end);
        exon.put("cdna_coding_start", asInt(exon, "cdna_start"));
        exon.put("cdna_coding_end", asInt(exon, "cdna_end") - (asInt(exon, "end") - end));
      }
    }

    // If stop codon is first 3 base pairs of end exon, there will be no coding sequence region for that exon.
    if (cds.isMissingNode()) {
      transcript.put("cdna_coding_end", asInt(exons.get(i - 1), "cdna_coding_end"));
      transcript.put("seq_exon_end", seqExonEnd(exons.get(i - 1), strand));
    } else {
      transcript.put("cdna_coding_end", asInt(exon, "cdna_coding_end"));
      transcript.put("seq_exon_end", seqExonEnd(exon, strand));
    }
  }

  /**
   * Calculates how far into the start exon the coding sequence starts.
   * @param exon JsonNode representation of start exon.
   * @param strand Flag which determines if we are working on a positive or negative strand.
   * @return number of bases as int into the start exon.
   */
  public static int seqExonStart(@NonNull JsonNode exon, @NonNull String strand) {
    int seqExonStart = asInt(exon, "cdna_coding_start") - asInt(exon, "cdna_start");
    if ("-1".equals(strand)) {
      seqExonStart++;
    }
    return seqExonStart;
  }

  /**
   * Calculates how far into the end exon the coding sequence ends.
   * @param exon JsonNode representation of end exon.
   * @param strand Flag which determines if we are working on a positive or negative strand.
   * @return number of bases as int into the end exon.
   */
  public static int seqExonEnd(@NonNull JsonNode exon, @NonNull String strand) {
    int seqExonEnd = asInt(exon, "cdna_coding_end") - asInt(exon, "cdna_coding_start");
    if ("1".equals(strand)) {
      seqExonEnd++;
    }
    return seqExonEnd;
  }

  /**
   * Helper for joining the protein features to a transcript as an array of Domain ObjectNodes
   * @param transcript ObjectNode transcript.
   * @param pFeatures Map of transcript ids mapping to a List of protein feature objects
   * @return ObjectNode representation of the transcript with the Domains joined.
   */
  public static ObjectNode attachDomains(@NonNull ObjectNode transcript,
      @NonNull Map<String, List<ProteinFeature>> pFeatures) {
    val pfs = pFeatures.get(asText(transcript, "id"));
    val domains = DEFAULT.createArrayNode();

    if (pfs != null) {
      for (val p : pfs) {
        val domain = DEFAULT.createObjectNode();
        domain.put("interpro_id", p.getInterproId());
        domain.put("hit_name", p.getHitName());
        domain.put("gff_source", p.getGffSource());
        domain.put("description", p.getDescription());
        domain.put("start", p.getStart());
        domain.put("end", p.getEnd());
        domains.add(domain);
      }
    }
    transcript.put("domains", domains);
    return transcript;
  }

  public static int asInt(JsonNode node, String fieldName) {
    return node.get(fieldName).asInt();
  }

  public static String asText(JsonNode node, String fieldName) {
    return node.get(fieldName).asText();
  }

}