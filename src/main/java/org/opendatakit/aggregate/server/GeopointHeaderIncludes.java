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

package org.opendatakit.aggregate.server;

public class GeopointHeaderIncludes {
  private String elementName;
  private boolean includeLatitude;
  private boolean includeLongitude;
  private boolean includeAltitude;
  private boolean includeAccuracy;

  public GeopointHeaderIncludes(String elementName, boolean latitude, boolean longitude,
                                boolean altitude, boolean accuracy) {
    this.elementName = elementName;
    this.includeLatitude = latitude;
    this.includeLongitude = longitude;
    this.includeAltitude = altitude;
    this.includeAccuracy = accuracy;
  }

  public boolean includeLatitude() {
    return includeLatitude;
  }

  public boolean includeLongitude() {
    return includeLongitude;
  }

  public boolean includeAltitude() {
    return includeAltitude;
  }

  public boolean includeAccuracy() {
    return includeAccuracy;
  }

}
