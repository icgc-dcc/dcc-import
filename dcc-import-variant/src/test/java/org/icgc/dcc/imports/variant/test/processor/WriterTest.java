package org.icgc.dcc.imports.variant.test.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.MongoClientURI;
import io.reactivex.Observable;
import lombok.val;
import org.icgc.dcc.common.test.mongodb.EmbeddedMongo;
import org.icgc.dcc.imports.variant.model.CivicClinicalEvidenceSummary;
import org.icgc.dcc.imports.variant.model.ClinvarVariant;
import org.icgc.dcc.imports.variant.model.ClinvarVariantSummary;
import org.icgc.dcc.imports.variant.model.ClinvarVariationAllele;
import org.icgc.dcc.imports.variant.processor.impl.civic.CivicClinicalEvidenceSummaryWriter;
import org.icgc.dcc.imports.variant.processor.impl.clinvar.ClinvarVariantWriter;
import org.jongo.Jongo;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

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

public class WriterTest {

  @Rule
  public static EmbeddedMongo mongo = new EmbeddedMongo();

  @Test
  public void civicWriterTest() {
    MongoClientURI mongoClientURI = getLocalMongoClientUri(mongo.getPort(), "dcc-import-variant-civic-writer-test");
    CivicClinicalEvidenceSummary.Builder builder = new CivicClinicalEvidenceSummary.Builder();
    Jongo jongo = getJongo(mongo, mongoClientURI);
    CivicClinicalEvidenceSummaryWriter writer = new CivicClinicalEvidenceSummaryWriter(jongo, "Civic");
    writer.write(
        Observable.just(
            builder.build("JAK2\t3717\tV617F\tLymphoid Leukemia\t10747\t\tDiagnostic\tSupports\tB\tNegative\tJAK2 V617F is not associated with lymphoid leukemia (B-lineage ALL, T-ALL or CLL).\t16081687\tLevine et al., 2005, Blood\t4\taccepted\t1\t64\t28\t9\t5073770\t5073770\tG\tT\tENST00000381652.3\t\t\t\t\t75\tGRCh37\tJAK2 V617F is a highly recurrent mutation in myeloproliferative diseases. It is found in around 98% of patients with polycythemia vera, and just over half of the patients with essential thrombocythemia and primary myelofibrosis. While less associated with cancer, when it is seen, it is more likely to be in myeloid leukemias than lymphoid leukemias. The V617F mutation is an activating mutation, resulting in increased kinase activity. The mutation seems to be restricted to hematologic malignancies. Treatment of JAK mutant diseases with ruxolitinib has seen some clinical success.\tSomatic Mutation\t2015-06-21 16:49:38 UTC\thttps://civic.genome.wustl.edu/links/evidence_items/1\thttps://civic.genome.wustl.edu/links/variants/64\thttps://civic.genome.wustl.edu/links/genes/28")
//            builder.build("PDGFRA\t5156\tD842V\tGastrointestinal Stromal Tumor\t9253\t\tDiagnostic\tSupports\tB\tNegative\tGIST tumors harboring PDGFRA D842V mutation are more likely to be benign than malignant.\t15146165\tLasota et al., 2004, Lab. Invest.\t3\taccepted\t2\t99\t38\t4\t55152093\t55152093\tA\tT\tENST00000257290.5\t\t\t\t\t75\tGRCh37\tPDGFRA D842 mutations are characterized broadly as imatinib resistance mutations. This is most well characterized in gastrointestinal stromal tumors, but other cell lines containing these mutations have been shown to be resistant as well. Exogenous expression of the A842V mutation resulted in constitutive tyrosine phosphorylation of PDGFRA in the absence of ligand in 293T cells and cytokine-independent proliferation of the IL-3-dependent Ba/F3 cell line, both evidence that this is an activating mutation. In imatinib resistant cell lines, a number of other therapeutics have demonstrated efficacy. These include; crenolanib, sirolimus, and midostaurin (PKC412).\tSomatic Mutation\t2015-06-21 16:49:38 UTC\thttps://civic.genome.wustl.edu/links/evidence_items/2\thttps://civic.genome.wustl.edu/links/variants/99\thttps://civic.genome.wustl.edu/links/genes/38")
        )
    ).subscribe();
    Assert.assertEquals(
      jongo.getCollection("Civic").findOne().as(JsonNode.class).get("geneCivicUrl").asText(), "https://civic.genome.wustl.edu/links/genes/28"
    );

  }

  @Test
  public void clinvarWriterTest() {
    MongoClientURI mongoClientURI = getLocalMongoClientUri(mongo.getPort(), "dcc-import-variant-clinvar-writer-test");
    ClinvarVariantSummary.Builder summaryBuilder = new ClinvarVariantSummary.Builder();
    ClinvarVariationAllele.Builder alleleBuilder = new ClinvarVariationAllele.Builder();

    Jongo jongo = getJongo(mongo, mongoClientURI);
    ClinvarVariantWriter writer = new ClinvarVariantWriter(jongo, "Clinvar");

    writer.write(
        Observable.just(
            ClinvarVariant.Builder.build(
                summaryBuilder.build("15041\tindel\tNM_014855.2(AP5Z1):c.80_83delGGATinsTGCTGTAAACTGTAACTGTAAA (p.Arg27_Ala362delinsLeuLeuTer)\t9907\tAP5Z1\tHGNC:22197\tPathogenic\t1\tJun 29, 2010\t397704705\t-\tRCV000000012\tMedGen:C3150901,OMIM:613647,Orphanet:ORPHA306511\tSpastic paraplegia 48, autosomal recessive\tgermline\tgermline\tGRCh37\tNC_000007.13\t7\t4820844\t4820847\tGGAT\tTGCTGTAAACTGTAACTGTAAA\t7p22.1\tno assertion criteria provided\t1\t\tN\tOMIM Allelic Variant:613653.0001\t1"),
                alleleBuilder.build("2\tVariant\t15041\tyes")
            )
        )
    ).subscribe();

    Assert.assertEquals(
        jongo.getCollection("Clinvar").findOne().as(JsonNode.class).get("chromosomeAccession").asText(), "NC_000007.13"
    );

  }

  private Jongo getJongo(EmbeddedMongo mongodb, MongoClientURI mongoUri) {
    val mongo = mongodb.getMongo();
    val db = mongo.getDB(mongoUri.getDatabase());
    val jongo = new Jongo(db);

    return jongo;
  }
}
