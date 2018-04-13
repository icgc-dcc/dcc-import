package org.icgc.dcc.imports.gdclegacy.writer;

import com.mongodb.MongoClientURI;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.imports.core.util.AbstractJongoWriter;
import org.icgc.dcc.imports.gdclegacy.model.CGHubSequenceRepo;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import static org.icgc.dcc.common.core.model.ReleaseCollection.PROJECT_COLLECTION;

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
public class CGHubSequenceRepoWriter extends AbstractJongoWriter<Iterable<CGHubSequenceRepo>> {

    private String collectionName;

    public CGHubSequenceRepoWriter(@NonNull MongoClientURI mongoUri, @NonNull String collectionName) {
        super(mongoUri);
        this.collectionName = collectionName;
    }

    @Override
    public void writeValue(Iterable<CGHubSequenceRepo> repos) {
        log.info("Clearing CGHub Sequence Repo documents...");
        val repoCollection = jongo.getCollection(collectionName);
        clearCGHubSequenceRepos(repoCollection);

        for (val repo : repos) {
            log.info("Writing GCHub Sequence Repo with Donor Id: {} ...", repo.getDonorId());
            repoCollection.save(repo);
        }
    }

    private static void clearCGHubSequenceRepos(MongoCollection repoCollection) {
        val result = repoCollection.remove();
        log.info("Finished clearing target collection {}: {}", repoCollection, result);
    }

}
