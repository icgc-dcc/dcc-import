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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Given a list of donor id's from our ES cluster, return corresponding GDC legacy portal ids
 */
@Slf4j
public class GDCLegacyPortalIdsReader {

    public static List<ImmutablePair> read(@NonNull URL portalURL, @NonNull ArrayList<String> donorIds) {
       return getGDCLegacyIds(portalURL, donorIds);
    }

    private static List<ImmutablePair> getGDCLegacyIds(URL portalURL, ArrayList<String> donorIds) {
        // Process id's and return
        return Streams.zip(donorIds.stream(), donorIds.stream(), ImmutablePair::new).collect(Collectors.toList());
    }
}
