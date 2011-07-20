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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

import com.google.appengine.repackaged.com.google.common.util.Base64;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class JsonElementFormatter implements ElementFormatter {
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
   * Construct a JSON Element Formatter
   * 
   * @param separateGpsCoordinates
   *          separate the GPS coordinates of latitude and longitude into
   *          columns
   * @param includeGpsAltitude
   *          include GPS altitude data
   * @param includeGpsAccuracy
   *          include GPS accuracy data
   */
  public JsonElementFormatter(boolean separateGpsCoordinates, boolean includeGpsAltitude,
      boolean includeGpsAccuracy) {
    separateCoordinates = separateGpsCoordinates;
    includeAltitude = includeGpsAltitude;
    includeAccuracy = includeGpsAccuracy;
  }

  @Override
  public void formatUid(String uri, String propertyName, Row row) {
    // unneeded so unimplemented
  }
  
  @Override
  public void formatBinary(BlobSubmissionType blobSubmission, FormElementModel element, String ordinalValue, Row row, CallingContext cc)
      throws ODKDatastoreException {
    if (blobSubmission == null || (blobSubmission.getAttachmentCount() == 0)) {
      row.addFormattedValue(null);
      return;
    }

    byte[] imageBlob = null;
    if (blobSubmission.getAttachmentCount() == 1) {
      imageBlob = blobSubmission.getBlob(1, cc);
    }
    if (imageBlob != null && imageBlob.length > 0) {
      addToJsonValueToRow(Base64.encode(imageBlob), element.getElementName(), row);
    }

  }

  @Override
  public void formatBoolean(Boolean bool, FormElementModel element, String ordinalValue, Row row) {
    addToJsonValueToRow(bool, element.getElementName(), row);
  }

  @Override
  public void formatChoices(List<String> choices, FormElementModel element, String ordinalValue, Row row) {
    StringBuilder b = new StringBuilder();

    boolean first = true;
    for (String s : choices) {
      if (!first) {
        b.append(" ");
      }
      first = false;
      b.append(s);
    }
    addToJsonValueToRow(b.toString(), element.getElementName(), row);
  }

  @Override
  public void formatDate(Date date, FormElementModel element, String ordinalValue, Row row) {
    addToJsonValueToRow(date, element.getElementName(), row);

  }

  @Override
  public void formatDateTime(Date date, FormElementModel element, String ordinalValue, Row row) {
    addToJsonValueToRow(date, element.getElementName(), row);

  }

  @Override
  public void formatTime(Date date, FormElementModel element, String ordinalValue, Row row) {
    if (date != null) {
    	GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    	g.setTime(date);
    	addToJsonValueToRow(String.format(FormatConsts.TIME_FORMAT_STRING, 
        									g.get(Calendar.HOUR_OF_DAY), 
        									g.get(Calendar.MINUTE), 
        									g.get(Calendar.SECOND)),
        									element.getElementName(), row);
    } else {
    	addToJsonValueToRow(null, element.getElementName(), row);
    }
  }

  @Override
  public void formatDecimal(BigDecimal dub, FormElementModel element, String ordinalValue, Row row) {
    addToJsonValueToRow(dub, element.getElementName(), row);

  }

  @Override
  public void formatGeoPoint(GeoPoint coordinate, FormElementModel element, String ordinalValue, Row row) {
    if (separateCoordinates) {
      addToJsonValueToRow(coordinate.getLatitude(), element.getElementName() + FormatConsts.HEADER_CONCAT
          + GeoPoint.LATITUDE, row);
      addToJsonValueToRow(coordinate.getLongitude(), element.getElementName() + FormatConsts.HEADER_CONCAT
          + GeoPoint.LONGITUDE, row);

      if (includeAltitude) {
        addToJsonValueToRow(coordinate.getAltitude(), element.getElementName() + FormatConsts.HEADER_CONCAT
            + GeoPoint.ALTITUDE, row);
      }

      if (includeAccuracy) {
        addToJsonValueToRow(coordinate.getAccuracy(), element.getElementName() + FormatConsts.HEADER_CONCAT
            + GeoPoint.ACCURACY, row);
      }
    } else {
      if (coordinate.getLongitude() != null && coordinate.getLatitude() != null) {
        String coordVal = coordinate.getLatitude().toString() + BasicConsts.COMMA
            + BasicConsts.SPACE + coordinate.getLongitude().toString();
        addToJsonValueToRow(coordVal, element.getElementName(), row);
        if (includeAltitude) {
          addToJsonValueToRow(coordinate.getAltitude().toString(), element.getElementName() + FormatConsts.HEADER_CONCAT
              + GeoPoint.ALTITUDE, row);
        }
        if (includeAccuracy) {
          addToJsonValueToRow(coordinate.getAccuracy().toString(), element.getElementName() + FormatConsts.HEADER_CONCAT
              + GeoPoint.ACCURACY, row);
        }
      } else {
        addToJsonValueToRow(null, element.getElementName(), row);
      }
    }

  }

  @Override
  public void formatLong(Long longInt, FormElementModel element, String ordinalValue, Row row) {
    addToJsonValueToRow(longInt, element.getElementName(), row);
  }

  @Override
  public void formatRepeats(SubmissionRepeat repeat, FormElementModel repeatElement, Row row, CallingContext cc)
      throws ODKDatastoreException {
    // TODO: figure out how to deal with repeat
  }

  @Override
  public void formatString(String string, FormElementModel element, String ordinalValue, Row row) {
    addToJsonValueToRow(string, element.getElementName(), row);
  }

  private void addToJsonValueToRow(Object value, String propertyName, Row row) {
    StringBuilder jsonString = new StringBuilder();
    jsonString.append(BasicConsts.QUOTE);
    jsonString.append(propertyName);
    jsonString.append(BasicConsts.QUOTE + BasicConsts.COLON);

    if (value != null) {
      jsonString.append(BasicConsts.QUOTE);
      jsonString.append(value.toString());
      jsonString.append(BasicConsts.QUOTE);
    } else {
      jsonString.append(BasicConsts.EMPTY_STRING);
    }

    row.addFormattedValue(jsonString.toString());
  }

}
