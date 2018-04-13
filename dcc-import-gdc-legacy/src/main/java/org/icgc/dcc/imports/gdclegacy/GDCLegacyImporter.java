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
import org.icgc.dcc.imports.core.util.Jongos;
import org.icgc.dcc.imports.gdclegacy.model.CGHubSequenceRepo;
import org.icgc.dcc.imports.gdclegacy.reader.CGHubDonorsReader;
import org.icgc.dcc.imports.gdclegacy.reader.GDCLegacyPortalIdsReader;
import org.jongo.Jongo;

import java.io.File;
import java.net.URL;

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

    @NonNull
    private URL esURL;

    @NonNull
    private URL gdcLegacyURL;

    private String sequenceReposCollectionName = "sequenceRepos";

    @Override
    public ImportSource getSource() {
        return ImportSource.GDCLEGACY;
    }

    @Override
    public void execute() {
        // GDCLegacyImporter is playing an injector role
        String systemDir = System.getProperty("java.io.tmpdir");
        String tmpPath = (systemDir.endsWith("/")?systemDir.substring(0, systemDir.length()-1):systemDir) + "/dcc/import/gdcLegacy";

        File targetDir = new File(tmpPath);
        if(targetDir.exists()) targetDir.delete();

        Jongo jongo = Jongos.createJongo(mongoUri);

        // Get CGHub Donors
        val donorIds = CGHubDonorsReader.read(esURL);

        // Process donors through GDC legacy reader
        val gdcIds = GDCLegacyPortalIdsReader.read(gdcLegacyURL, donorIds);

        // Construct data for insert into Mongo
        val data = Lists.transform(gdcIds, this::makeRepoItem);
    }

    public CGHubSequenceRepo makeRepoItem(ImmutablePair itemIds) {
        val donorId = itemIds.getLeft().toString();
        val gdcId = itemIds.getRight().toString();
        val gdcLegacyUrl = formatGDCLegacyURL(gdcId);

        return CGHubSequenceRepo
                .builder()
                .donorId(donorId)
                .gdcId(gdcId)
                .gdcLegacyUrl(gdcLegacyUrl)
                .build();
    }

    public URL formatGDCLegacyURL(String gdcId) {
        return getUrl("http://google.ca");
    }
}
