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

import static java.util.stream.Collectors.toMap;
import static org.icgc.dcc.common.core.json.Jackson.DEFAULT;
import static org.icgc.dcc.common.core.util.Splitters.SEMICOLON;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneGtfReader extends TsvReader {

  /**
   * Constants
   */
  private final static Pattern QUOTES = Pattern.compile("\"");
  private final static Pattern UNKNOWN_WHITESPACE = Pattern.compile("\\s+");

  public GeneGtfReader(String uri) {
    super(uri);
  }

  /**
   * Streams GTF file as a Stream of ObjectNodes.
   */
  @SneakyThrows
  public Stream<ObjectNode> read() {
    log.info("CONSUMING GENES");
    return readRecords().filter(this::isData).map(this::parseLine);
  }

  protected boolean isData(List<String> line) {
    return !line.isEmpty() && !line.get(0).isEmpty() && line.get(0).charAt(0) != '#';
  }

  /**
   * Responsible for parsing the lines of the gtf file and producing generic ObjectNodes
   * 
   * @param String representing the current line in the gtf file
   * @return ObjectNode representation of the gtf row.
   */
  protected ObjectNode parseLine(@NonNull List<String> line) {
    val seqname = line.get(0);
    val source = line.get(1);
    val type = line.get(2);
    val locStart = line.get(3);
    val locEnd = line.get(4);

    char strand = line.get(6).charAt(0);
    int locationStart = Integer.parseInt(locStart);
    int locationEnd = Integer.parseInt(locEnd);
    if (locationStart > locationEnd) {
      int location = locationStart;
      locationStart = locationEnd;
      locationEnd = location;
    }

    int strandNumber = convertStrand(strand);

    val attributes = line.get(8);
    val attributeMap = parseAttributes(attributes);

    val feature = DEFAULT.createObjectNode();
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

  private static int convertStrand(char strand) {
    if (strand == '+') {
      return 1;
    } else if (strand == '-') {
      return -1;
    } else {
      return 0;
    }
  }

  /**
   * Last column of data is semicolon separated attributes which are represented as key values separated by whitespace.
   * 
   * @param attributes String representation of raw attributes
   * @return A Map of attribute name to attribute value.
   */
  private static Map<String, String> parseAttributes(String keyValueAttributes) {
    return SEMICOLON.splitToList(keyValueAttributes).stream()
        .map(GeneGtfReader::stripQuotes)
        .map(UNKNOWN_WHITESPACE::split)
        .filter(GeneGtfReader::isValidAttribute)
        .collect(toMap(attr -> attr[0], attr -> attr[1]));
  }

  private static String stripQuotes(String token) {
    return QUOTES.matcher(token.trim()).replaceAll("");
  }

  private static boolean isValidAttribute(String[] attr) {
    return attr.length == 2 && !attr[0].equals("tag") && !attr[0].equals("cdds_id");
  }

}
