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

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.json.Jackson.DEFAULT;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asInt;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asText;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.seqExonEnd;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.seqExonStart;

import java.nio.file.Paths;

import org.icgc.dcc.imports.gene.reader.GeneGtfReader;
import org.junit.Test;

import lombok.SneakyThrows;
import lombok.val;

public class TranscriptProcessingTest {

  private static final String TRANSCRIPT_1 = "src/test/resources/fixtures/transcript1.gtf.gz";
  private static final String EXON_1 = "src/test/resources/fixtures/exon1.gtf.gz";

  @Test
  @SneakyThrows
  public void testTranscriptConstruction() {
    val geneReader = new GeneGtfReader(Paths.get(TRANSCRIPT_1).toUri().toString());
    val gtfStream = geneReader.read();

    val transcript = gtfStream
        .map(TranscriptProcessing::constructTranscriptNode)
        .findFirst()
        .get();

    assertThat(transcript).isNotNull();
    assertThat(asText(transcript, "id")).isEqualTo("ENST00000473358");
    assertThat(asText(transcript, "name")).isEqualTo("MIR1302-10-001");
    assertThat(asInt(transcript, "start")).isEqualTo(29554);
    assertThat(asInt(transcript, "end")).isEqualTo(31097);
    assertThat(asText(transcript, "biotype")).isEqualTo("lincRNA");

    assertThat(asInt(transcript, "coding_region_start")).isEqualTo(0);
    assertThat(asInt(transcript, "coding_region_end")).isEqualTo(0);
    assertThat(asInt(transcript, "cdna_coding_start")).isEqualTo(0);
    assertThat(asInt(transcript, "cdna_coding_end")).isEqualTo(0);
  }

  @Test
  public void testExonConstruction() {
    val geneReader = new GeneGtfReader(Paths.get(EXON_1).toUri().toString());
    val gtfStream = geneReader.read();

    val exon = gtfStream
        .map(TranscriptProcessing::constructExonNode)
        .findFirst()
        .get();

    assertThat(exon).isNotNull();
    assertThat(asInt(exon, "start")).isEqualTo(30267);
    assertThat(asInt(exon, "end")).isEqualTo(30667);
    assertThat(asText(exon, "id")).isEqualTo("ENSE00001841699");
  }

  @Test
  public void testSeqExonStart() {
    val exon = DEFAULT.createObjectNode();
    exon.put("cdna_coding_start", 5);
    exon.put("cdna_start", 2);

    val seqExonStart = seqExonStart(exon);
    assertThat(seqExonStart).isEqualTo(4);
  }

  @Test
  public void testSeqExonEnd() {
    val exon = DEFAULT.createObjectNode();
    exon.put("cdna_coding_end", 23);
    exon.put("cdna_coding_start", 20);

    val seqExonEnd = seqExonEnd(exon);
    assertThat(seqExonEnd).isEqualTo(4);
  }

}
