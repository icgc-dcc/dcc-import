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
package org.icgc.dcc.imports.gene.reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.Splitters.TAB;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asInt;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asText;

import java.util.List;

import org.junit.Test;

import lombok.val;

public class GeneGtfReaderTest {

  public static final List<String> GTF_LINE =
      TAB.splitToList(
          "1\tensembl\ttranscript\t11872\t14412\t.\t+\t.\tgene_id \"ENSG00000223972\"; gene_version \"4\"; transcript_id \"ENST00000515242\";");

  @Test
  public void testIsData1() {
    val line = TAB.splitToList("# THIS IS NOT DATA");
    val reader = new GeneGtfReader("test");
    reader.isData(line);
    assertThat(reader.isData(line)).isEqualTo(false);
  }

  @Test
  public void testIsData2() {
    val line = TAB.splitToList("");
    val reader = new GeneGtfReader("test");
    assertThat(reader.isData(line)).isEqualTo(false);
  }

  @Test
  public void testIsData3() {
    val reader = new GeneGtfReader("test");
    assertThat(reader.isData(GTF_LINE)).isEqualTo(true);
  }

  @Test
  public void testParseLine1() {
    val reader = new GeneGtfReader("test");
    val entry = reader.parseLine(GTF_LINE);
    assertThat(entry).isNotNull();
    assertThat(asText(entry, "source")).isEqualTo("ensembl");
    assertThat(asText(entry, "type")).isEqualTo("transcript");
    assertThat(asInt(entry, "locationStart")).isEqualTo(11872);
    assertThat(asInt(entry, "locationEnd")).isEqualTo(14412);
    assertThat(asText(entry, "strand")).isEqualTo("1");
    assertThat(asText(entry, "gene_id")).isEqualTo("ENSG00000223972");
    assertThat(asText(entry, "transcript_id")).isEqualTo("ENST00000515242");
  }

}
