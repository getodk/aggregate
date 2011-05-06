package org.opendatakit.aggregate.client.preferences;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface PreferenceServiceAsync {

  void getGoogleMapsKey(AsyncCallback<String> callback);

  void setGoogleMapsKey(String key, AsyncCallback<Void> callback);

}
