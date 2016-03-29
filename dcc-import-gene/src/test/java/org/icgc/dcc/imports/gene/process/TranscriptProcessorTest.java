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

  private static final String KRAS = "src/test/resources/fixtures/KRAS.gtf.gz";

  private ObjectNode kras;

  @Before
  public void setupStream() {
    val gtfReader = new GeneGtfReader(Paths.get(KRAS).toUri().toString());

    val gtfStream = gtfReader.read();
    val genes = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(new GeneIterator(gtfStream.iterator()), NONNULL | DISTINCT), false);

    // Manually set canonical since not testing Ensembl joining
    kras = genes
        .map(g -> g.put("canonical_transcript_id", "ENST00000256078"))
        .map(TranscriptProcessor::process)
        .findFirst()
        .get();
  }

  @Test
  public void testPipelineNegativeStrand() {
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
  public void testCanonicalCorrectness() {
    val transcript = kras.withArray("transcripts").get(2);

    assertThat(asText(transcript, "id")).isEqualTo("ENST00000256078");
    assertThat(asText(transcript, "id")).isEqualTo(asText(kras, "canonical_transcript_id"));
    assertThat(asText(transcript, "name")).isEqualTo("KRAS-004");
    assertThat(transcript.get("is_canonical").asBoolean()).isEqualTo(true);
  }

  @Test
  public void testStartExon() {
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
  public void testEndExon() {
    val startExon = kras.withArray("transcripts").get(3).withArray("exons").get(2);

    assertThat(asInt(startExon, "start")).isEqualTo(25386753);
    assertThat(asInt(startExon, "end")).isEqualTo(25388160);
    assertThat(asInt(startExon, "cdna_start")).isEqualTo(289);
    assertThat(asInt(startExon, "cdna_end")).isEqualTo(1696);
    assertThat(asInt(startExon, "genomic_coding_start")).isEqualTo(25388143);
    assertThat(asInt(startExon, "genomic_coding_end")).isEqualTo(25388160);
    assertThat(asInt(startExon, "cdna_coding_start")).isEqualTo(289);
    assertThat(asInt(startExon, "cdna_coding_end")).isEqualTo(306);
  }

}
