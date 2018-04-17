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

import org.icgc.dcc.common.client.api.ICGCClient;
import org.icgc.dcc.common.client.api.ICGCClientConfig;
import org.icgc.dcc.common.client.api.cgp.CGPClient;
import org.icgc.dcc.common.core.mail.Mailer;
import org.icgc.dcc.imports.client.core.Importer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.val;

@Configuration
public class ClientConfig {

  @Bean
  public Mailer mailer(ClientProperties properties) {
    val mailConfig = properties.getMail();

    return Mailer.builder()
        .enabled(mailConfig.isEnabled())
        .host(mailConfig.getSmtpServer())
        .recipient(mailConfig.getRecipients())
        .build();
  }

  @Bean
  public Importer importer(ClientProperties properties, CGPClient cgpClient, Mailer mailer) {
    val mongoUri = properties.getImports().getMongoUri();
    return new Importer(
        mongoUri,
        mailer,
        cgpClient,
        properties.getCosmic().getUserName(),
        properties.getCosmic().getPassword(),
        properties.getGdcLegacy());
  }

  @Bean
  public CGPClient cgpClient(ClientProperties properties) {
    val icgc = properties.getIcgc();

    val config = ICGCClientConfig.builder()
        .cgpServiceUrl(icgc.cgpUrl)
        .consumerKey(icgc.consumerKey)
        .consumerSecret(icgc.consumerSecret)
        .accessToken(icgc.accessToken)
        .accessSecret(icgc.accessSecret)
        .requestLoggingEnabled(icgc.enableHttpLogging)
        .strictSSLCertificates(icgc.enableStrictSSL)
        .build();

    return ICGCClient.create(config).cgp();
  }

}
