/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.submission.type;

import org.odk.aggregate.constants.BasicConsts;

public class GeoPoint {

  private Double latitude;

  private Double longitude;
  
  /**
   * Constructs a GeoPoint with given latitude and longitude coordinates
   * 
   * @param latitudeCoordinate
   * @param longitudeCoordinate
   */
  public GeoPoint(Double latitudeCoordinate, Double longitudeCoordinate) {
    latitude = latitudeCoordinate;
    longitude = longitudeCoordinate;
  }
  
  /**
   * Returns the latitude of the GeoPoint
   * 
   * @return
   *    the latitude
   */
  public Double getLatitude() {
    return latitude;
  }

  /**
   * Returns the longitude of the GeoPoint
   * 
   * @return
   *    the longitude
   */
  public Double getLongitude() {
    return longitude;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return BasicConsts.LATITUDE + BasicConsts.COLON + latitude + BasicConsts.SPACE + BasicConsts.LONGITUDE + BasicConsts.COLON + longitude;
  }
}
