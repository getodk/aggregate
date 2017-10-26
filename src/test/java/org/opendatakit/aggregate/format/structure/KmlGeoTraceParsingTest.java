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

package org.opendatakit.aggregate.format.structure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.aggregate.exception.ODKParseException;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.persistence.WrappedBigDecimal;

/**
 *
 * @author wbrunette@gmail.com
 * 
 */


public class KmlGeoTraceParsingTest {

  @Test
  public void testSimpleParse() {
    String coordinate = "1.0 2.0 3.0";
    try {
      List<GeoPoint> points = KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      assertEquals(1, points.size());
      GeoPoint point = points.get(0);
      assertEquals(new WrappedBigDecimal("1.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("2.0"), point.getLongitude());
      assertEquals(new WrappedBigDecimal("3.0"), point.getAltitude());
      assertEquals(null, point.getAccuracy());
    } catch (ODKParseException e) {
      fail();
    }
  }

  @Test
  public void testEmptyString() {
    String coordinate = "";  
    try {
      List<GeoPoint> points = KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      assertTrue(points.isEmpty());
    } catch (ODKParseException e) {
      fail();
    }
  }

  @Test
  public void testNullString() {
    String coordinate = null;
    try {
      List<GeoPoint> points = KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      assertTrue(points.isEmpty());
    } catch (ODKParseException e) {
      fail();
    }
  }

  @Test
  public void testBadCoordinateString() {
    String coordinate = "Hello World!";
    try {
      KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      fail();
    } catch (ODKParseException e) {
      // should throw a parse exception
      assertTrue(true);
    }
  }

  @Test
  public void testOnlyHalfCoordinateString() {
    String coordinate = "1.0 ; 2.0 1.0";
    try {
     KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      fail();
    } catch (ODKParseException e) {
      // should throw a parse exception
      assertTrue(true);
    }
  }
  
  @Test
  public void testTwoCoordinatesSimpleParse() {
    String coordinate = "1.0 2.0 3.0; -1.0 -2.0 0.0";
    try {
      List<GeoPoint> points = KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      assertEquals(2, points.size());
      GeoPoint point = points.get(0);
      assertEquals(new WrappedBigDecimal("1.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("2.0"), point.getLongitude());
      assertEquals(new WrappedBigDecimal("3.0"), point.getAltitude());
      point = points.get(1);
      assertEquals(new WrappedBigDecimal("-1.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("-2.0"), point.getLongitude());
      assertEquals(new WrappedBigDecimal("0.0"), point.getAltitude());
    } catch (ODKParseException e) {
      fail();
    }
  }

  @Test
  public void testThreeCoordinatesSimpleParse() {
    String coordinate = "1.0 2.0 3.0; -1.0 -2.0 0.0;10 20 0";
    try {
      List<GeoPoint> points = KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      assertEquals(3, points.size());
      GeoPoint point = points.get(0);
      assertEquals(new WrappedBigDecimal("1.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("2.0"), point.getLongitude());
      assertEquals(new WrappedBigDecimal("3.0"), point.getAltitude());
      point = points.get(1);
      assertEquals(new WrappedBigDecimal("-1.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("-2.0"), point.getLongitude());
      assertEquals(new WrappedBigDecimal("0.0"), point.getAltitude());
      point = points.get(2);
      assertEquals(new WrappedBigDecimal("10"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("20"), point.getLongitude());
      assertEquals(new WrappedBigDecimal("0"), point.getAltitude());
    } catch (ODKParseException e) {
      fail();
    }
  }

  @Test
  public void testNoAltParse() {
    String coordinate = "1.0 2.0";
    try {
      List<GeoPoint> points = KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      assertEquals(1, points.size());
      GeoPoint point = points.get(0);
      assertEquals(new WrappedBigDecimal("1.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("2.0"), point.getLongitude());
      assertEquals(null, point.getAltitude());
      assertEquals(null, point.getAccuracy());
    } catch (ODKParseException e) {
      fail();
    }
  }

  @Test
  public void testNoAltTwoCoordinatesParse() {
    String coordinate = "1.0 2.0 ; -1.0 -2.0 ";
    try {
      List<GeoPoint> points = KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      assertEquals(2, points.size());
      GeoPoint point = points.get(0);
      assertEquals(new WrappedBigDecimal("1.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("2.0"), point.getLongitude());
      assertEquals(null, point.getAltitude());
      assertEquals(null, point.getAccuracy());
      point = points.get(1);
      assertEquals(new WrappedBigDecimal("-1.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("-2.0"), point.getLongitude());
      assertEquals(null, point.getAltitude());
      assertEquals(null, point.getAccuracy());
    } catch (ODKParseException e) {
      fail();
    }
  }

  @Test
  public void testNoAltThreeCoordinatesParse() {
    String coordinate = "97.0 -5; -10.0 -2.0;100 20 ";
    try {
      List<GeoPoint> points = KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      assertEquals(3, points.size());
      GeoPoint point = points.get(0);
      assertEquals(new WrappedBigDecimal("97.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("-5"), point.getLongitude());
      assertEquals(null, point.getAltitude());
      assertEquals(null, point.getAccuracy());
      point = points.get(1);
      assertEquals(new WrappedBigDecimal("-10.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("-2.0"), point.getLongitude());
      assertEquals(null, point.getAltitude());
      assertEquals(null, point.getAccuracy());
      point = points.get(2);
      assertEquals(new WrappedBigDecimal("100"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("20"), point.getLongitude());
      assertEquals(null, point.getAltitude());
      assertEquals(null, point.getAccuracy());
    } catch (ODKParseException e) {
      fail();
    }
  }

  @Test
  public void testWAccuracyParse() {
    String coordinate = "3 7.0 13 0.0";
    try {
      List<GeoPoint> points = KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      assertEquals(1, points.size());
      GeoPoint point = points.get(0);
      assertEquals(new WrappedBigDecimal("3"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("7.0"), point.getLongitude());
      assertEquals(new WrappedBigDecimal("13"), point.getAltitude());
      assertEquals(new WrappedBigDecimal("0.0"), point.getAccuracy());
    } catch (ODKParseException e) {
      fail();
    }
  }

  @Test
  public void testWAccuracyTwoCoordinatesParse() {
    String coordinate = "1.0 2.0 30 50; -1.0 -2.0 300 0";
    try {
      List<GeoPoint> points = KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      assertEquals(2, points.size());
      GeoPoint point = points.get(0);
      assertEquals(new WrappedBigDecimal("1.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("2.0"), point.getLongitude());
      assertEquals(new WrappedBigDecimal("30"), point.getAltitude());
      assertEquals(new WrappedBigDecimal("50"), point.getAccuracy());
      point = points.get(1);
      assertEquals(new WrappedBigDecimal("-1.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("-2.0"), point.getLongitude());
      assertEquals(new WrappedBigDecimal("300"), point.getAltitude());
      assertEquals(new WrappedBigDecimal("0"), point.getAccuracy());
    } catch (ODKParseException e) {
      fail();
    }
  }

  @Test
  public void testWAccuracyThreeCoordinatesParse() {
    String coordinate = "91.0 -50. 14 1; -10.0 -2.0 1 40;100 20 33 55";
    try {
      List<GeoPoint> points = KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates(coordinate);
      assertEquals(3, points.size());
      GeoPoint point = points.get(0);
      assertEquals(new WrappedBigDecimal("91.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("-50."), point.getLongitude());
      assertEquals(new WrappedBigDecimal("14"), point.getAltitude());
      assertEquals(new WrappedBigDecimal("1"), point.getAccuracy());
      point = points.get(1);
      assertEquals(new WrappedBigDecimal("-10.0"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("-2.0"), point.getLongitude());
      assertEquals(new WrappedBigDecimal("1"), point.getAltitude());
      assertEquals(new WrappedBigDecimal("40"), point.getAccuracy());
      point = points.get(2);
      assertEquals(new WrappedBigDecimal("100"), point.getLatitude());
      assertEquals(new WrappedBigDecimal("20"), point.getLongitude());
      assertEquals(new WrappedBigDecimal("33"), point.getAltitude());
      assertEquals(new WrappedBigDecimal("55"), point.getAccuracy());
    } catch (ODKParseException e) {
      fail();
    }
  }

  
}
