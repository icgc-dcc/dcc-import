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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.of;

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

    private static int maxDonors = 10;

    public static List<String> read(String esURL, String esIndex) {
        return read(esURL, esIndex, empty());
    }

    public static List<String> read(String esURL, String esIndex, int limit) {
        return read(esURL, esIndex, of(limit));
    }

    @SneakyThrows
    private static List<String> read(String esURL, String esIndex, Optional<Integer> limit) {

        // Build ES transport client
        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esURL), 9300));

        // Fetch matching donors from ES
        val response = limit.isPresent() ? queryES(client, esIndex, limit.get()) : queryES(client, esIndex);

        // Filter id's and return
        val filteredIds = filterIds(response);

        // Close the client
        client.close();

        // Return ids
        return filteredIds;
    }

    private static SearchHit[] queryES(TransportClient client, String esIndex) {
        // Build bool query
        BoolQueryBuilder boolQueryBuilder = buildQuery();

        // Build complete search query/request
        SearchSourceBuilder sourceBuilder = buildSource(boolQueryBuilder);

        // Search
        SearchResponse searchResponse =  searchES(esIndex,  sourceBuilder, client);

        return searchResponse.getHits().getHits();
    }

    @SneakyThrows
    static SearchHit[] queryES(TransportClient client, String esIndex, Integer limit) {
        // Build bool query
        BoolQueryBuilder boolQueryBuilder = buildQuery();

        // Build complete search query/request
        SearchSourceBuilder sourceBuilder = buildSource(boolQueryBuilder, limit);

        // Search
        SearchResponse searchResponse =  searchES(esIndex,  sourceBuilder, client);

        return searchResponse.getHits().getHits();
    }

    private static BoolQueryBuilder buildQuery() {
        // Construct Match Queries
        MatchQueryBuilder typeQueryBuilder = new MatchQueryBuilder("_type", "donor");
        MatchQueryBuilder repoQueryBuilder = new MatchQueryBuilder("_summary.repository", "CGHub");

        // Compose bool query using match queries
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        return boolQueryBuilder
                .must(typeQueryBuilder)
                .must(repoQueryBuilder);
    }

    private static SearchSourceBuilder buildSource(BoolQueryBuilder boolQueryBuilder) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        return sourceBuilder
                .query(boolQueryBuilder)
                .fetchSource("specimen.specimen_id", "")
                .size(maxDonors); // max Donors
    }

    private static SearchSourceBuilder buildSource(BoolQueryBuilder boolQueryBuilder, Integer limit) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        return sourceBuilder
                .query(boolQueryBuilder)
                .fetchSource("specimen.specimen_id", "")
                .size(limit);
    }

    @SneakyThrows
    private static SearchResponse searchES(String esIndex, SearchSourceBuilder sourceBuilder, TransportClient client) {
        // Restrict search to esIndex provided in config
        SearchRequest searchRequest = new SearchRequest(esIndex);
        searchRequest.source(sourceBuilder);

        // Execute search on index and return the results
        return client.search(searchRequest).get();
    }

    @SuppressWarnings("unchecked")
    private static List<String> filterIds(SearchHit[] searchHits) {
        return ((List) Arrays.stream(searchHits).flatMap((SearchHit hit) -> {
            Map source = hit.getSource();
            ArrayList<HashMap> specimens = (ArrayList<HashMap>) source.get("specimen");
            return (specimens.stream().map(specimen -> {
                return specimen.get("specimen_id").toString();
            }));
        }).collect(Collectors.toList()));
    }
}
