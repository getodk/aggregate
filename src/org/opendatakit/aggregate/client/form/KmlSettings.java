package org.opendatakit.aggregate.client.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KmlSettings implements Serializable {

  /**
   * Serialization Version Identifier
   */
  private static final long serialVersionUID = 3805106830794715416L;
  
  private List<KmlSettingOption> geopointNodes = new ArrayList<KmlSettingOption>();
  private List<KmlSettingOption> binaryNodes = new ArrayList<KmlSettingOption>();
  private List<KmlSettingOption> nodes = new ArrayList<KmlSettingOption>();
  
  public void addNode(String displayName, String elementKey) {
    KmlSettingOption node = new KmlSettingOption(displayName, elementKey);
    nodes.add(node);
  }
  
  public void addGeopointNode(String displayName, String elementKey) {
    KmlSettingOption node = new KmlSettingOption(displayName, elementKey);
    geopointNodes.add(node);
  }
  
  public void addBinaryNode(String displayName, String elementKey) {
    KmlSettingOption node = new KmlSettingOption(displayName, elementKey);
    binaryNodes.add(node);
  }
  
  public List<KmlSettingOption> getGeopointNodes() {
    return geopointNodes;
  }

  public List<KmlSettingOption> getBinaryNodes() {
    return binaryNodes;
  }

  public List<KmlSettingOption> getNodes() {
    return nodes;
  }


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

}
