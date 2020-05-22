/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.server;

import static org.opendatakit.aggregate.buildconfig.BuildConfig.VERSION;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.preferences.PreferenceSummary;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferenceServiceImpl extends RemoteServiceServlet implements
    org.opendatakit.aggregate.client.preferences.PreferenceService {

  private static final Logger log = LoggerFactory.getLogger(PreferenceServiceImpl.class);
  /**
   * Holds the latest available version as reported by GitHub
   */
  private static String LATEST_AVAILABLE_VERSION = null;
  /**
   * Defines the period to refresh the latest available version value
   */
  private static final Duration PERIOD_OF_REFRESH_LATEST_AVAILABLE_VERSION = Duration.ofDays(1);
  /**
   * Holds the date of the last refresh of the latest available version.
   * <p>
   * On launch, holds a remote date in the past to ensure that we will
   * refresh the value even if the first request comes before the set
   * period has passed since the server's launch.
   */
  private static LocalDate LAST_REFRESH_OF_LATEST_AVAILABLE_VERSION = LocalDate.of(2000, 1, 1);

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -489283284844600170L;
  public static final ObjectMapper JSON = new ObjectMapper();

  @Override
  public PreferenceSummary getPreferences() throws RequestFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      return ServerPreferencesProperties.getPreferenceSummary(cc);
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    }
  }

  @Override
  public void setSkipMalformedSubmissions(Boolean skipMalformedSubmissions)
      throws RequestFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      ServerPreferencesProperties.setSkipMalformedSubmissions(cc, skipMalformedSubmissions);

      log.info("setSkipMalformedSubmissions as: " + Boolean.toString(skipMalformedSubmissions));

    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    }

  }

  @Override
  public String getVersioNote() {
    String shortVersion = VERSION.contains("-") ? VERSION.substring(0, VERSION.indexOf("-")) : VERSION;
    String latestAvailableVersion = getLatestAvailableVersion();
    return latestAvailableVersion == null
        ? shortVersion + " - Version check failed"
        : shortVersion.equals(latestAvailableVersion)
        ? shortVersion + " - You're up to date!"
        : shortVersion + " - <a href=\"https://github.com/getodk/aggregate/releases/latest\" target=\"_blank\">Update available</a>";
  }

  synchronized private static String getLatestAvailableVersion() {
    if (latestAvailableVersionRefreshRequired()) {
      try {
        log.warn("Getting latest available version");
        URL url = new URL("https://api.github.com/repos/getodk/aggregate/releases/latest");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        if (con.getResponseCode() == 200)
          try (InputStream in = con.getInputStream()) {
            LATEST_AVAILABLE_VERSION = Optional.ofNullable(JSON.readTree(in))
                .map(node -> node.get("tag_name"))
                .map(JsonNode::asText)
                .orElse(null);
          }
        else
          log.error("Can't get latest available version - GitHub responded with HTTP " + con.getResponseCode() + " " + con.getResponseMessage());
        LAST_REFRESH_OF_LATEST_AVAILABLE_VERSION = LocalDate.now();
      } catch (IOException e) {
        log.error("Can't get latest available version", e);
      }
    }
    return LATEST_AVAILABLE_VERSION;
  }

  private static boolean latestAvailableVersionRefreshRequired() {
    return Duration.between(LAST_REFRESH_OF_LATEST_AVAILABLE_VERSION.atStartOfDay(), LocalDateTime.now()).abs()
        .compareTo(PERIOD_OF_REFRESH_LATEST_AVAILABLE_VERSION) > 0;
  }
}
