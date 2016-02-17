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

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import org.icgc.dcc.imports.gene.thread.ASNInputReader;
import org.icgc.dcc.imports.gene.thread.OutputReader;

import lombok.SneakyThrows;
import lombok.val;

public class ASNReader {

  private static final String URI =
      "ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/ASN_BINARY/Mammalia/Homo_sapiens.ags.gz";

  private static final String CMD = "/Users/dandric/Downloads/gene2xml.Darwin-13.4.0-x86_64 -b T";

  @SneakyThrows
  public Map<String, String> callGene2Xml() {

    Map<String, String> summaryMap = new ConcurrentHashMap<String, String>();

    val gzip = new GZIPInputStream(new URL(URI).openStream());

    Process p = Runtime.getRuntime().exec(CMD);
    Thread inThread = new Thread(new ASNInputReader(gzip, p.getOutputStream()));
    Thread outThread = new Thread(new OutputReader(p.getInputStream(), summaryMap));
    inThread.start();
    outThread.start();
    p.waitFor();

    return summaryMap;
  }

}
