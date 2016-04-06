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

public final class KmlGeopointOption implements Serializable {

  /**
   * Serialization Version Identifier
   */
  private static final long serialVersionUID = 3811510683079735416L;

  private KmlOptionSetting geoElement;
  private ArrayList<KmlOptionSetting> binaryNodes = new ArrayList<KmlOptionSetting>();
  private ArrayList<KmlOptionSetting> titleNodes = new ArrayList<KmlOptionSetting>();
  
//default constructor required
  public KmlGeopointOption(){
    
  }
  
  public KmlGeopointOption(String displayName, String elementKey) {
    this.geoElement = new KmlOptionSetting(displayName, elementKey);
  }

  public KmlOptionSetting getGeoElement() {
    return geoElement;
  }
  
  public void addTitleNode(String displayName, String elementKey) {
    KmlOptionSetting node = new KmlOptionSetting(displayName, elementKey);
    titleNodes.add(node);
  }
  
  public void addBinaryNode(String displayName, String elementKey) {
    KmlOptionSetting node = new KmlOptionSetting(displayName, elementKey);
    binaryNodes.add(node);
  }
  
  public ArrayList<KmlOptionSetting> getBinaryNodes() {
    return binaryNodes;
  }

  public ArrayList<KmlOptionSetting> getTitleNodes() {
    return titleNodes;
  }

}
