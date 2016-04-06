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

package org.opendatakit.aggregate.client.preferences;

import java.util.ArrayList;
import java.util.TreeSet;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Preferences {

  public static interface PreferencesCompletionCallback {
    public void refreshFromUpdatedPreferences();

    public void failedRefresh();
  }

  private static final String NULL_PREFERENCES_ERROR = "ERROR: somehow got a null preference summary";

  private static String googleSimpleApiKey;

  private static String googleApiClientId;

  private static String enketoApiUrl;

  private static String enketoApiToken;

  private static Boolean odkTablesEnabled;
  
  private static String appName;

  private static Boolean fasterBackgroundActionsDisabled;
  
  private static Boolean skipMalformedSubmissions;

  private static int nesting = 0;
  private static ArrayList<PreferencesCompletionCallback> userCallbacks = new ArrayList<PreferencesCompletionCallback>();

  private static AsyncCallback<PreferenceSummary> callback = new AsyncCallback<PreferenceSummary>() {
    @Override
    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
      --nesting;
      if ( nesting <= 0 ) {
        nesting = 0;
        ArrayList<PreferencesCompletionCallback> local = userCallbacks;
        userCallbacks = new ArrayList<PreferencesCompletionCallback>();
        for ( PreferencesCompletionCallback uc : local ) {
          uc.failedRefresh();
        }
      }
    }

    @Override
    public void onSuccess(PreferenceSummary summary) {
      if (summary == null) {
        GWT.log(NULL_PREFERENCES_ERROR);
        AggregateUI.getUI().reportError(new Throwable(NULL_PREFERENCES_ERROR));
      }

      googleSimpleApiKey = summary.getGoogleSimpleApiKey();
      googleApiClientId = summary.getGoogleApiClientId();
      enketoApiUrl = summary.getEnketoApiUrl();
      enketoApiToken = summary.getEnketoApiToken();
      @SuppressWarnings("unused")
      Boolean oldTablesValue = odkTablesEnabled;
      odkTablesEnabled = summary.getOdkTablesEnabled();
      appName = summary.getAppName();
      fasterBackgroundActionsDisabled = summary.getFasterBackgroundActionsDisabled();
      skipMalformedSubmissions = summary.getSkipMalformedSubmissions();

      --nesting;
      if ( nesting <= 0 ) {
        nesting = 0;
        ArrayList<PreferencesCompletionCallback> local = userCallbacks;
        userCallbacks = new ArrayList<PreferencesCompletionCallback>();
        for ( PreferencesCompletionCallback uc : local ) {
          uc.refreshFromUpdatedPreferences();
        }
      }

      AggregateUI.getUI().updateOdkTablesFeatureVisibility();
    }
  };

  public static void updatePreferences(final PreferencesCompletionCallback userCallback) {
    userCallbacks.add(userCallback);
    ++nesting;
    SecureGWT.getPreferenceService().getPreferences(callback);
  }

  public static String getGoogleSimpleApiKey() {
    if (googleSimpleApiKey != null) {
      return googleSimpleApiKey;
    }
    return "";
  }

  public static String getGoogleApiClientId() {
    if (googleApiClientId != null) {
      return googleApiClientId;
    }
    return "";
  }

  public static boolean showEnketoIntegration() {
    TreeSet<GrantedAuthorityName> authorities = AggregateUI.getUI().getUserInfo().getGrantedAuthorities();
    if (authorities.contains(GrantedAuthorityName.ROLE_DATA_COLLECTOR) || authorities.contains(GrantedAuthorityName.ROLE_DATA_OWNER)) {
      if (getEnketoApiUrl() != null && !getEnketoApiUrl().equals("")) {
        return true;
      }
    }
    return false;
  }

  public static String getEnketoApiUrl() {
    if (enketoApiUrl != null) {
      return enketoApiUrl;
    }
    return "";
  }

  public static String getEnketoApiToken() {
    if (enketoApiToken != null) {
      return enketoApiToken;
    }
    return "";
  }

  public static Boolean getOdkTablesEnabled() {
    if (odkTablesEnabled != null) {
      return odkTablesEnabled;
    }
    return Boolean.FALSE;
  }


  public static String getAppName() {
    if (appName != null && appName.length() != 0) {
      return appName;
    }
    return "default";
  }

  public static Boolean getFasterBackgroundActionsDisabled() {
    if (fasterBackgroundActionsDisabled != null) {
      return fasterBackgroundActionsDisabled;
    }
    return Boolean.FALSE;
  }

  public static Boolean getSkipMalformedSubmissions() {
    if (skipMalformedSubmissions != null) {
      return skipMalformedSubmissions;
    }
    return Boolean.FALSE;
  }



}
