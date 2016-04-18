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

import static org.icgc.dcc.common.core.util.Formats.formatCount;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EntrezXMLReader implements Runnable {

  /**
   * State.
   */
  @NonNull
  private final InputStream in;
  @NonNull
  private final Map<String, String> summaryMap;

  @Override
  @SneakyThrows
  public void run() {
    val handler = new EntrezXMLHandler(summaryMap);

    val factory = createParserFactory();
    val saxParser = factory.newSAXParser();
    saxParser.parse(in, handler);
  }

  private static SAXParserFactory createParserFactory()
      throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
    val factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    factory.setFeature("http://xml.org/sax/features/validation", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    return factory;
  }

  @RequiredArgsConstructor
  static class EntrezXMLHandler extends DefaultHandler {

    /**
     * State
     */
    private final Map<String, String> summaryMap;

    private final Stack<String> elementStack = new Stack<String>();
    private boolean isEnsembl = false;
    private String currentId;
    private StringBuilder currentValue;

    /**
     * Dependencies
     */
    @NonNull
    public List<String> ids = new ArrayList<String>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      if ("Entrezgene_summary".equals(qName)) {
        currentValue = new StringBuilder();
      }

      this.elementStack.push(qName);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      if ("Entrezgene_summary".equals(qName)) {
        summaryMap.put(currentId, currentValue.toString());
      }

      this.elementStack.pop();
    }

    @Override
    public void characters(char ch[], int start, int length) {
      String value = new String(ch, start, length).trim();
      if (value.length() == 0) return;

      if (isGeneId()) {
        ids.add(value);
        isEnsembl = false;
        currentId = value;
        if (ids.size() % 10000 == 0) {
          log.info("Read {} records from xml", formatCount(ids));
        }
      } else if (isDbName()) {
        if (isEnsembl(value)) {
          isEnsembl = true;
        }
      } else if (isSummary()) {
        currentValue.append(value);
      }
    }

    private String currentElement() {
      return this.elementStack.peek();
    }

    /**
     * Determines if current element is a gene based on current depth in XML.
     * @return true if current element corresponds to a gene, false otherwise.
     */
    private boolean isGene() {
      return this.elementStack.size() == 9;
    }

    private boolean isGeneId() {
      return isEnsembl && isGene() && currentElement().equals("Object-id_str");
    }

    private boolean isSummary() {
      return currentElement().equals("Entrezgene_summary");
    }

    private boolean isDbName() {
      return currentElement().equals("Dbtag_db");
    }

    private boolean isEnsembl(String value) {
      return value.equals("Ensembl");
    }

  }

}
