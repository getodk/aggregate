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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.structure.XmlFormatter;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Formats xml tags <name>value</name> with proper escaping for
 * reconstructing the submission xml that Collect may have used
 * when submitting data.
 *
 * NOTE: This class does not use the Row object for the
 * reconstruction, but instead writes to the XmlFormatter
 * output stream directly.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class XmlElementFormatter implements ElementFormatter {
	XmlFormatter xmlFormatter;

	String prefix = "";

  /**
   * Construct a XML Element Formatter
 * @param xmlFormatter
   *
   */
  public XmlElementFormatter(XmlFormatter xmlFormatter) {
	  this.xmlFormatter = xmlFormatter;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public void formatUid(String uri, String propertyName, Row row) {
    // unneeded so unimplemented
  }

  @Override
  public void formatBinary(BlobSubmissionType blobSubmission, FormElementModel element, String ordinalValue,
      Row row, CallingContext cc) throws ODKDatastoreException {
    if( blobSubmission == null ||
    	(blobSubmission.getAttachmentCount(cc) == 0) ||
    	(blobSubmission.getContentHash(1, cc) == null) ) {
   	    addToXmlValueToRow(null, element.getElementName(), row);
	} else {
		addToXmlValueToRow(blobSubmission.getUnrootedFilename(1, cc), element.getElementName(), row);
	}
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
    String str = b.toString();
    if ( str.length() > 0 ) {
    	addToXmlValueToRow(str, element.getElementName(), row);
    } else {
    	addToXmlValueToRow(null, element.getElementName(), row);
    }
  }

  @Override
  public void formatDate(Date date, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(WebUtils.asSubmissionDateOnlyString(date), element.getElementName(), row);
  }

  @Override
  public void formatDateTime(Date date, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(WebUtils.asSubmissionDateTimeString(date), element.getElementName(), row);
  }

  @Override
  public void formatTime(Date date, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(WebUtils.asSubmissionTimeOnlyString(date), element.getElementName(), row);
  }

  @Override
  public void formatDecimal(WrappedBigDecimal dub, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(dub, element.getElementName(), row);

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
  }

  @Override
  public void formatString(String string, FormElementModel element, String ordinalValue, Row row) {
    addToXmlValueToRow(string, element.getElementName(), row);
  }

  private void addToXmlValueToRow(Object value, String propertyName, Row row) {

    if (value != null) {
      String xmlString = HtmlUtil.createBeginTag(prefix + propertyName);
      xmlString += StringEscapeUtils.escapeXml10(value.toString());
      xmlString += HtmlUtil.createEndTag(prefix + propertyName);
      xmlFormatter.writeXml(xmlString);
    } else {
      xmlFormatter.writeXml( HtmlUtil.createSelfClosingTag(prefix + propertyName));
    }
  }

}
