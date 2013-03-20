package org.opendatakit.aggregate.client.preferences;

import java.io.Serializable;

public class PreferenceSummary implements Serializable{

  private static final long serialVersionUID = -5344882762820967969L;

  private String googleSimpleApiKey;

  private String googleApiClientId;

  private Boolean odkTablesEnabled;

  private Boolean fasterPublishingEnabled;

  public PreferenceSummary() {

  }

  public PreferenceSummary(String googleSimpleApiKey, String googleApiClientId, Boolean odkTablesEnabled, Boolean fasterPublishingEnabled) {
    this.googleSimpleApiKey = googleSimpleApiKey;
    this.googleApiClientId = googleApiClientId;
    this.odkTablesEnabled = odkTablesEnabled;
    this.fasterPublishingEnabled = fasterPublishingEnabled;
  }

  public String getGoogleSimpleApiKey() {
    return googleSimpleApiKey;
  }

  public String getGoogleApiClientId() {
    return googleApiClientId;
  }

  public Boolean getOdkTablesEnabled() {
    return odkTablesEnabled;
  }

  public Boolean getFasterPublishingEnabled() {
    return fasterPublishingEnabled;
  }

}
