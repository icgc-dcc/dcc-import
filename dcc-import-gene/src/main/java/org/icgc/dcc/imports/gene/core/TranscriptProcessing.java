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

import lombok.val;

/**
 * Collection of stateless helpers used for processing transcripts and exons
 */
public final class TranscriptProcessing {

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

  public static ObjectNode constructExonNode(ObjectNode data) {
    val exon = DEFAULT.createObjectNode();
    exon.put("start", asInt(data, "locationStart"));
    exon.put("end", asInt(data, "locationEnd"));
    return exon;
  }

  public static ArrayNode exonDefaults(ArrayNode exons) {
    int preExonCdnaEnd = 0;
    for (JsonNode exon : exons) {
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

  public static void computeStartRegion(ObjectNode transcript, ObjectNode exon, String strand) {
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
  }

  public static void computeEndRegion(ObjectNode transcript, ObjectNode exon, int i, String strand) {
    val cds = exon.path("cds");
    val exons = (ArrayNode) transcript.get("exons");
    val startExon = transcript.get("start_exon");

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

    transcript.put("seq_exon_start", seqExonStart(exons.get(startExon.asInt()), strand));

    // If stop codon is first 3 base pairs of end exon, there will be no coding sequence region for that exon.
    if (cds.isMissingNode()) {
      transcript.put("cdna_coding_end", asInt(exons.get(i - 1), "cdna_coding_end"));
      transcript.put("seq_exon_end", seqExonEnd(exons.get(i - 1), strand));
    } else {
      transcript.put("cdna_coding_end", asInt(exon, "cdna_coding_end"));
      transcript.put("seq_exon_end", seqExonEnd(exon, strand));
    }
  }

  public static int seqExonStart(JsonNode exon, String strand) {
    int seqExonStart = asInt(exon, "cdna_coding_start") - asInt(exon, "cdna_start");
    if ("-1".equals(strand)) {
      seqExonStart++;
    }
    return seqExonStart;
  }

  public static int seqExonEnd(JsonNode exon, String strand) {
    int seqExonEnd = asInt(exon, "cdna_coding_end") - asInt(exon, "cdna_coding_start");
    if ("1".equals(strand)) {
      seqExonEnd++;
    }
    return seqExonEnd;
  }

  public static ObjectNode attachDomains(ObjectNode transcript, Map<String, List<ProteinFeature>> pFeatures) {
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
