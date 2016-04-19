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
package org.icgc.dcc.imports.pathway.util;

import static com.google.common.base.Objects.firstNonNull;
import static java.lang.Boolean.parseBoolean;
import static lombok.AccessLevel.PRIVATE;

import org.apache.commons.lang3.StringEscapeUtils;
import org.icgc.dcc.imports.geneset.model.pathway.PathwaySegment;
import org.w3c.dom.Node;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public final class PathwaySegmentConverter {

  /**
   * Reactome XML name constants.
   */
  private static final String REACTOME_DB_ID = "dbId";
  private static final String REACTOME_DISPLAY_NAME_ATTRIBUTE_NAME = "displayName";
  private static final String REACTOME_HAS_DIAGRAM_ATTRIBUTE_NAME = "hasDiagram";

  private static final String REACTOME_PREFIX = "R-HSA-";

  public static PathwaySegment convertPathwayNode(@NonNull Node pathwayNode) {
    return PathwaySegment.builder()
        .reactomeId(getReactomeId(pathwayNode).trim())
        .reactomeName(getDisplayName(pathwayNode).trim())
        .diagrammed(getHasDiagram(pathwayNode))
        .build();
  }

  private static String getReactomeId(Node pathwayNode) {
    // From @jweiser:
    // Everything in the RESTful results is human and should map without a problem by just appending the prefix
    // For human data, we literally form the stable id by appending R-HSA- to our internal ids.

    // Convert to stable id
    return REACTOME_PREFIX + getAttributeValue(pathwayNode, REACTOME_DB_ID);
  }

  private static boolean getHasDiagram(Node pathwayNode) {
    return parseBoolean(firstNonNull(getAttributeValue(pathwayNode, REACTOME_HAS_DIAGRAM_ATTRIBUTE_NAME), "false"));
  }

  private static String getDisplayName(Node pathwayNode) {
    val value = getAttributeValue(pathwayNode, REACTOME_DISPLAY_NAME_ATTRIBUTE_NAME);
    return StringEscapeUtils.unescapeHtml4(value);
  }

  private static String getAttributeValue(Node pathwayNode, String attributeName) {
    val attribute = pathwayNode.getAttributes().getNamedItem(attributeName);

    return attribute == null ? null : attribute.getNodeValue();
  }

}
