/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.client.form;

import java.io.Serializable;
import java.util.ArrayList;

public final class KmlSettings implements Serializable {

  /**
   * Serialization Version Identifier
   */
  private static final long serialVersionUID = 3805106830794715416L;
  
  private ArrayList<KmlSettingOption> geopointNodes = new ArrayList<KmlSettingOption>();
  private ArrayList<KmlSettingOption> binaryNodes = new ArrayList<KmlSettingOption>();
  private ArrayList<KmlSettingOption> titleNodes = new ArrayList<KmlSettingOption>();
  
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
  
  public ArrayList<KmlSettingOption> getGeopointNodes() {
    return geopointNodes;
  }

  public ArrayList<KmlSettingOption> getBinaryNodes() {
    return binaryNodes;
  }

  public ArrayList<KmlSettingOption> getTitleNodes() {
    return titleNodes;
  }

}
