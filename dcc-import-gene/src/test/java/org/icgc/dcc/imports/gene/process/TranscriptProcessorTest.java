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
package org.icgc.dcc.imports.gene.process;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.NONNULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asInt;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asText;

import java.nio.file.Paths;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.icgc.dcc.imports.gene.core.GeneIterator;
import org.icgc.dcc.imports.gene.processor.TranscriptProcessor;
import org.icgc.dcc.imports.gene.reader.GeneGtfReader;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.val;

public class TranscriptProcessorTest {

  private static final String FIXTURES = "src/test/resources/fixtures/";
  private static final String KRAS = FIXTURES + "KRAS.gtf.gz";
  private static final String EXOSC = FIXTURES + "EXOSC10-008.gtf.gz";
  private static final String KLHL = FIXTURES + "KLHL21-003.gtf.gz";

  private ObjectNode kras;
  private ObjectNode exosc;
  private ObjectNode klhl;

  @Before
  public void setupStream() {
    kras = loadTestData(KRAS, "ENST00000256078");
    exosc = loadTestData(EXOSC, "ENST00000376936");
    klhl = loadTestData(KLHL, "ENST00000328089");
  }

  @Test
  public void kras_testPipelineNegativeStrand() {
    // Sanity Check
    assertThat(asText(kras, "symbol")).isEqualTo("KRAS");
    assertThat(asText(kras, "biotype")).isEqualTo("protein_coding");

    val transcripts = kras.withArray("transcripts");
    assertThat(transcripts.size()).isEqualTo(4);

    val transcript = transcripts.get(0);
    assertThat(asText(transcript, "id")).isEqualTo("ENST00000311936");
    assertThat(asText(transcript, "name")).isEqualTo("KRAS-001");
    assertThat(asText(transcript, "biotype")).isEqualTo("protein_coding");
    assertThat(asInt(transcript, "start")).isEqualTo(25357723);
    assertThat(asInt(transcript, "end")).isEqualTo(25403865);
    assertThat(asInt(transcript, "coding_region_start")).isEqualTo(25362732);
    assertThat(asInt(transcript, "coding_region_end")).isEqualTo(25398318);
    assertThat(asInt(transcript, "cdna_coding_start")).isEqualTo(193);
    assertThat(asInt(transcript, "cdna_coding_end")).isEqualTo(756);
    assertThat(asInt(transcript, "seq_exon_start")).isEqualTo(12);
    assertThat(asInt(transcript, "seq_exon_end")).isEqualTo(114);
    assertThat(asInt(transcript, "length")).isEqualTo(5765);
    assertThat(asInt(transcript, "length_cds")).isEqualTo(564);
    assertThat(asInt(transcript, "length_amino_acid")).isEqualTo(188);
    assertThat(asInt(transcript, "start_exon")).isEqualTo(1);
    assertThat(asInt(transcript, "end_exon")).isEqualTo(4);
    assertThat(asInt(transcript, "number_of_exons")).isEqualTo(5);
  }

  @Test
  public void kras_testCanonicalCorrectness() {
    val transcript = kras.withArray("transcripts").get(2);

    assertThat(asText(transcript, "id")).isEqualTo("ENST00000256078");
    assertThat(asText(transcript, "id")).isEqualTo(asText(kras, "canonical_transcript_id"));
    assertThat(asText(transcript, "name")).isEqualTo("KRAS-004");
    assertThat(transcript.get("is_canonical").asBoolean()).isEqualTo(true);
  }

  @Test
  public void kras_testStartExon() {
    val startExon = kras.withArray("transcripts").get(3).withArray("exons").get(1);

    assertThat(asInt(startExon, "start")).isEqualTo(25398208);
    assertThat(asInt(startExon, "end")).isEqualTo(25398329);
    assertThat(asInt(startExon, "cdna_start")).isEqualTo(167);
    assertThat(asInt(startExon, "cdna_end")).isEqualTo(288);
    assertThat(asInt(startExon, "genomic_coding_start")).isEqualTo(25398208);
    assertThat(asInt(startExon, "genomic_coding_end")).isEqualTo(25398318);
    assertThat(asInt(startExon, "cdna_coding_start")).isEqualTo(178);
    assertThat(asInt(startExon, "cdna_coding_end")).isEqualTo(288);
  }

  @Test
  public void kras_testEndExon() {
    val endExon = kras.withArray("transcripts").get(3).withArray("exons").get(2);

    assertThat(asInt(endExon, "start")).isEqualTo(25386753);
    assertThat(asInt(endExon, "end")).isEqualTo(25388160);
    assertThat(asInt(endExon, "cdna_start")).isEqualTo(289);
    assertThat(asInt(endExon, "cdna_end")).isEqualTo(1696);
    assertThat(asInt(endExon, "genomic_coding_start")).isEqualTo(25388143);
    assertThat(asInt(endExon, "genomic_coding_end")).isEqualTo(25388160);
    assertThat(asInt(endExon, "cdna_coding_start")).isEqualTo(289);
    assertThat(asInt(endExon, "cdna_coding_end")).isEqualTo(306);
  }

  @Test
  public void exosc_testPipeline() {
    assertThat(asText(exosc, "symbol")).isEqualTo("EXOSC10");
    assertThat(asText(exosc, "biotype")).isEqualTo("protein_coding");

    val transcripts = exosc.withArray("transcripts");
    assertThat(transcripts.size()).isEqualTo(1);

    val transcript = transcripts.get(0);
    assertThat(asText(transcript, "id")).isEqualTo("ENST00000460196");
    assertThat(asText(transcript, "name")).isEqualTo("EXOSC10-008");
    assertThat(asText(transcript, "biotype")).isEqualTo("nonsense_mediated_decay");
    assertThat(asInt(transcript, "start")).isEqualTo(11151560);
    assertThat(asInt(transcript, "end")).isEqualTo(11159911);
    assertThat(asInt(transcript, "coding_region_start")).isEqualTo(11155815);
    assertThat(asInt(transcript, "coding_region_end")).isEqualTo(11159888);
    assertThat(asInt(transcript, "cdna_coding_start")).isEqualTo(24);
    assertThat(asInt(transcript, "cdna_coding_end")).isEqualTo(395);
    assertThat(asInt(transcript, "seq_exon_start")).isEqualTo(24);
    assertThat(asInt(transcript, "seq_exon_end")).isEqualTo(124);
    assertThat(asInt(transcript, "length")).isEqualTo(622);
    assertThat(asInt(transcript, "length_cds")).isEqualTo(372);
    assertThat(asInt(transcript, "length_amino_acid")).isEqualTo(124);
    assertThat(asInt(transcript, "start_exon")).isEqualTo(0);
    assertThat(asInt(transcript, "end_exon")).isEqualTo(3);
    assertThat(asInt(transcript, "number_of_exons")).isEqualTo(5);
  }

  @Test
  public void exosc_testCanonicalCorrectness() {
    val transcript = exosc.withArray("transcripts").get(0);
    assertThat(transcript.get("is_canonical").asBoolean()).isEqualTo(false);
  }

  @Test
  public void exosc_testStartExon() {
    val startExon = exosc.withArray("transcripts").get(0).withArray("exons").get(0);

    assertThat(asInt(startExon, "start")).isEqualTo(11159778);
    assertThat(asInt(startExon, "end")).isEqualTo(11159911);
    assertThat(asInt(startExon, "cdna_start")).isEqualTo(1);
    assertThat(asInt(startExon, "cdna_end")).isEqualTo(134);
    assertThat(asInt(startExon, "genomic_coding_start")).isEqualTo(11159778);
    assertThat(asInt(startExon, "genomic_coding_end")).isEqualTo(11159888);
    assertThat(asInt(startExon, "cdna_coding_start")).isEqualTo(24);
    assertThat(asInt(startExon, "cdna_coding_end")).isEqualTo(134);
  }

  @Test
  public void exosc_testEndExon() {
    val endExon = exosc.withArray("transcripts").get(0).withArray("exons").get(3);

    assertThat(asInt(endExon, "start")).isEqualTo(11155473);
    assertThat(asInt(endExon, "end")).isEqualTo(11155604);
    assertThat(asInt(endExon, "cdna_start")).isEqualTo(396);
    assertThat(asInt(endExon, "cdna_end")).isEqualTo(527);
    assertThat(asInt(endExon, "genomic_coding_start")).isEqualTo(0);
    assertThat(asInt(endExon, "genomic_coding_end")).isEqualTo(0);
    assertThat(asInt(endExon, "cdna_coding_start")).isEqualTo(0);
    assertThat(asInt(endExon, "cdna_coding_end")).isEqualTo(0);
  }

  @Test
  public void klhl_testPipeline() {
    assertThat(asText(klhl, "symbol")).isEqualTo("KLHL21");
    assertThat(asText(klhl, "biotype")).isEqualTo("protein_coding");

    val transcripts = klhl.withArray("transcripts");
    assertThat(transcripts.size()).isEqualTo(1);

    val transcript = transcripts.get(0);
    assertThat(asText(transcript, "id")).isEqualTo("ENST00000463043");
    assertThat(asText(transcript, "name")).isEqualTo("KLHL21-003");
    assertThat(asText(transcript, "biotype")).isEqualTo("protein_coding");
    assertThat(asInt(transcript, "start")).isEqualTo(6654141);
    assertThat(asInt(transcript, "end")).isEqualTo(6674667);
    assertThat(asInt(transcript, "coding_region_start")).isEqualTo(6655545);
    assertThat(asInt(transcript, "coding_region_end")).isEqualTo(6659432);
    assertThat(asInt(transcript, "cdna_coding_start")).isEqualTo(239);
    assertThat(asInt(transcript, "cdna_coding_end")).isEqualTo(637);
    assertThat(asInt(transcript, "seq_exon_start")).isEqualTo(81);
    assertThat(asInt(transcript, "seq_exon_end")).isEqualTo(73);
    assertThat(asInt(transcript, "length")).isEqualTo(667);
    assertThat(asInt(transcript, "length_cds")).isEqualTo(399);
    assertThat(asInt(transcript, "length_amino_acid")).isEqualTo(133);
    assertThat(asInt(transcript, "start_exon")).isEqualTo(1);
    assertThat(asInt(transcript, "end_exon")).isEqualTo(3);
    assertThat(asInt(transcript, "number_of_exons")).isEqualTo(4);
  }

  @Test
  public void klhl_testCanonicalCorrectness() {
    val transcript = klhl.withArray("transcripts").get(0);
    assertThat(transcript.get("is_canonical").asBoolean()).isEqualTo(false);
  }

  @Test
  public void klhl_testStartExon() {
    val startExon = klhl.withArray("transcripts").get(0).withArray("exons").get(1);

    assertThat(asInt(startExon, "start")).isEqualTo(6659107);
    assertThat(asInt(startExon, "end")).isEqualTo(6659512);
    assertThat(asInt(startExon, "cdna_start")).isEqualTo(159);
    assertThat(asInt(startExon, "cdna_end")).isEqualTo(564);
    assertThat(asInt(startExon, "genomic_coding_start")).isEqualTo(6659107);
    assertThat(asInt(startExon, "genomic_coding_end")).isEqualTo(6659432);
    assertThat(asInt(startExon, "cdna_coding_start")).isEqualTo(239);
    assertThat(asInt(startExon, "cdna_coding_end")).isEqualTo(564);
  }

  @Test
  public void klhl_testEndExon() {
    val endExon = klhl.withArray("transcripts").get(0).withArray("exons").get(3);

    assertThat(asInt(endExon, "start")).isEqualTo(6654141);
    assertThat(asInt(endExon, "end")).isEqualTo(6654170);
    assertThat(asInt(endExon, "cdna_start")).isEqualTo(638);
    assertThat(asInt(endExon, "cdna_end")).isEqualTo(667);
    assertThat(asInt(endExon, "genomic_coding_start")).isEqualTo(0);
    assertThat(asInt(endExon, "genomic_coding_end")).isEqualTo(0);
    assertThat(asInt(endExon, "cdna_coding_start")).isEqualTo(0);
    assertThat(asInt(endExon, "cdna_coding_end")).isEqualTo(0);
  }

  private ObjectNode loadTestData(String path, String canonical) {
    val gtfReader = new GeneGtfReader(Paths.get(path).toUri().toString());
    val gene = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(new GeneIterator(gtfReader.read().iterator()), NONNULL | DISTINCT), false)
        .map(g -> g.put("canonical_transcript_id", canonical))
        .map(TranscriptProcessor::process)
        .findFirst()
        .get();

    return gene;
  }

}
