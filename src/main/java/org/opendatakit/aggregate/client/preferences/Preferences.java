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

import org.mortbay.log.Log;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Preferences {

  public static interface PreferencesCompletionCallback {
	  public void refreshFromUpdatedPreferences();
  }

  private static final String NULL_PREFERENCES_ERROR = "ERROR: somehow got a null preference summary";

  private static String googleSimpleApiKey;

  private static String googleApiClientId;

  private static Boolean odkTablesEnabled;

  private static Boolean fasterBackgroundActionsDisabled;

  public static void updatePreferences(final PreferencesCompletionCallback callback) {
    SecureGWT.getPreferenceService().getPreferences(new AsyncCallback<PreferenceSummary>() {
      public void onFailure(Throwable caught) {
          AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(PreferenceSummary summary) {
        if(summary == null) {
          GWT.log(NULL_PREFERENCES_ERROR);
          AggregateUI.getUI().reportError(new Throwable(NULL_PREFERENCES_ERROR));
        }

        googleSimpleApiKey = summary.getGoogleSimpleApiKey();
        googleApiClientId = summary.getGoogleApiClientId();
        odkTablesEnabled = summary.getOdkTablesEnabled();
        fasterBackgroundActionsDisabled = summary.getFasterBackgroundActionsDisabled();

        if (callback != null) {
        	callback.refreshFromUpdatedPreferences();
        }
      }
    });

  }

  public static String getGoogleSimpleApiKey() {
    if(googleSimpleApiKey != null) {
      return googleSimpleApiKey;
    }
    return "";
  }

  public static String getGoogleApiClientId() {
    if(googleApiClientId != null) {
      return googleApiClientId;
    }
    return "";
  }

  public static Boolean getOdkTablesEnabled() {
    boolean debug = odkTablesEnabled;
    if(odkTablesEnabled != null) {
      return odkTablesEnabled;
    }
    return Boolean.FALSE;
  }

  public static void setOdkTablesBoolean(Boolean enabled) {
    SecureGWT.getPreferenceService().setOdkTablesEnabled(enabled, new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
          AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(Void void1) {
        // do nothing
      }
    });
    odkTablesEnabled = enabled;
  }

  public static Boolean getFasterBackgroundActionsDisabled() {
    if(fasterBackgroundActionsDisabled != null) {
      return fasterBackgroundActionsDisabled;
    }
    return Boolean.FALSE;
  }

  public static void setFasterBackgroundActionsDisabledBoolean(Boolean disabled) {
    SecureGWT.getPreferenceService().setFasterBackgroundActionsDisabled(disabled, new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
          AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(Void void1) {
        // do nothing
      }
    });
    fasterBackgroundActionsDisabled = disabled;
  }


}
