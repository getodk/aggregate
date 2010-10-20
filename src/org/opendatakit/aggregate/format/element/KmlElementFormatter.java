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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.opendatakit.aggregate.servlet.BinaryDataServlet;
import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.format.structure.KmlFormatter;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

public class KmlElementFormatter implements ElementFormatter {
   
  private String baseWebServerUrl;
  
  /**
   * include GPS accuracy data
   */
  private boolean includeAccuracy;
  
  /**
   * Construct a KML Link Element Formatter
   * @param webServerUrl TODO
   * @param includeGpsAccuracy
   *          include GPS accuracy data
   */
  public KmlElementFormatter(String webServerUrl, boolean includeGpsAccuracy) {
    baseWebServerUrl = webServerUrl;
    includeAccuracy = includeGpsAccuracy;
  }
 
  
  @Override
  public void formatBinary(SubmissionKey key, String propertyName, Row row)
      throws ODKDatastoreException {
    if(key == null) {
      row.addFormattedValue(null);
      return;
    }
    
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.BLOB_KEY, key.toString());
    String url = HtmlUtil.createHrefWithProperties(baseWebServerUrl + BinaryDataServlet.ADDR, properties, FormatConsts.VIEW_LINK_TEXT);
    generateDataElement(url, propertyName, row);
  }

  @Override
  public void formatBoolean(Boolean bool, String propertyName, Row row) {
    generateDataElement(bool, propertyName, row);
  }

  @Override
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
    generateDataElement(b.toString(), propertyName, row);
  }

  @Override
  public void formatDate(Date date, String propertyName, Row row) {
    generateDataElement(date, propertyName, row);
  }

  @Override
  public void formatDecimal(BigDecimal dub, String propertyName, Row row) {
    generateDataElement(dub, propertyName, row);
  }

  @Override
  public void formatGeoPoint(GeoPoint coordinate, String propertyName, Row row) {
    String preName = propertyName + FormatConsts.HEADER_CONCAT;
    generateDataElement(coordinate.getLatitude(), preName + FormatConsts.LATITUDE, row);
    generateDataElement(coordinate.getLongitude(), preName + FormatConsts.LONGITUDE, row);
    generateDataElement(coordinate.getAltitude(), preName + FormatConsts.ALTITUDE, row);

    if (includeAccuracy) {
      generateDataElement(coordinate.getAccuracy(), preName + FormatConsts.ACCURACY, row);
    }
    
  }

  @Override
  public void formatLong(Long longInt, String propertyName, Row row) {
    generateDataElement(longInt, propertyName, row);
  }

  @Override
  public void formatRepeats(SubmissionRepeat repeat, String propertyName, Row row)
      throws ODKDatastoreException {
    // TODO FIGURE OUT WHAT TO DO WITH REPEATS IN KML

  }

  @Override
  public void formatString(String string, String propertyName, Row row) {
    generateDataElement(string, propertyName, row);
  }

  private void generateDataElement(Object value, String name, Row row){
    String valueAsString = BasicConsts.EMPTY_STRING;
    if(value != null) {
      valueAsString += StringEscapeUtils.escapeXml(value.toString());
    }
    row.addFormattedValue(String.format(KmlFormatter.KML_DATA_ELEMENT_TEMPLATE, StringEscapeUtils.escapeXml(name), valueAsString));
  }
    
}
