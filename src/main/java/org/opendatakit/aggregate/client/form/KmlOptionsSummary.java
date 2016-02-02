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

public class KmlOptionsSummary implements Serializable {

  /**
   * Serialization Version Identifier
   */
  private static final long serialVersionUID = 3805106830794715416L;

  private ArrayList<KmlGeopointOption> geopointOptions = new ArrayList<KmlGeopointOption>();
  private ArrayList<KmlGeoTraceNShapeOption> geoTraceNShapeOptions = new ArrayList<KmlGeoTraceNShapeOption>();

  public ArrayList<KmlGeopointOption> getGeopointOptions() {
    return geopointOptions;
  }

  public void addGeopointOptions(KmlGeopointOption geopointNode) {
    geopointOptions.add(geopointNode);
  }

  public ArrayList<KmlGeoTraceNShapeOption> getGeoTraceNShapeOptions() {
    return geoTraceNShapeOptions;
  }

  public void addGeoTraceNShapeOptions(KmlGeoTraceNShapeOption geoTraceNShapeNode) {
    geoTraceNShapeOptions.add(geoTraceNShapeNode);
  }

}
