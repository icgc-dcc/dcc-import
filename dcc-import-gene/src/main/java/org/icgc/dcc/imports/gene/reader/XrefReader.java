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

import java.util.HashMap;
import java.util.List;

import org.icgc.dcc.imports.gene.model.XrefMapping;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import lombok.val;

/**
 * Reads display names for genes and constructs map of symbol to gene name
 */
public class XrefReader extends TsvReader {

  /**
   * Dependencies
   */
  public final String interproDBId;

  public XrefReader(String uri, @NonNull String interproDBId) {
    super(uri);
    this.interproDBId = interproDBId;
  }

  /**
   * Get the map of xref display id -> gene name Caches external db ids in hashmaps for entrez, hgnc, mim, & uniprot.
   * Also gets interpro values for domains.
   */
  public XrefMapping read() {
    // HashMap needed for name/entrez due to duplicate entries for some uncharacterized locations.
    val nameMapBuilder = new HashMap<String, String>();
    val entrezMapBuilder = new HashMap<String, String>();
    val hgncMapBuilder = ImmutableMap.<String, String> builder();
    val mimGeneMapBuider = ImmutableMap.<String, String> builder();
    val uniprotMapBuilder = ImmutableMap.<String, String> builder();
    val interproMapBuilder = ImmutableMap.<String, String> builder();

    readRecords().forEach(record -> {
      if (isGeneWiki(record)) {
        nameMapBuilder.put(getSymbol(record), getName(record));
      } else if (isEntrez(record)) {
        entrezMapBuilder.put(getXrefId(record), getDbId(record));
      } else if (isHGNC(record)) {
        hgncMapBuilder.put(getXrefId(record), getDbId(record));
      } else if (isMimGene(record)) {
        mimGeneMapBuider.put(getXrefId(record), getDbId(record));
      } else if (isUniprot(record)) {
        uniprotMapBuilder.put(getXrefId(record), getDbId(record));
      } else if (isInterpro(record)) {
        interproMapBuilder.put(getDbId(record), getName(record));
      }
    });

    return XrefMapping.builder()
        .nameMap(ImmutableMap.copyOf(nameMapBuilder))
        .entrezMap(ImmutableMap.copyOf(entrezMapBuilder))
        .hgncMap(hgncMapBuilder.build())
        .mimMap(mimGeneMapBuider.build())
        .uniprotMap(uniprotMapBuilder.build())
        .interproMap(interproMapBuilder.build())
        .build();
  }

  private boolean isInterpro(List<String> record) {
    return interproDBId.equals(record.get(1));
  }

  private static boolean isGeneWiki(List<String> record) {
    return "12600".equals(record.get(1));
  }

  private static boolean isEntrez(List<String> record) {
    return "1300".equals(record.get(1));
  }

  private static boolean isHGNC(List<String> record) {
    return "1100".equals(record.get(1));
  }

  private static boolean isMimGene(List<String> record) {
    return "1510".equals(record.get(1));
  }

  private static boolean isUniprot(List<String> record) {
    return "2200".equals(record.get(1));
  }

  private static String getSymbol(List<String> record) {
    return record.get(3);
  }

  private static String getName(List<String> record) {
    return record.get(5);
  }

  private static String getXrefId(List<String> record) {
    return record.get(0);
  }

  private String getDbId(List<String> record) {
    return record.get(2);
  }

}
