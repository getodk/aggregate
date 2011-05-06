package org.opendatakit.aggregate.client.preferences;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Preferences {
  
  private static String googleMapsApiKey;
  

  public static void updatePreferences() {
    PreferenceServiceAsync preferencesSvc = GWT.create(PreferenceService.class);
    preferencesSvc.getGoogleMapsKey(new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        // TODO: Do something with errors.
      }

      public void onSuccess(String key) {
        googleMapsApiKey = key;
      }
    });

  }
  
  public static String getGoogleMapsApiKey() {
    if(googleMapsApiKey != null) {
      return googleMapsApiKey;
    }
    return "";
  }
  
  public static void setGoogleMapsApiKey(String mapsApiKey) {
    PreferenceServiceAsync preferencesSvc = GWT.create(PreferenceService.class);
    preferencesSvc.setGoogleMapsKey(mapsApiKey, new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
        // TODO: Do something with errors.
      }

      public void onSuccess(Void void1) {
        // do nothing
      }
    });
    googleMapsApiKey = mapsApiKey;
  }
 
  
}
