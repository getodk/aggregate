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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.servlet.BinaryDataServlet;
import org.opendatakit.aggregate.servlet.FormMultipleValueServlet;
import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

public class HtmlLinkElementFormatter extends BasicElementFormatter{

  private final FormDefinition formDefinition;
  
  private final String baseWebServerUrl;
  
  /**
   * Construct a Html Link Element Formatter
   * @param xform 
   *    form definition of the submissions that will be formatted
   * @param webServerUrl TODO
   * @param separateGpsCoordinates
   *          separate the GPS coordinates of latitude and longitude into
   *          columns
   * @param includeGpsAltitude
   *          include GPS altitude data
   * @param includeGpsAccuracy
   *          include GPS accuracy data
   */
  public HtmlLinkElementFormatter(FormDefinition xform, String webServerUrl,
      boolean separateGpsCoordinates, boolean includeGpsAltitude, boolean includeGpsAccuracy) {
    super(separateGpsCoordinates, includeGpsAltitude,includeGpsAccuracy);
    formDefinition = xform;
    baseWebServerUrl = webServerUrl;
  }
 
  @Override
  public void formatBinary(SubmissionKey key, String propertyName, Row row) throws ODKDatastoreException {
    if(key == null) {
      row.addFormattedValue(null);
      return;
    }
    
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.BLOB_KEY, key.toString());
    String url = HtmlUtil.createHrefWithProperties(HtmlUtil.createUrl(baseWebServerUrl) + BinaryDataServlet.ADDR, properties, FormatConsts.VIEW_LINK_TEXT);
    row.addFormattedValue(url);    
  }


  @Override
  public void formatRepeats(SubmissionRepeat repeat, String propertyName, Row row) throws ODKDatastoreException {
    if(repeat == null) {
      row.addFormattedValue(null);
      return;
    }
    
    List<SubmissionSet> sets = repeat.getSubmissionSets();
    if ( sets.size() == 0 ) {
    	row.addFormattedValue(null);
    	return;
    }

    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.ODK_ID, formDefinition.getFormId());
    properties.put(ServletConsts.KIND, repeat.getPropertyName());
    properties.put(ServletConsts.FORM_ELEMENT_KEY, repeat.constructSubmissionKey().toString());
    properties.put(ServletConsts.PARENT_KEY, row.getEntityKey().getKey());
    
    String url = HtmlUtil.createHrefWithProperties( HtmlUtil.createUrl(baseWebServerUrl) + FormMultipleValueServlet.ADDR, properties, FormatConsts.VIEW_LINK_TEXT);
    row.addFormattedValue(url);    
  }

  
}
