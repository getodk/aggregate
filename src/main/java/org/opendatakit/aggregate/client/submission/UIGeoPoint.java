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

package org.opendatakit.aggregate.client.submission;


import java.io.Serializable;

public class UIGeoPoint implements Serializable {

  private static final long serialVersionUID = 1923310609645393319L;
  private String latitude;
  private String longitude;

  public UIGeoPoint() {

  }

  public UIGeoPoint(String lat, String lon) {
    this.latitude = lat;
    this.longitude = lon;
  }

  public String getLatitude() {
    return latitude;
  }

  public String getLongitude() {
    return longitude;
  }

}
