package org.icgc.dcc.imports.variant.test.processor;

import org.icgc.dcc.imports.variant.processor.api.Downloader;
import org.icgc.dcc.imports.variant.processor.impl.common.GzipFileUnCompressor;
import org.icgc.dcc.imports.variant.processor.impl.common.ShellCommandDownloader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

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

public class GzipFileUnCompressorTest {
  @Test
  public void testUnzip() {
    String systemDir = System.getProperty("java.io.tmpdir");
    String tmpPath = (systemDir.endsWith("/")?systemDir.substring(0, systemDir.length()-1):systemDir) + "/dcc/import/variant";
    String clinvarSummaryFilename = "variant_summary.txt.gz";
    Downloader clinvarSummaryDownloader = new ShellCommandDownloader("ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/" + clinvarSummaryFilename, tmpPath, clinvarSummaryFilename);
    GzipFileUnCompressor unzipper = new GzipFileUnCompressor("variant_summary.txt");
    clinvarSummaryDownloader
        .download().compose(unzipper::unzip)
        .subscribe();
    String clinvarSummaryUnzippedFilename = "variant_summary.txt";
    File unzippedFile = new File(tmpPath + "/" + clinvarSummaryUnzippedFilename);
    Assert.assertEquals(unzippedFile.exists(), true);
    unzippedFile.delete();
  }
}
