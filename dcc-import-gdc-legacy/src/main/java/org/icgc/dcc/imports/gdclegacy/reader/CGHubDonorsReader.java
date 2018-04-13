package org.icgc.dcc.imports.gdclegacy.reader;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.util.ArrayList;

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

/**
 * Reads all donor id's from config'd ES cluster that are in the CGHub repository
 */
@Slf4j
public class CGHubDonorsReader {

    public static ArrayList<String> read(String esURL, String esIndex) {
        // Fetch matching donors from ES
        val response = queryES(esURL, esIndex);

        // Filter id's and return
        return filterIds(response);
    }

    @SneakyThrows
    static String queryES(String esURL, String esIndex) {

        // Build ES transport client
        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esURL), 9300));

        // Construct Match Queries
        MatchQueryBuilder typeQueryBuilder = new MatchQueryBuilder("_type", "donor");
        MatchQueryBuilder repoQueryBuilder = new MatchQueryBuilder("_summary.repository", "CGHub");

        // Compose bool query using match queries
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder
            .must(typeQueryBuilder)
            .must(repoQueryBuilder);

        // Build complete search query/request
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        String[] includeFields = new String[] {"specimen.specimen_id"};
        sourceBuilder
            .query(boolQueryBuilder)
            .fetchSource(includeFields, null)
            .size(10); // temp size for testing

        // Restrict search to esIndex provided in config
        SearchRequest searchRequest = new SearchRequest(esIndex);
        searchRequest.source(sourceBuilder);

        // Execute search on index and then close the client
        SearchResponse searchResponse = client.search(searchRequest).get();

        client.close();

        val hits = searchResponse.getHits();

        return "";
    }

    static ArrayList<String> filterIds(String esQueryResponse) {
        return new ArrayList<String>();
    }
}
