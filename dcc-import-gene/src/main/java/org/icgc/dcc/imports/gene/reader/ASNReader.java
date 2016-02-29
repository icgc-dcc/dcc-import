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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.icgc.dcc.imports.gene.thread.ASNInputReader;
import org.icgc.dcc.imports.gene.thread.OutputReader;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ASNReader {

  private static final String URI =
      "ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/ASN_BINARY/Mammalia/Homo_sapiens.ags.gz";

  /**
   * Runs gene2Xml binary and feeds it ASN.1 dump from NCBI and streams output as XML
   * @return Map of gene_id -> summary text
   */
  @SneakyThrows
  public Map<String, String> callGene2Xml() {

    Map<String, String> summaryMap = new ConcurrentHashMap<String, String>();

    val downloadUtil = getCmd();

    val gzip = new GZIPInputStream(new URL(URI).openStream());

    Process p = Runtime.getRuntime().exec(downloadUtil + " -b T");
    Thread inThread = new Thread(new ASNInputReader(gzip, p.getOutputStream()));
    Thread outThread = new Thread(new OutputReader(p.getInputStream(), summaryMap));
    inThread.start();
    outThread.start();
    p.waitFor();

    return summaryMap;
  }

  /**
   * Downloads Gene2Xml tool based on current platform.
   * @return Path to runnable binary.
   */
  @SneakyThrows
  public static String getCmd() {
    val platform = System.getProperty("os.name");
    log.info("Detecting current platform as: {}", System.getProperty("os.name"));

    String download;
    if (platform.contains("Windows")) {
      download = "ftp://ftp.ncbi.nih.gov/toolbox/ncbi_tools/cmdline/gene2xml.win32.zip";
    } else if (platform.contains("Mac")) {
      download = "ftp://ftp.ncbi.nih.gov/toolbox/ncbi_tools/cmdline/gene2xml.Darwin-13.4.0-x86_64.gz";
    } else if (platform.contains("Linux")) {
      download = "ftp://ftp.ncbi.nih.gov/toolbox/ncbi_tools/cmdline/gene2xml.Linux-2.6.32-573.7.1.el6.x86_64-x86_64.gz";
    } else if (platform.contains("Sun")) {
      download = "ftp://ftp.ncbi.nih.gov/toolbox/ncbi_tools/cmdline/gene2xml.SunOS-5.10-sun4v.gz";
    } else {
      throw new RuntimeException("Your platform is not supported for the dcc-import-gene project.");
    }
    log.info("Downloading: {}", download);

    val tmpFile = File.createTempFile("gene2xml", ".bin");
    tmpFile.setExecutable(true);
    tmpFile.setWritable(true);
    tmpFile.deleteOnExit();

    val ftpInputStream = new URL(download).openStream();
    val gzip = new GZIPInputStream(ftpInputStream);

    val fOut = new FileOutputStream(tmpFile);
    IOUtils.copy(gzip, fOut);

    return tmpFile.getAbsolutePath();
  }

}
