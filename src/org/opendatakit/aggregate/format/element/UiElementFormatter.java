package org.opendatakit.aggregate.format.element;

import java.util.Map;

import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.server.GeopointHeaderIncludes;
import org.opendatakit.aggregate.submission.type.GeoPoint;

public class UiElementFormatter extends BasicElementFormatter {

  private Map<String, GeopointHeaderIncludes> gpsFormatters;

  /**
   * Construct a UI Element Formatter
   */
  public UiElementFormatter(Map<String, GeopointHeaderIncludes> gpsFormatter) {
    super(false, false, false);
    this.gpsFormatters = gpsFormatter;
  }

  @Override
  public void formatGeoPoint(GeoPoint coordinate, String propertyName, Row row) {
    GeopointHeaderIncludes gpsFormatter = null;

    if(gpsFormatters != null) {
      gpsFormatter = gpsFormatters.get(propertyName);
    }
  
    if (gpsFormatter == null) {
      basicStringConversion(coordinate.getLatitude(), row);
      basicStringConversion(coordinate.getLongitude(), row);
      basicStringConversion(coordinate.getAltitude(), row);
      basicStringConversion(coordinate.getAccuracy(), row);
    } else {
      if (gpsFormatter.includeLatitude()) {
        basicStringConversion(coordinate.getLatitude(), row);
      }

      if (gpsFormatter.includeLongitude()) {
        basicStringConversion(coordinate.getLongitude(), row);
      }

      if (gpsFormatter.includeAltitude()) {
        basicStringConversion(coordinate.getAltitude(), row);
      }

      if (gpsFormatter.includeAccuracy()) {
        basicStringConversion(coordinate.getAccuracy(), row);
      }
    }
  }
}
