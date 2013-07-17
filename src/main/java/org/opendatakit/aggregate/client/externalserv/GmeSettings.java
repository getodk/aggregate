package org.opendatakit.aggregate.client.externalserv;

import java.io.Serializable;
import java.util.ArrayList;

public class GmeSettings implements Serializable {

  /**
   * Serialization Version Identifier
   */
  private static final long serialVersionUID = 5329486667683600748L;
  
  private ArrayList<GeoPointElement> geoPoints;

  private String gmeAssetId;
  
  public GmeSettings() {
    geoPoints = new ArrayList<GeoPointElement>();
  }
  
  public String getGmeAssetId() {
    return gmeAssetId;
  }


  public void setGmeAssetId(String gmeAssetId) {
    this.gmeAssetId = gmeAssetId;
  }


  public void addGeoPoint(String displayName, String elementKey) {
    GeoPointElement geoPnt = new GeoPointElement(displayName, elementKey);
    geoPoints.add(geoPnt);
  }
  
  public ArrayList<GeoPointElement> getPossibleGeoPoints() {
    return geoPoints;
  }
}
