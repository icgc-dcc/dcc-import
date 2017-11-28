package org.icgc.dcc.imports.variant.processor.impl.clinvar;

import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.icgc.dcc.imports.variant.model.ClinvarVariant;
import org.icgc.dcc.imports.variant.model.ClinvarVariantSummary;
import org.icgc.dcc.imports.variant.model.ClinvarVariationAllele;
import org.icgc.dcc.imports.variant.processor.api.*;
import scala.Tuple2;

import java.util.Iterator;

import static org.apache.spark.sql.functions.*;


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
public class ClinvarVariantProcessor implements VariantDataProcessor {

  @NonNull private Downloader summaryDownloader;
  @NonNull private UnCompressor summaryUnzipper;
  @NonNull private FileReader<ClinvarVariantSummary> summaryReader;
  @NonNull private Downloader alleleDownloader;
  @NonNull private UnCompressor alleleUnzipper;
  @NonNull private FileReader<ClinvarVariationAllele> alleleReader;

  @NonNull private ContentWriter<ClinvarVariant> writer;

  @Override
  public void process() {
    SparkSession session = SparkSession.builder().appName("ClinvarVariantProcessor").master("local[*]").getOrCreate();

    Single.zip(
        summaryDownloader.download().compose(summaryUnzipper::unzip).compose(summaryReader::extract).toList(),
        alleleDownloader.download().compose(alleleUnzipper::unzip).compose(alleleReader::extract).toList(),
        (summary, allele) -> Pair.of(summary, allele)
    ).map(tuple ->
        session
            .createDataset(tuple.getKey(), Encoders.bean(ClinvarVariantSummary.class))
            .joinWith(
                session.createDataset(tuple.getValue(), Encoders.bean(ClinvarVariationAllele.class)),
                col("alleleID")
            ).map(
                new MapFunction<Tuple2<ClinvarVariantSummary,ClinvarVariationAllele>, ClinvarVariant>() {
                  @Override
                  public ClinvarVariant call(Tuple2<ClinvarVariantSummary, ClinvarVariationAllele> pair) throws Exception {
                    return ClinvarVariant.Builder.build(pair._1(), pair._2());
                  }
                },
                Encoders.bean(ClinvarVariant.class)
            )
    ).subscribe(ds ->
      ds.foreachPartition((ForeachPartitionFunction<ClinvarVariant>) iterator ->
        Observable.fromIterable(() -> iterator).compose(writer::write).subscribe()
      )
    );

    session.close();
  }
}
