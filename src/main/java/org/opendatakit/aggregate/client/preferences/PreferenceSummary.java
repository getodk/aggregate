package org.opendatakit.aggregate.client.preferences;

import java.io.Serializable;

public class PreferenceSummary implements Serializable{

  private static final long serialVersionUID = -5344882762820967969L;

  private String googleMapsApiKey;
  
  private String googleApiClientId;
  
  private Boolean odkTablesEnabled;
  
  public PreferenceSummary() {
    
  }
  
  public PreferenceSummary(String googleMapsApiKey, String googleApiClientId, Boolean odkTablesEnabled) {
    this.googleMapsApiKey = googleMapsApiKey;
    this.googleApiClientId = googleApiClientId;
    this.odkTablesEnabled = odkTablesEnabled;
  }

  public String getGoogleMapsApiKey() {
    return googleMapsApiKey;
  }

  public String getGoogleApiClientId() {
    return googleApiClientId;
  }
  
  public Boolean getOdkTablesEnabled() {
    return odkTablesEnabled;
  }
  
}
