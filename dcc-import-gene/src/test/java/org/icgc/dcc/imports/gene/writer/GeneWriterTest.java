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
package org.icgc.dcc.imports.gene.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.imports.core.util.Importers.getLocalMongoClientUri;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.icgc.dcc.common.test.mongodb.EmbeddedMongo;
import org.icgc.dcc.imports.gene.GeneImporter;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.JsonLoader;
import com.mongodb.MongoClientURI;

import lombok.SneakyThrows;
import lombok.val;

public class GeneWriterTest {

  /**
   * Schema file.
   */
  private static final String SCHEMA_PATH = "gene.json";
  protected static final String FIXTURES_DIR = "src/test/resources/fixtures";

  /**
   * Test data.
   */
  private static final String KRAS = "src/test/resources/fixtures/KRAS.gtf.gz";

  private final JsonSchema schema = getSchema();

  @Rule
  public final EmbeddedMongo embeddedMongo = new EmbeddedMongo();

  @Test
  @Ignore
  public void testGenesLoader() throws IOException {

    val mongoClientURI = getLocalMongoClientUri(embeddedMongo.getPort(), "dcc-genome-test");

    val importer = new GeneImporter(Paths.get(KRAS).toUri(), mongoClientURI);
    importer.execute();

    val gene = getGene(mongoClientURI);
    System.out.println(gene);

    val report = validate(gene);
    assertThat(report.isSuccess()).isTrue();
  }

  @SneakyThrows
  private ProcessingReport validate(JsonNode gene) {
    val report = schema.validate(gene);

    return report;
  }

  private JsonNode getGene(MongoClientURI mongoUri) {
    val genes = getGenes(mongoUri);
    val gene = genes.findOne().as(JsonNode.class);
    genes.getDBCollection().getDB().getMongo().close();

    return gene;
  }

  private MongoCollection getGenes(MongoClientURI mongoUri) {
    val jongo = getJongo(mongoUri);
    val genes = jongo.getCollection("Gene");

    return genes;
  }

  private Jongo getJongo(MongoClientURI mongoUri) {
    val mongo = embeddedMongo.getMongo();
    val db = mongo.getDB(mongoUri.getDatabase());
    val jongo = new Jongo(db);

    return jongo;
  }

  @SneakyThrows
  private JsonSchema getSchema() {
    val schemaNode = JsonLoader.fromFile(new File("src/test/resources", SCHEMA_PATH));
    val factory = JsonSchemaFactory.byDefault();
    val schema = factory.getJsonSchema(schemaNode);

    return schema;
  }

}