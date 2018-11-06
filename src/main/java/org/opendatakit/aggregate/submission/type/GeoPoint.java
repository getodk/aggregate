/*
 * Copyright (C) 2009 Google Inc.
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.submission.type;

import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class GeoPoint {

  // gps coordinate constants
  public static final String GEO_POINT = "GeoPoint";
  public static final String LATITUDE = "Latitude";
  public static final String LONGITUDE = "Longitude";
  public static final String ALTITUDE = "Altitude";
  public static final String ACCURACY = "Accuracy";

  private WrappedBigDecimal latitude;

  private WrappedBigDecimal longitude;

  private WrappedBigDecimal altitude;

  private WrappedBigDecimal accuracy;

  public GeoPoint(WrappedBigDecimal latitudeCoordinate, WrappedBigDecimal longitudeCoordinate, WrappedBigDecimal altitudeValue, WrappedBigDecimal accuracyValue) {
    latitude = latitudeCoordinate;
    longitude = longitudeCoordinate;
    altitude = altitudeValue;
    accuracy = accuracyValue;
  }

  public GeoPoint(WrappedBigDecimal latitudeCoordinate, WrappedBigDecimal longitudeCoordinate, WrappedBigDecimal altitudeValue) {
    this(latitudeCoordinate, longitudeCoordinate, altitudeValue, null);
  }

  public GeoPoint(WrappedBigDecimal latitudeCoordinate, WrappedBigDecimal longitudeCoordinate) {
    this(latitudeCoordinate, longitudeCoordinate, null, null);
  }

  public GeoPoint() {
    this(null, null, null, null);
  }

  public WrappedBigDecimal getLatitude() {
    return latitude;
  }

  public WrappedBigDecimal getLongitude() {
    return longitude;
  }

  public WrappedBigDecimal getAltitude() {
    return altitude;
  }

  public WrappedBigDecimal getAccuracy() {
    return accuracy;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GeoPoint)) {
      return false;
    }
    // super will compare value
    if (!super.equals(obj)) {
      return false;
    }

    GeoPoint other = (GeoPoint) obj;
    return (latitude == null ? (other.latitude == null) : (latitude.equals(other.latitude)))
        && (longitude == null ? (other.longitude == null) : (longitude.equals(other.longitude)))
        && (altitude == null ? (other.altitude == null) : (altitude.equals(other.altitude)))
        && (accuracy == null ? (other.accuracy == null) : (accuracy.equals(other.accuracy)));
  }

  @Override
  public int hashCode() {
    int hashCode = 11;
    if (latitude != null)
      hashCode += latitude.hashCode();
    if (longitude != null)
      hashCode += longitude.hashCode();
    if (altitude != null)
      hashCode += altitude.hashCode();
    if (accuracy != null)
      hashCode += accuracy.hashCode();
    return hashCode;
  }

  @Override
  public String toString() {
    String str = GeoPoint.GEO_POINT + BasicConsts.COLON + BasicConsts.SPACE;
    if (latitude != null) {
      str += GeoPoint.LATITUDE + BasicConsts.COLON + latitude + BasicConsts.SPACE;
    }
    if (longitude != null) {
      str += GeoPoint.LATITUDE + BasicConsts.COLON + longitude + BasicConsts.SPACE;
    }
    if (altitude != null) {
      str += GeoPoint.ALTITUDE + BasicConsts.COLON + altitude + BasicConsts.SPACE;
    }
    if (accuracy != null) {
      str += GeoPoint.ACCURACY + BasicConsts.COLON + accuracy + BasicConsts.SPACE;
    }

    return str;
  }
}
