package org.opendatakit.aggregate.server;

public class GeopointHeaderIncludes {
  private String elementName;
  private boolean includeLatitude;
  private boolean includeLongitude;
  private boolean includeAltitude;
  private boolean includeAccuracy;

  public GeopointHeaderIncludes(String elementName, boolean latitude, boolean longitude,
      boolean altitude, boolean accuracy) {
    this.elementName = elementName;
    this.includeLatitude = latitude;
    this.includeLongitude = longitude;
    this.includeAltitude = altitude;
    this.includeAccuracy = accuracy;
  }

  public String getElementName() {
    return elementName;
  }

  public boolean includeLatitude() {
    return includeLatitude;
  }

  public boolean includeLongitude() {
    return includeLongitude;
  }

  public boolean includeAltitude() {
    return includeAltitude;
  }

  public boolean includeAccuracy() {
    return includeAccuracy;
  }

}
