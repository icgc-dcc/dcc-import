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

import static com.google.common.collect.Maps.immutableEntry;
import static org.icgc.dcc.common.core.json.Jackson.DEFAULT;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asInt;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.asText;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.constructExonNode;
import static org.icgc.dcc.imports.gene.core.TranscriptProcessing.constructTranscriptNode;

import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class GeneIterator implements Iterator<ObjectNode> {

  @NonNull
  private final Iterator<ObjectNode> records;

  /**
   * State
   */
  ObjectNode nextGene = null;
  GeneState geneState = null;

  @Override
  public boolean hasNext() {
    if (nextGene != null) {
      return true;
    }

    nextGene = readNextGene();

    return nextGene != null;
  }

  @Override
  public ObjectNode next() {
    if (nextGene != null) {
      val next = nextGene;
      nextGene = readNextGene();

      return next;
    }

    nextGene = readNextGene();

    return nextGene;
  }

  private ObjectNode readNextGene() {
    ObjectNode retVal = null;

    if (geneState == null) {
      geneState = new GeneState();
    }

    while (records.hasNext()) {
      val stateTuple = handleEntry(records.next(), geneState);
      geneState = stateTuple.getKey();
      if (stateTuple.getKey() != stateTuple.getValue()) {
        retVal = geneState.geneNode;
        geneState = stateTuple.getValue();
        return retVal;
      }
    }

    // We ran out of lines to read so finish current state.
    if (geneState.finished == false) {
      finalizeGene(geneState);
      return geneState.geneNode;
    }

    return retVal;
  }

  private Entry<GeneState, GeneState> handleEntry(ObjectNode entry, GeneState geneState) {
    val geneNode = geneState.geneNode;
    val curTranscript = geneState.curTranscript;
    val exons = geneState.exons;

    if (("gene".equals(asText(entry, "type")))) {
      if (geneNode != null) {
        geneState = finalizeGene(geneState);
        val nextState = new GeneState();
        nextState.geneNode = constructGeneNode(entry);

        return immutableEntry(geneState, nextState);
      }

      geneState.geneNode = constructGeneNode(entry);
    } else if ("transcript".equals(asText(entry, "type"))) {
      if (curTranscript != null) {
        geneState = finalizeTranscript(geneState);
      }

      geneState.curTranscript = constructTranscriptNode(entry);
    } else if ("exon".equals(asText(entry, "type"))) {
      exons.add(constructExonNode(entry));
    } else if ("CDS".equals(asText(entry, "type"))) {
      ((ObjectNode) exons.get(exons.size() - 1)).put("cds", entry);
    } else if ("start_codon".equals(asText(entry, "type"))) {
      curTranscript.put("start_exon", exons.size() - 1);
    } else if ("stop_codon".equals(asText(entry, "type"))) {
      curTranscript.put("end_exon", exons.size() - 1);
    }

    return immutableEntry(geneState, geneState);
  }

  private ObjectNode constructGeneNode(ObjectNode data) {
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

  private GeneState finalizeGene(GeneState geneState) {
    // Finish with the current transcript.
    geneState = finalizeTranscript(geneState);
    geneState.geneNode.put("transcripts", geneState.transcripts);
    geneState.finished = true;

    return geneState;
  }

  private GeneState finalizeTranscript(GeneState geneState) {
    geneState.curTranscript.put("exons", geneState.exons);
    geneState.transcripts.add(geneState.curTranscript);
    geneState.exons = DEFAULT.createArrayNode();

    return geneState;
  }

  static class GeneState {

    ObjectNode geneNode = null;
    ObjectNode curTranscript = null;
    ArrayNode transcripts = DEFAULT.createArrayNode();
    ArrayNode exons = DEFAULT.createArrayNode();
    boolean finished = false;

  }

}
