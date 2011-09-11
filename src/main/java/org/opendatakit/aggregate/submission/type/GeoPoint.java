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

import java.math.BigDecimal;

import org.opendatakit.common.web.constants.BasicConsts;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class GeoPoint {

  // gps coordinate constants
  public static final String GEO_POINT = "GeoPoint";
  public static final String LATITUDE = "Latitude";
  public static final String LONGITUDE = "Longitude";
  public static final String ALTITUDE = "Altitude";
  public static final String ACCURACY = "Accuracy";

  
  private BigDecimal latitude;

  private BigDecimal longitude;
  
  private BigDecimal altitude;
  
  private BigDecimal accuracy;  

  /**
   * Constructs a GeoPoint with given latitude and longitude coordinates
   * 
   * @param latitudeCoordinate
   * @param longitudeCoordinate
   * @param altitudeValue
   * @param accuracyValue
   */
  public GeoPoint(BigDecimal latitudeCoordinate, BigDecimal longitudeCoordinate, BigDecimal altitudeValue, BigDecimal accuracyValue) {
    latitude = latitudeCoordinate;
    longitude = longitudeCoordinate;
    altitude = altitudeValue;
    accuracy = accuracyValue;
  }
  
  /**
   * Constructs a GeoPoint with given latitude and longitude coordinates
   * 
   * @param latitudeCoordinate
   * @param longitudeCoordinate
   * @param altitudeValue
   */
  public GeoPoint(BigDecimal latitudeCoordinate, BigDecimal longitudeCoordinate, BigDecimal altitudeValue) {
    this(latitudeCoordinate, longitudeCoordinate, altitudeValue, null);
  }
  
  /**
   * Constructs a GeoPoint with given latitude and longitude coordinates
   * 
   * @param latitudeCoordinate
   * @param longitudeCoordinate
   */
  public GeoPoint(BigDecimal latitudeCoordinate, BigDecimal longitudeCoordinate) {
    this(latitudeCoordinate, longitudeCoordinate, null, null);
  }
  
  /**
   * Constructs a GeoPoint with all null values.
   */
  public GeoPoint() {
	  this(null, null, null, null);
  }
  
  /**
   * Returns the latitude of the GeoPoint
   * 
   * @return
   *    the latitude
   */
  public BigDecimal getLatitude() {
    return latitude;
  }

  /**
   * Returns the longitude of the GeoPoint
   * 
   * @return
   *    the longitude
   */
  public BigDecimal getLongitude() {
    return longitude;
  }
  
  /**
   * Returns the altitude of the GeoPoint
   * 
   * @return
   *    the altitude
   */
  public BigDecimal getAltitude() {
    return altitude;
  }

  /**
   * Returns the accuracy of the GeoPoint
   * 
   * @return
   *    the accuracy
   */
  public BigDecimal getAccuracy() {
    return accuracy;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
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

  /**
   * @see java.lang.Object#hashCode()
   */
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
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = GeoPoint.GEO_POINT+ BasicConsts.COLON + BasicConsts.SPACE;
    if(latitude != null) {
      str += GeoPoint.LATITUDE + BasicConsts.COLON + latitude + BasicConsts.SPACE; 
    }
    if(longitude != null) {
      str += GeoPoint.LATITUDE + BasicConsts.COLON + longitude + BasicConsts.SPACE; 
    }
    if(altitude != null) {
      str += GeoPoint.ALTITUDE + BasicConsts.COLON + altitude + BasicConsts.SPACE; 
    }
    if(accuracy != null) {
      str += GeoPoint.ACCURACY + BasicConsts.COLON + accuracy + BasicConsts.SPACE; 
    }

    return str;
  }
}
