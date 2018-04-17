package org.icgc.dcc.imports.gdclegacy;

import com.google.common.collect.Lists;
import com.mongodb.MongoClientURI;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.icgc.dcc.imports.core.SourceImporter;
import org.icgc.dcc.imports.core.model.ImportSource;
import org.icgc.dcc.imports.gdclegacy.model.CGHubSequenceRepo;
import org.icgc.dcc.imports.gdclegacy.reader.CGHubDonorsReader;
import org.icgc.dcc.imports.gdclegacy.reader.GDCLegacyPortalIdsReader;
import org.icgc.dcc.imports.gdclegacy.writer.CGHubSequenceRepoWriter;
import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import static org.icgc.dcc.common.core.util.URLs.getUrl;

/**
 * Copyright (c) 2018 The Ontario Institute for Cancer Research. All rights reserved.
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

@Slf4j
@RequiredArgsConstructor
public class GDCLegacyImporter implements SourceImporter {

    @NonNull
    private MongoClientURI mongoUri;

    @Value("${gdcLegacy.gdcLegacyURL}")
    private URL gdcLegacyURL;

    @Value("${gdcLegacy.esURL}")
    private String esURL;

    @Value("${gdcLegacy.esIndex}")
    private String esIndex;

    @Override
    public ImportSource getSource() {
        return ImportSource.GDCLEGACY;
    }

    @Override
    public void execute() {
        // Get CGHub Donor Specimen Id's
        val specimenIds = CGHubDonorsReader.read(esURL, esIndex);

        // Process donors through GDC legacy reader
        val gdcIds = GDCLegacyPortalIdsReader.read(gdcLegacyURL, specimenIds);

        // Construct data for insert into Mongo
        val data = Lists.transform(gdcIds, this::makeRepoItem);

        // Write to mongo
        writeMongo(data);
    }

    private void writeMongo(List<CGHubSequenceRepo> data) {
        val mongoWriter = new CGHubSequenceRepoWriter(mongoUri, "CGHubSequenceRepos");
        mongoWriter.writeValue(data);
    }

    CGHubSequenceRepo makeRepoItem(ImmutablePair itemIds) {
        val specimenId = itemIds.getLeft().toString();
        val gdcId = itemIds.getRight().toString();
        val gdcLegacyUrl = formatGDCLegacyURL(gdcId);

        return CGHubSequenceRepo
                .builder()
                .specimenId(specimenId)
                .gdcId(gdcId)
                .gdcLegacyUrl(gdcLegacyUrl)
                .build();
    }

    String formatGDCLegacyURL(String gdcId) {
        String templateString = "https://portal.gdc.cancer.gov/legacy-archive/search/f?filters=%7B%22op%22:%22and%22,%22content%22:%5B%7B%22op%22:%22in%22,%22content%22:%7B%22field%22:%22cases.case_id%22,%22value%22:%5B%22{0}%22%5D%7D%7D%5D%7D";
        return templateString.replace("{0}", gdcId);
    }
}
