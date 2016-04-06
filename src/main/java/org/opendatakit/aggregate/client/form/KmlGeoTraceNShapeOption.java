/*
 * Copyright (C) 2016 University of Washington
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

public final class KmlGeoTraceNShapeOption implements Serializable {

  /**
   * Serialization Version Identifier
   */
  private static final long serialVersionUID = -682412346245691227L;

  public enum GeoTraceNShapeType {
    GEOTRACE,
    GEOSHAPE;
  }
  
  private GeoTraceNShapeType type;
  private KmlOptionSetting geoElement;
  private ArrayList<KmlOptionSetting> nameNodes = new ArrayList<KmlOptionSetting>();

  // default constructor required
  public KmlGeoTraceNShapeOption() {
    
  }
  
  
  public KmlGeoTraceNShapeOption(GeoTraceNShapeType type, String displayName, String elementKey) {
    this.type = type;
    this.geoElement = new KmlOptionSetting(displayName, elementKey);
  }
  
  public KmlOptionSetting getGeoElement() {
    return geoElement;
  }
  
  public void addNameNode(String displayName, String elementKey) {
    KmlOptionSetting node = new KmlOptionSetting(displayName, elementKey);
    nameNodes.add(node);
  }
  
  public ArrayList<KmlOptionSetting> getNameNodes() {
    return nameNodes;
  }

  public GeoTraceNShapeType getType() {
    return type;
  }

}
