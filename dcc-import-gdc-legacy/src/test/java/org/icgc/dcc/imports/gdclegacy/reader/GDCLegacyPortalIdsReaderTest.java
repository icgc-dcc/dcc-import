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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class GDCLegacyPortalIdsReaderTest {

    private String portalURL;
    private List<String> donorIds;

    @Before
    @SneakyThrows
    public void setUp() {
        this.portalURL = "https://api.gdc.cancer.gov/v0/legacy/cases/ids?query=";
        this.donorIds = Arrays.asList("TCGA-XF-AAMQ-01A", "TCGA-XF-AAMQ-10A", "TCGA-E9-A1NI-10A");
    }

    @Test
    public void testRead() {
        val gdcIds = GDCLegacyPortalIdsReader.read(portalURL, donorIds);
        Assert.assertEquals(gdcIds.get(0).getRight(), "82f4c181-e022-4799-9f7f-01abcdc3803e");
        Assert.assertEquals(gdcIds.get(1).getRight(), "82f4c181-e022-4799-9f7f-01abcdc3803e");
        Assert.assertEquals(gdcIds.get(2).getRight(), "c65b835a-2d38-4250-a173-0780d2c2cf58");
    }
}
