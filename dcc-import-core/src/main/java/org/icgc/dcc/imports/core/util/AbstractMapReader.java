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
package org.icgc.dcc.imports.core.util;

import static com.google.common.base.Preconditions.checkState;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor
public abstract class AbstractMapReader {

  /**
   * Constants.
   */
  protected static final char TAB_FIELD_SEPARATOR = '\t';
  protected static final char COMMA_FIELD_SEPARATOR = ',';
  protected static final TypeReference<Map<String, String>> RECORD_TYPE_REFERENCE =
      new TypeReference<Map<String, String>>() {};

  /**
   * Configuration
   */
  private final char fieldSeparator;

  public boolean isTSV() {
    return fieldSeparator == TAB_FIELD_SEPARATOR;
  }

  public boolean isCSV() {
    return fieldSeparator == COMMA_FIELD_SEPARATOR;
  }

  @SneakyThrows
  protected Iterable<Map<String, String>> readRecords(String text) {
    return readRecords(new ByteArrayInputStream(text.getBytes(UTF_8)));
  }

  @SneakyThrows
  protected Iterable<Map<String, String>> readRecords(InputStream inputStream) {
    val reader = createReader(CsvSchema.emptySchema().withHeader());

    return readRecords(inputStream, reader);
  }

  @SneakyThrows
  protected Iterable<Map<String, String>> readRecords(String[] columnNames, InputStream inputStream) {
    val reader = createReader(columnNames);

    return readRecords(inputStream, reader);
  }

  private Iterable<Map<String, String>> readRecords(InputStream inputStream, ObjectReader reader) throws IOException,
      JsonProcessingException {
    MappingIterator<Map<String, String>> values = reader.readValues(inputStream);

    return once(values);
  }

  private ObjectReader createReader(String[] columnNames) {
    val schema = CsvSchema.builder();
    for (val columnName : columnNames) {
      schema.addColumn(columnName);
    }

    return createReader(schema.build());
  }

  private ObjectReader createReader(CsvSchema schema) {
    return new CsvMapper()
        .reader(RECORD_TYPE_REFERENCE)
        .with(
            schema
                .withColumnSeparator(fieldSeparator));
  }

  public static <T> Iterable<T> once(final Iterator<T> source) {
    return new Iterable<T>() {

      private AtomicBoolean exhausted = new AtomicBoolean();

      @Override
      public Iterator<T> iterator() {
        checkState(!exhausted.getAndSet(true));

        return source;
      }

    };
  }

}
