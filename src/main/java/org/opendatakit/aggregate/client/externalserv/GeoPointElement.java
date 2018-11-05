package org.opendatakit.aggregate.client.externalserv;

import java.io.Serializable;

public class GeoPointElement implements Serializable {

  /**
   * Serialization Version Identifier
   */
  private static final long serialVersionUID = -727326854516637501L;

  private String displayName;
  private String elementKey;

  public GeoPointElement() {
  }

  public GeoPointElement(String displayName, String elementKey) {
    this.setDisplayName(displayName);
    this.setElementKey(elementKey);
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getElementKey() {
    return elementKey;
  }

  public void setElementKey(String elementKey) {
    this.elementKey = elementKey;
  }

}
