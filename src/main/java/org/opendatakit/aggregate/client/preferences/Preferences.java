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

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class Preferences {
  
  private static String googleMapsApiKey;
  
  private static Boolean odkTablesEnabled;

  public static void updatePreferences() {
    SecureGWT.getPreferenceService().getPreferences(new AsyncCallback<PreferenceSummary>() {
      public void onFailure(Throwable caught) {
          AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(PreferenceSummary summary) {
        googleMapsApiKey = summary.getGoogleMapsApiKey();
        odkTablesEnabled = summary.getOdkTablesEnabled();
      }
    });
    
  }
  
  public static String getGoogleMapsApiKey() {
    if(googleMapsApiKey != null) {
      return googleMapsApiKey;
    }
    return "";
  }

  public static Boolean getOdkTablesEnabled() {
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
  
  public static void setGoogleMapsApiKey(String mapsApiKey) {
    SecureGWT.getPreferenceService().setGoogleMapsKey(mapsApiKey, new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
          AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(Void void1) {
        // do nothing
      }
    });
    googleMapsApiKey = mapsApiKey;
  }
 
  
}
