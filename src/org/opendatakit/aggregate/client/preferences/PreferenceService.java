package org.opendatakit.aggregate.client.preferences;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("preferenceservice")
public interface PreferenceService extends RemoteService {
  String getGoogleMapsKey();
  
  void setGoogleMapsKey(String key);
}
