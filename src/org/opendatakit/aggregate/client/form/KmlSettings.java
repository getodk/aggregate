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
  private List<KmlSettingOption> titleNodes = new ArrayList<KmlSettingOption>();
  
  public void addTitleNode(String displayName, String elementKey) {
    KmlSettingOption node = new KmlSettingOption(displayName, elementKey);
    titleNodes.add(node);
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

  public List<KmlSettingOption> getTitleNodes() {
    return titleNodes;
  }

}
