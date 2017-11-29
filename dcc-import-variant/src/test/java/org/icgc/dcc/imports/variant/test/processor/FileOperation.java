package org.icgc.dcc.imports.variant.test.processor;

import lombok.SneakyThrows;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * <p>
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

public class FileOperation {
  protected static String tmpPath;

  protected static File[] files = null;

  @BeforeClass
  @SneakyThrows
  public static void initialize() {
    String systemDir = System.getProperty("java.io.tmpdir");
    tmpPath = (systemDir.endsWith("/")?systemDir.substring(0, systemDir.length()-1):systemDir) + "/dcc/import/variant";

    File civic = new File(tmpPath + "/civic.tsv");
    File allele = new File(tmpPath + "/clinvar_allele.txt");
    File summary = new File(tmpPath + "/clinvar_summary.txt");

    if (civic.exists()) civic.delete();
    if (allele.exists()) allele.delete();
    if (summary.exists()) summary.delete();

    files = new File[]{civic, allele, summary};

    for(File file: files) {
      FileOutputStream fos = new FileOutputStream(file);
      InputStream is = FileReaderTest.class.getResourceAsStream("/" + file.getName());
      byte[] buffer = new byte[512];
      int len =0;
      while((len = is.read(buffer)) > 0){
        fos.write(buffer, 0, len);
      }
      fos.flush();
      fos.close();
      is.close();
    }
  }

  @AfterClass
  public static void shutdown() {
    if (files != null) {
      for(File file: files) {
        if(file.exists()) file.delete();
      }
    }
  }

}
