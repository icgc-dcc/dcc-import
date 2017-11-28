package org.icgc.dcc.imports.variant.processor.impl.civic;

import io.reactivex.Observable;
import org.icgc.dcc.imports.variant.model.CivicClinicalEvidenceSummary;
import org.icgc.dcc.imports.variant.processor.api.FileReader;

import java.io.BufferedReader;
import java.io.File;
import java.util.stream.Collectors;

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

public class CivicClinicalEvidenceSummaryFileReader implements FileReader<CivicClinicalEvidenceSummary>{

  private CivicClinicalEvidenceSummary.Builder builder = new CivicClinicalEvidenceSummary.Builder();

  @Override
  public Observable<CivicClinicalEvidenceSummary> extract(Observable<File> input) {
    return
        input.flatMap(file -> {
          BufferedReader reader = new BufferedReader(new java.io.FileReader(file));
          return
              Observable.fromIterable(
                  reader.lines().skip(1).collect(Collectors.toList())
              ).map(builder::build);
        });
  }
}
