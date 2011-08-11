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
package org.opendatakit.aggregate.format.element;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringEscapeUtils;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * Emits attribute=\"value\" string for a list of properties.
 * Escapes value but does not handle non-XML-compliant attribute names.
 * Used for metadata fields.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class XmlAttributeFormatter implements ElementFormatter {

  /**
   * Construct a XML Element Formatter
   * 
   */
  public XmlAttributeFormatter() {
  }

  private String asAttributeName(FormElementModel m) {
	if ( m.isMetadata() ) {
		switch( m.getType()) {
		case META_MODEL_VERSION:
			return "version";
		case META_UI_VERSION:
			return "uiVersion";
		case META_INSTANCE_ID:
			return "instanceID";
		case META_SUBMISSION_DATE:
			return "submissionDate";
		case META_IS_COMPLETE:
			return "isComplete";
		default:
			throw new IllegalStateException("Unrecognized metadata");
		}
	} else {
		return m.getElementName();
	}
  }
  
  @Override
  public void formatUid(String uri, String propertyName, Row row) {
    // unneeded so unimplemented
  }

  @Override
  public void formatBinary(BlobSubmissionType blobSubmission, FormElementModel element, String ordinalValue,
      Row row, CallingContext cc) throws ODKDatastoreException {
	addToXmlValueToRow(blobSubmission.getUnrootedFilename(0), asAttributeName(element), row);
  }

  @Override
  public void formatBoolean(Boolean bool, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(bool, asAttributeName(element), row);

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
    addToXmlValueToRow(b.toString(), asAttributeName(element), row);
  }

  @Override
  public void formatDate(Date date, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(date, asAttributeName(element), row);

  }

  @Override
  public void formatDateTime(Date date, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(date, asAttributeName(element), row);

  }

  @Override
  public void formatTime(Date date, FormElementModel element, String ordinalValue, Row row) {
    if (date != null) {
      GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
      g.setTime(date);
      addToXmlValueToRow(
          String.format(FormatConsts.TIME_FORMAT_STRING, g.get(Calendar.HOUR_OF_DAY),
              g.get(Calendar.MINUTE), g.get(Calendar.SECOND)), asAttributeName(element), row);
    } else {
      addToXmlValueToRow(null, asAttributeName(element), row);
    }
  }

  @Override
  public void formatDecimal(BigDecimal dub, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(dub, asAttributeName(element), row);
  }

  @Override
  public void formatGeoPoint(GeoPoint coordinate, FormElementModel element, String ordinalValue, Row row) {
    if (coordinate.getLongitude() != null && coordinate.getLatitude() != null) {
      String coordVal = coordinate.getLatitude().toString() + BasicConsts.SPACE
          + coordinate.getLongitude().toString();
      if (coordinate.getAltitude() != null) {
        coordVal += BasicConsts.SPACE + coordinate.getAltitude().toString();
      } else {
        coordVal += BasicConsts.SPACE + "0.0";
      }
      if (coordinate.getAccuracy() != null) {
        coordVal += BasicConsts.SPACE + coordinate.getAccuracy().toString();
      } else {
        coordVal += BasicConsts.SPACE + "0.0";
      }
      addToXmlValueToRow(coordVal, asAttributeName(element), row);
    } else {
      addToXmlValueToRow(null, asAttributeName(element), row);
    }
  }

  @Override
  public void formatLong(Long longInt, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(longInt, asAttributeName(element), row);
  }

  @Override
  public void formatRepeats(SubmissionRepeat repeat, FormElementModel repeatElement, Row row,
      CallingContext cc) throws ODKDatastoreException {
    throw new IllegalStateException("unimplemented");
  }

  @Override
  public void formatString(String string, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(string, asAttributeName(element), row);
  }

  private void addToXmlValueToRow(Object value, String propertyName, Row row) {
    if (value != null) {
      String xmlString = propertyName + "=\"" + StringEscapeUtils.escapeXml(value.toString()) + "\"";
      row.addFormattedValue(xmlString);
    }
  }

}
