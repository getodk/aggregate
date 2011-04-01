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

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlUtil;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class XmlElementFormatter implements ElementFormatter {

  /**
   * Construct a XML Element Formatter
   * 
   */
  public XmlElementFormatter() {
  }

  @Override
  public void formatUid(String uri, String propertyName, Row row) {
    // unneeded so unimplemented
  }

  @Override
  public void formatBinary(BlobSubmissionType blobSubmission, FormElementModel element, String ordinalValue,
      Row row, CallingContext cc) throws ODKDatastoreException {

  }

  @Override
  public void formatBoolean(Boolean bool, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(bool, element.getElementName(), row);

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
    addToXmlValueToRow(b.toString(), element.getElementName(), row);
  }

  @Override
  public void formatDate(Date date, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(date, element.getElementName(), row);

  }

  @Override
  public void formatDateTime(Date date, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(date, element.getElementName(), row);

  }

  @Override
  public void formatTime(Date date, FormElementModel element, String ordinalValue, Row row) {
    if (date != null) {
      GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
      g.setTime(date);
      addToXmlValueToRow(
          String.format(FormatConsts.TIME_FORMAT_STRING, g.get(Calendar.HOUR_OF_DAY),
              g.get(Calendar.MINUTE), g.get(Calendar.SECOND)), element.getElementName(), row);
    } else {
      addToXmlValueToRow(null, element.getElementName(), row);
    }
  }

  @Override
  public void formatDecimal(BigDecimal dub, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(dub, element.getElementName(), row);

  }

  @Override
  public void formatGeoPoint(GeoPoint coordinate, FormElementModel element, String ordinalValue, Row row) {
    if (coordinate.getLongitude() != null && coordinate.getLatitude() != null) {
      String coordVal = coordinate.getLatitude().toString() + BasicConsts.COMMA + BasicConsts.SPACE
          + coordinate.getLongitude().toString();
      if (coordinate.getAltitude() != null) {
        coordVal += BasicConsts.COMMA + BasicConsts.SPACE + coordinate.getAltitude().toString();
      } else {
        coordVal += BasicConsts.COMMA + BasicConsts.SPACE + "0.0";
      }
      if (coordinate.getAccuracy() != null) {
        coordVal += BasicConsts.COMMA + BasicConsts.SPACE + coordinate.getAccuracy().toString();
      } else {
        coordVal += BasicConsts.COMMA + BasicConsts.SPACE + "0.0";
      }
      addToXmlValueToRow(coordVal, element.getElementName(), row);
    } else {
      addToXmlValueToRow(null, element.getElementName(), row);
    }

  }

  @Override
  public void formatLong(Long longInt, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(longInt, element.getElementName(), row);
  }

  @Override
  public void formatRepeats(SubmissionRepeat repeat, FormElementModel repeatElement, Row row,
      CallingContext cc) throws ODKDatastoreException {
    // TODO: figure out how to deal with repeat
  }

  @Override
  public void formatString(String string, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(string, element.getElementName(), row);
  }

  private void addToXmlValueToRow(Object value, String propertyName, Row row) {
    String xmlString = HtmlUtil.createBeginTag(propertyName);
    
    if (value != null) {
      xmlString += value.toString();
    } else {
      xmlString += BasicConsts.EMPTY_STRING;
    }
    xmlString += HtmlUtil.createEndTag(propertyName);
    
    row.addFormattedValue(xmlString);
  }

}
