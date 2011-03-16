package org.opendatakit.aggregate.client.form;

import java.io.Serializable;

public class KmlSettingOption implements Serializable {

  /**
   * Serialization Version Identifier
   */
  private static final long serialVersionUID = -6824246262410791227L;

  private String displayName;
  private String elementKey;

  public KmlSettingOption() {
  }

  public KmlSettingOption(String displayName, String elementKey) {
    this.setDisplayName(displayName);
    this.setElementKey(elementKey);
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setElementKey(String elementKey) {
    this.elementKey = elementKey;
  }

  public String getElementKey() {
    return elementKey;
  }

}