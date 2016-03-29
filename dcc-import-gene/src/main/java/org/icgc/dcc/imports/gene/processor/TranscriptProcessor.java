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
package org.icgc.dcc.imports.gene.processor;

import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asInt;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asText;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.computeEndRegion;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.computeStartRegion;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.exonDefaults;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.val;

public class TranscriptProcessor {

  public static ObjectNode process(ObjectNode gene) {
    return processTranscripts(gene);
  }

  private static ObjectNode processTranscripts(ObjectNode gene) {
    val transcripts = gene.withArray("transcripts");
    transcripts.forEach(transcript -> {
      postProcessTranscript((ObjectNode) transcript, asInt(gene, "strand"), asText(gene, "canonical_transcript_id"));
    });

    return gene;
  }

  /**
   * This method performs the calculations on transcript and exons to correctly mark coding regions. Heavily based on:
   * https://github.com/icgc-dcc/dcc-heliotrope/blob/working/src/main/scripts/Heliotrope/Update/Ensembl.pm#L885
   * 
   * @param transcript ObjectNode representation of transcript with exons if any are present
   * @param strand marks if + or - strand
   * @return Processed Transcript ObjectNode
   */
  private static ObjectNode postProcessTranscript(ObjectNode transcript, Integer strand, String canonical) {
    transcript.put("is_canonical", isCanonical(transcript, canonical));

    val exons = exonDefaults((ArrayNode) transcript.get("exons"));
    transcript.put("number_of_exons", exons.size());

    JsonNode startExon = transcript.path("start_exon");
    JsonNode endExon = transcript.path("end_exon");

    if (exons.size() > 0) {
      transcript.put("length", asInt(exons.get(exons.size() - 1), "cdna_end"));
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
      for (int i = exons.size() - 1; i >= 0; i--) {
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

    // Amino Acids are specified by codons which are made up of 3 bases.
    val aminoAcidLength = Math.round(cdsLength / 3);
    transcript.put("length_cds", (int) cdsLength);
    transcript.put("length_amino_acid", aminoAcidLength);

    exons.forEach(e -> cleanExon((ObjectNode) e));

    return transcript;
  }

  private static boolean isCanonical(ObjectNode transcript, String canonical) {
    return asText(transcript, "id").equals(canonical);
  }

  private static void cleanExon(ObjectNode exon) {
    exon.remove("cds");
  }

}
