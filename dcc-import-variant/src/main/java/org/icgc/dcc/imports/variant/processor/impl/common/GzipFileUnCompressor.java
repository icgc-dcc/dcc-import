package org.icgc.dcc.imports.variant.processor.impl.common;

import io.reactivex.Observable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.imports.variant.processor.api.UnCompressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;

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

@RequiredArgsConstructor
public class GzipFileUnCompressor implements UnCompressor {

  @NonNull
  private String untarFilename;

  @Override
  public Observable<File> unzip(Observable<File> input) {

    return
      input.map(src -> {
        File outputFile = new File(src.getParentFile(), untarFilename);
        byte[] buffer = new byte[1024];
        GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(src));
        FileOutputStream out = new FileOutputStream(outputFile);
        int len;
        while((len = gzis.read(buffer)) > 0){
          out.write(buffer, 0, len);
        }
        gzis.close();
        out.close();
        src.delete();
        return outputFile;
      });

  }
}
