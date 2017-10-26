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

public class GeopointElementList implements Serializable {
  /**
   * Serialization Version Identifier
   */
  private static final long serialVersionUID = -7472836506605295283L;
  private ArrayList<KmlOptionSetting> geopointElements = new ArrayList<KmlOptionSetting>();

  public void addGeopointElement(String displayName, String elementKey) {
    KmlOptionSetting node = new KmlOptionSetting(displayName, elementKey);
    geopointElements.add(node);
  }

  public ArrayList<KmlOptionSetting> getGeopointElements() {
    return geopointElements;
  }
  
}
