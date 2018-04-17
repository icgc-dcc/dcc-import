package org.icgc.dcc.imports.gdclegacy.reader;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Given a list of donor id's from our ES cluster, return corresponding GDC legacy portal ids
 */
@Slf4j
public class GDCLegacyPortalIdsReader {

    public static List<ImmutablePair> read(@NonNull URL portalURL, @NonNull List<String> specimenIds) {
        return getGDCLegacyIds(portalURL, specimenIds);
    }

    private static List<ImmutablePair> getGDCLegacyIds(URL portalURL, List<String> specimenIds) {
        // Process id's and return
        return specimenIds.stream().map(specimenId -> new ImmutablePair<>(specimenId, queryGDCforId(portalURL, specimenId))).collect(Collectors.toList());
    }

    private static String queryGDCforId(URL url, String query) throws RuntimeException {

        // Initial setup
        URL queryURL;
        String id = "";

        // Build the URL (base url + query which is the specimenId)
        try {
            queryURL = new URL(url + query);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) queryURL.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.disconnect();

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            // Get response
            InputStream inputStream = conn.getInputStream();

            // Parse to JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> jsonMap = mapper.readValue(inputStream, Map.class);

            id = extractId(jsonMap);
        } catch (IOException e) {
            // Covers malformed URL exception
            e.printStackTrace();
        }

        return id;
    }

    private static String extractId(Map<String, Object> jsonMap) {
        HashMap data = (HashMap) jsonMap.get("data");
        HashMap hit = (HashMap) ((ArrayList) data.get("hits")).get(0);
        return hit.get("id").toString();
    }
}
