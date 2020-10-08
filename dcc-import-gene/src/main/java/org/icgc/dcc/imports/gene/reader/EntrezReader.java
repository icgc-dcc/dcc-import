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

import static java.util.Collections.unmodifiableMap;
import static org.icgc.dcc.imports.gene.core.Sources.NCBI_URI;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import com.google.common.io.ByteStreams;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntrezReader {

  /**
   * Constants
   */
  private static final String BASE_URI = "ftp://ftp.ncbi.nih.gov/toolbox/ncbi_tools/cmdline/";

  /**
   * Runs gene2xml binary and feeds it ASN.1 dump from NCBI and streams output as XML.
   * 
   * @return Map of gene_id -> summary text
   */
  @SneakyThrows
  public Map<String, String> readSummary() {
    val executable = resolveExecutable();
    val process = Runtime.getRuntime().exec(executable + " -b T");

    // ASN1 -> XML
    @Cleanup
    val gzip = new GZIPInputStream(new URL(NCBI_URI).openStream());
    val inThread = new Thread(new EntrezXMLWriter(gzip, process.getOutputStream()));
    inThread.start();

    // XML -> Map
    val summaryMap = new ConcurrentHashMap<String, String>();
    val outThread = new Thread(new EntrezXMLReader(process.getInputStream(), summaryMap));
    outThread.start();

    // TODO: checkState return code
    val status = process.waitFor();
    log.info("Finished executing gen2xml with return code: {}", status);

    return unmodifiableMap(summaryMap);
  }

  /**
   * Downloads Gene2Xml tool based on current platform.
   * 
   * @return Path to runnable binary.
   */
  @SneakyThrows
  public static String resolveExecutable() {
    val platform = System.getProperty("os.name");
    log.info("Detecting current platform as: {}", platform);

    val download = getDownload(platform);
    log.info("Downloading: {}", download);

    val tmpFile = File.createTempFile("gene2xml", ".bin");
    tmpFile.setExecutable(true);
    tmpFile.setWritable(true);
    tmpFile.deleteOnExit();

    @Cleanup
    val ftpInputStream = new URL(download).openStream();
    val gzip = new GZIPInputStream(ftpInputStream);
    @Cleanup
    val fOut = new FileOutputStream(tmpFile);
    ByteStreams.copy(gzip, fOut);

    return tmpFile.getAbsolutePath();
  }

  private static String getDownload(String platform) {

      return BASE_URI + "gene2xml.Darwin-15.6.0-x86_64.gz";
    
  }

}
