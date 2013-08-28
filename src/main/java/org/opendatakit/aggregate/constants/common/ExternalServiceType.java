/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.constants.common;

import java.io.Serializable;


/**
 * Enum of all the available external services. Provides UI and type mapping
 * information
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public enum ExternalServiceType implements Serializable {
  GOOGLE_SPREADSHEET("Google Spreadsheet"),
  JSON_SERVER("Z-ALPHA JSON Server"),
  OHMAGE_JSON_SERVER("Z-ALPHA Ohmage JSON Server"),
  GOOGLE_FUSIONTABLES( "Google FusionTables"),
  REDCAP_SERVER("Z-ALPHA REDCap Server"),
  GOOGLE_MAPS_ENGINE("Z-ALPHA Google Maps Engine");

  private String serviceName;

  private ExternalServiceType() {
    // GWT
  }

  private ExternalServiceType(String name) {
    serviceName = name;
  }
  public String getDisplayText() {
    return serviceName;
  }

  public String toString() {
    return serviceName;
  }

}