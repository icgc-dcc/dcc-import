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
package org.icgc.dcc.imports.cgc.util;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.net.HttpHeaders.COOKIE;
import static com.google.common.net.HttpHeaders.SET_COOKIE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Client for interfacing with Cosmic endpoints.
 * 
 * <pre>
 * curl -c /tmp/cookie.txt -X POST 'https://cancer.sanger.ac.uk/cosmic/User' --data 'email=user%40oicr.on.ca&pass=pass' -v
 * curl -b /tmp/cookie.txt 'https://cancer.sanger.ac.uk/cosmic/census/all?export=tsv'
 * </pre>
 */
@RequiredArgsConstructor
public class CosmicClient {

  /**
   * Constants
   */
  private static final String DEFAULT_API_URL = "https://cancer.sanger.ac.uk/cosmic";
  private static final int READ_TIMEOUT = (int) SECONDS.toMillis(5);
  private static final String METHOD_POST = "POST";

  /**
   * Configuration.
   */
  @NonNull
  private final String url;
  @NonNull
  private final String userName;
  @NonNull
  private final String password;

  /**
   * State.
   */
  private String sessionId;

  public CosmicClient(String userName, String password) {
    this(DEFAULT_API_URL, userName, password);
  }

  @SneakyThrows
  public void login() {
    // Setup
    val connection = openConnection("/User");
    connection.setInstanceFollowRedirects(false); // This is required for login!
    connection.setRequestMethod(METHOD_POST);
    connection.setDoOutput(true);

    // Send request
    val request = "email=" + userName + "&pass=" + password;
    connection.getOutputStream().write(request.getBytes(UTF_8));

    // Process response
    val code = connection.getResponseCode();
    checkState(code == 302 || code == 200, "Bad login: response code %s", code);
    this.sessionId = getSessionId(connection.getHeaderFields());
  }

  public InputStream getCensusCSV() {
    // Need to use csv because headers are always comma separated!
    return get("/census/all?export=csv");
  }

  @SneakyThrows
  private HttpURLConnection openConnection(String path) throws SocketTimeoutException {
    val connection = (HttpsURLConnection) new URL(url + path).openConnection();
    connection.setReadTimeout(READ_TIMEOUT);
    connection.setConnectTimeout(READ_TIMEOUT);

    return connection;
  }

  @SneakyThrows
  private InputStream get(String path) {
    checkState(isSessionActive(), "You must login first before calling API methods.");
    val connection = openConnection(path);
    connection.setRequestProperty(COOKIE, sessionId);

    return connection.getInputStream();
  }

  private boolean isSessionActive() {
    return sessionId != null;
  }

  private static String getSessionId(Map<String, List<String>> headers) {
    for (val entry : headers.entrySet()) {
      val headerName = entry.getKey();
      if (isSetCookieHeader(headerName)) {
        for (val headerValue : entry.getValue()) {
          return headerValue;
        }
      }
    }

    throw new IllegalArgumentException("Could not find session id in headers: " + headers);
  }

  private static boolean isSetCookieHeader(String headerName) {
    return headerName != null && headerName.compareToIgnoreCase(SET_COOKIE) == 0;
  }

}
