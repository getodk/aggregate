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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
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
   * Format dates appropriately for googleDocs
   */
  private boolean googleDocsDate;
  
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
      boolean includeGpsAccuracy, boolean googleDocsDate) {
    separateCoordinates = separateGpsCoordinates;
    includeAltitude = includeGpsAltitude;
    includeAccuracy = includeGpsAccuracy;
    this.googleDocsDate = googleDocsDate;
  }

  public void formatUid(String uri, String propertyName, Row row) {
    basicStringConversion(uri, row);
  }
  
  public void formatBinary(BlobSubmissionType blobSubmission, FormElementModel element, String ordinalValue, Row row, CallingContext cc) throws ODKDatastoreException {
	  SubmissionKey key = blobSubmission.getValue();
	  basicStringConversion(key.toString(), row);
  }

  public void formatBoolean(Boolean bool, FormElementModel element, String ordinalValue, Row row) {
    basicStringConversion(bool, row);
  }

  public void formatChoices(List<String> choices, FormElementModel element, String ordinalValue, Row row) {
	StringBuilder b = new StringBuilder();

	boolean first = true;
	for ( String s : choices ) {
		if ( !first ) {
			b.append(BasicConsts.SPACE);
		}
		first = false;
		b.append(s);
	}
	basicStringConversion(b.toString(), row);
  }

  public void formatDate(Date date, FormElementModel element, String ordinalValue, Row row) {
    if ( googleDocsDate ) {
      basicStringConversion( WebUtils.googleDocsDateOnly(date), row);  
    } else {
      basicStringConversion(date, row);
    }
  }

  public void formatDateTime(Date date, FormElementModel element, String ordinalValue, Row row) {
    if ( googleDocsDate ) {
      basicStringConversion( WebUtils.googleDocsDateTime(date), row);  
    } else {
      basicStringConversion(date, row);
    }
  }

  public void formatTime(Date date, FormElementModel element, String ordinalValue, Row row) {
    if (date != null) {
    	GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    	g.setTime(date);
        row.addFormattedValue(String.format(FormatConsts.TIME_FORMAT_STRING, 
							        		g.get(Calendar.HOUR_OF_DAY), 
							        		g.get(Calendar.MINUTE),
							        		g.get(Calendar.SECOND)));
    } else {
        row.addFormattedValue(null);
    }
  }

  public void formatDecimal(WrappedBigDecimal dub, FormElementModel element, String ordinalValue, Row row) {
    formatBigDecimalToString(dub, row);
  }

  public void formatGeoPoint(GeoPoint coordinate, FormElementModel element, String ordinalValue, Row row) {
    if (separateCoordinates) {
      basicStringConversion(coordinate.getLatitude(), row);
      basicStringConversion(coordinate.getLongitude(), row);

      if (includeAltitude) {
        basicStringConversion(coordinate.getAltitude(), row);
      }

      if (includeAccuracy) {
        basicStringConversion(coordinate.getAccuracy(), row);
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

  public void formatLong(Long longInt, FormElementModel element, String ordinalValue, Row row) {
    basicStringConversion(longInt, row);
  }

  public void formatString(String string, FormElementModel element, String ordinalValue, Row row) {
    basicStringConversion(string, row);
  }

  public void formatRepeats(SubmissionRepeat repeat, FormElementModel repeatElement, Row row, CallingContext cc) throws ODKDatastoreException {
    basicStringConversion(repeat.getUniqueKeyStr(), row);
  }

  protected void basicStringConversion(Object value, Row row) {
    if (value != null) {
      row.addFormattedValue(value.toString());
    } else {
      row.addFormattedValue(null);
    }
  }

  protected void formatBigDecimalToString(WrappedBigDecimal dub, Row row) {
    basicStringConversion(dub, row);
  }
}
