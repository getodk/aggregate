package org.opendatakit.aggregate.client.preferences;

import java.io.Serializable;

public class PreferenceSummary implements Serializable{

  private static final long serialVersionUID = -5344882762820967969L;

  private String googleMapsApiKey;
  
  private Boolean odkTablesEnabled;
  
  public PreferenceSummary() {
    
  }
  
  public PreferenceSummary(String googleMapsApiKey, Boolean odkTablesEnabled) {
    this.googleMapsApiKey = googleMapsApiKey;
    this.odkTablesEnabled = odkTablesEnabled;
  }

  public String getGoogleMapsApiKey() {
    return googleMapsApiKey;
  }

  public Boolean getOdkTablesEnabled() {
    return odkTablesEnabled;
  }
  
}
