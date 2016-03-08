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

import static org.icgc.dcc.common.core.util.Splitters.TAB;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import com.google.common.base.Stopwatch;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for reading files and consuming lines.
 */
@Slf4j
public final class BaseReader {

  /**
   * Reads the lines of a file and feeding them to the provided lambda for processing
   * @param URI - String URI of the file to read
   * @param lambda - Lambda encapsulating the callers logic for operating on a line
   */
  @SneakyThrows
  public static void read(String URI, Consumer<String[]> lambda) {
    log.info("Reading {}", URI);
    val watch = Stopwatch.createStarted();
    val bufferedReader = getReader(URI);

    for (String s = bufferedReader.readLine(); null != s; s = bufferedReader.readLine()) {
      s = s.trim();
      if (s.length() > 0) {
        String[] line = TAB.splitToList(s).stream().toArray(String[]::new);
        lambda.accept(line);
      }
    }
    log.info("Read finished in {}", watch);
  }

  @SneakyThrows
  private static BufferedReader getReader(String URI) {
    val gzip = new GZIPInputStream(new URL(URI).openStream());
    val inputStreamReader = new InputStreamReader(gzip);
    val bufferedReader = new BufferedReader(inputStreamReader);
    return bufferedReader;
  }

}
