package org.icgc.dcc.imports.variant.test.processor;

import com.mongodb.MongoClientURI;
import io.reactivex.Observable;
import lombok.val;
import org.icgc.dcc.common.test.mongodb.EmbeddedMongo;
import org.icgc.dcc.imports.variant.processor.api.Downloader;
import org.icgc.dcc.imports.variant.processor.api.UnCompressor;
import org.icgc.dcc.imports.variant.processor.impl.civic.CivicClinicalEvidenceSummaryFileReader;
import org.icgc.dcc.imports.variant.processor.impl.civic.CivicClinicalEvidenceSummaryProcessor;
import org.icgc.dcc.imports.variant.processor.impl.civic.CivicClinicalEvidenceSummaryWriter;
import org.icgc.dcc.imports.variant.processor.impl.clinvar.*;
import org.jongo.Jongo;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;

import static org.icgc.dcc.imports.core.util.Importers.getLocalMongoClientUri;

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

public class ProcessorTest extends FileOperation implements Serializable {
  @Rule
  public static EmbeddedMongo mongo = new EmbeddedMongo();


  @Test
  public void civicTest() {
    MongoClientURI mongoClientURI = getLocalMongoClientUri(mongo.getPort(), "dcc-import-variant-civic-processor-test");
    Jongo jongo = getJongo(mongo, mongoClientURI);
    Downloader downloader = new Downloader() {
      @Override
      public Observable<File> download() {
        return Observable.just(files[0]);
      }
    };

    CivicClinicalEvidenceSummaryProcessor processor = new CivicClinicalEvidenceSummaryProcessor(
        downloader,
        new CivicClinicalEvidenceSummaryFileReader(),
        new CivicClinicalEvidenceSummaryWriter(jongo, "Civic")
        );
    processor.process();

    Assert.assertEquals(
      jongo.getCollection("Civic").count(), 9
    );
  }

  @Test
  public void clinvarTest() {
    MongoClientURI mongoClientURI = getLocalMongoClientUri(mongo.getPort(), "dcc-import-variant-clinvar-processor-test");
    Jongo jongo = getJongo(mongo, mongoClientURI);
    ClinvarVariantProcessor processor = new ClinvarVariantProcessor(
        new Downloader() {
          @Override
          public Observable<File> download() {
            return Observable.just(files[2]);
          }
        },
        new UnCompressor() {
          @Override
          public Observable<File> unzip(Observable<File> input) {
            return input;
          }
        },
        new ClinvarVariantSummaryFileReader(),
        new Downloader() {
          @Override
          public Observable<File> download() {
            return Observable.just(files[1]);
          }
        },
        new UnCompressor() {
          @Override
          public Observable<File> unzip(Observable<File> input) {
            return input;
          }
        },
        new ClinvarVariationAlleleFileReader(),
        new ClinvarVariantWriter(jongo, "Clinvar")
    );
    processor.process();

    Assert.assertEquals(
        jongo.getCollection("Clinvar").count(), 9
    );
  }

  private Jongo getJongo(EmbeddedMongo mongodb, MongoClientURI mongoUri) {
    val mongo = mongodb.getMongo();
    val db = mongo.getDB(mongoUri.getDatabase());
    val jongo = new Jongo(db);

    return jongo;
  }
}
