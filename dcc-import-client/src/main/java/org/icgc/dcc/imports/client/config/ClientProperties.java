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
package org.icgc.dcc.imports.client.config;

import java.net.URI;
import java.util.Set;

import javax.validation.Valid;

import org.icgc.dcc.imports.client.util.MongoURI;
import org.icgc.dcc.imports.core.model.ImportSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClientURI;

import lombok.Data;

@Data
@Component
@ConfigurationProperties
public class ClientProperties {

  @Valid
  ImportsProperties imports;
  @Valid
  ICGCProperties icgc;
  @Valid
  MailProperties mail;
  @Valid
  CosmicProperties cosmic;
  @Valid
  GDCLegacyProperties gdcLegacy;

  @Data
  public static class ImportsProperties {

    Set<ImportSource> sources;

    @MongoURI
    MongoClientURI mongoUri;
    URI esUri;

    public Set<ImportSource> getSources() {
      return sources == null || sources.isEmpty() ? ImportSource.all() : sources;
    }

  }

  @Data
  public static class MailProperties {

    boolean enabled;
    String smtpServer;
    String recipients;

  }

  @Data
  public static class CosmicProperties {

    String userName;
    String password;

  }

  @Data
  public static class ICGCProperties {

    String cgpUrl;
    String consumerKey;
    String consumerSecret;
    String accessToken;
    String accessSecret;
    boolean enableHttpLogging;
    boolean enableStrictSSL = true;

  }

  @Data
  public static class GDCLegacyProperties {

    String esUrl;
    String esIndex;
    String portalUrl;

  }
}
