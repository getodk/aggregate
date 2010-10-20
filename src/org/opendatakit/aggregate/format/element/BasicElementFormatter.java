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
package org.opendatakit.aggregate.format.element;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

public class BasicElementFormatter implements ElementFormatter {

  /**
   * separate the GPS coordinates of latitude and longitude into columns
   */
  private boolean separateCoordinates;

  /**
   * include GPS altitude data
   */
  private boolean includeAltitude;

  /**
   * include GPS accuracy data
   */
  private boolean includeAccuracy;

  /**
   * Construct a Basic Element Formatter
   * 
   * @param separateGpsCoordinates
   *          separate the GPS coordinates of latitude and longitude into
   *          columns
   * @param includeGpsAltitude
   *          include GPS altitude data
   * @param includeGpsAccuracy
   *          include GPS accuracy data
   */
  public BasicElementFormatter(boolean separateGpsCoordinates, boolean includeGpsAltitude,
      boolean includeGpsAccuracy) {
    separateCoordinates = separateGpsCoordinates;
    includeAltitude = includeGpsAltitude;
    includeAccuracy = includeGpsAccuracy;
  }

  public void formatBinary(SubmissionKey key, String propertyName, Row row) throws ODKDatastoreException {
    basicStringConverstion(key.toString(), row);
  }

  public void formatBoolean(Boolean bool, String propertyName, Row row) {
    basicStringConverstion(bool, row);
  }

  public void formatChoices(List<String> choices, String propertyName, Row row) {
	StringBuilder b = new StringBuilder();

	boolean first = true;
	for ( String s : choices ) {
		if ( !first ) {
			b.append(" ");
		}
		first = false;
		b.append(s);
	}
	basicStringConverstion(b.toString(), row);
  }

  public void formatDate(Date date, String propertyName, Row row) {
    basicStringConverstion(date, row);
  }

  public void formatDecimal(BigDecimal dub, String propertyName, Row row) {
    basicStringConverstion(dub, row);
  }

  public void formatGeoPoint(GeoPoint coordinate, String propertyName, Row row) {
    if (separateCoordinates) {
      basicStringConverstion(coordinate.getLatitude(), row);
      basicStringConverstion(coordinate.getLongitude(), row);

      if (includeAltitude) {
        basicStringConverstion(coordinate.getAltitude(), row);
      }

      if (includeAccuracy) {
        basicStringConverstion(coordinate.getAccuracy(), row);
      }
    } else {
      if (coordinate.getLongitude() != null && coordinate.getLatitude() != null) {
        String coordVal = coordinate.getLatitude().toString() + BasicConsts.COMMA
            + BasicConsts.SPACE + coordinate.getLongitude().toString();
        if (includeAltitude) {
        	coordVal += BasicConsts.COMMA
            + BasicConsts.SPACE + coordinate.getAltitude().toString();
        }
        if (includeAccuracy) {
        	coordVal += BasicConsts.COMMA
            + BasicConsts.SPACE + coordinate.getAccuracy().toString();
        }
        row.addFormattedValue(coordVal);
      } else {
        row.addFormattedValue(null);
      }
    }
  }

  public void formatLong(Long longInt, String propertyName, Row row) {
    basicStringConverstion(longInt, row);
  }

  public void formatString(String string, String propertyName, Row row) {
    basicStringConverstion(string, row);
  }

  public void formatRepeats(SubmissionRepeat repeat, String propertyName, Row row) throws ODKDatastoreException {
    // TODO: decide how to handle in basic case
  }

  private void basicStringConverstion(Object value, Row row) {
    if (value != null) {
      row.addFormattedValue(value.toString());
    } else {
      row.addFormattedValue(null);
    }
  }
}
