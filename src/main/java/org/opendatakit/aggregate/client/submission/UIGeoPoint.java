package org.opendatakit.aggregate.client.submission;

import java.io.Serializable;

public class UIGeoPoint implements Serializable {
 
  private static final long serialVersionUID = 1923310609645393319L;
  private String latitude;
  private String longitude;
  
  public UIGeoPoint() {
    
  }
  
  public UIGeoPoint(String lat, String lon) {
    this.latitude = lat;
    this.longitude = lon;
  }

  public String getLatitude() {
    return latitude;
  }

  public String getLongitude() {
    return longitude;
  }
  
}
