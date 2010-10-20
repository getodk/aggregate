/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.format.form;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.aggregate.servlet.CsvServlet;
import org.opendatakit.aggregate.servlet.ExternalServiceServlet;
import org.opendatakit.aggregate.servlet.FormSubmissionsServlet;
import org.opendatakit.aggregate.servlet.FormXmlServlet;
import org.opendatakit.aggregate.servlet.KmlSettingsServlet;
import org.opendatakit.aggregate.servlet.QueryServlet;
import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.element.Row;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.EntityKey;

/**
 * Generates an html table of forms for the servlets
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class FormHtmlTable {


  /**
   * Array containing the table header names
   */
  private static List<String> HEADERS_W_BUTTONS = Arrays.asList(FormatConsts.FT_HEADER_NAME,
      FormatConsts.FT_HEADER_ODKID, FormatConsts.FT_HEADER_USER, FormatConsts.FT_HEADER_RESULTS,
      FormatConsts.FT_HEADER_QUERY, FormatConsts.FT_HEADER_CSV,
      FormatConsts.FT_HEADER_EXTERNAL_SERVICE, FormatConsts.FT_HEADER_KML,
      FormatConsts.FT_HEADER_XFORM);

  private static List<String> HEADERS_WO_BUTTONS = Arrays.asList(FormatConsts.FT_HEADER_NAME,
      FormatConsts.FT_HEADER_ODKID, FormatConsts.FT_HEADER_USER);
 
  private QueryFormList forms;

  public FormHtmlTable(QueryFormList formsToFormat)  {
    forms = formsToFormat;
  }

  public int getNumberForms() {
    return forms.getForms().size();
  }
  
  /**
   * Generate a table of available forms in html format
   * 
   * @throws UnsupportedEncodingException
   */
  public String generateHtmlFormTable(boolean buttons, boolean selectBoxes)
      throws UnsupportedEncodingException {
  

    List<Row> formattedFormValues = new ArrayList<Row>();
    
    // build HTML table of form information
    for (Form form : forms.getForms()) {

      // create row
      EntityKey entityKey = form.getEntityKey();
      Row row = new Row(entityKey);

      row.addFormattedValue(form.getViewableName());
      row.addFormattedValue(form.getFormId());
      row.addFormattedValue(form.getCreationUser());

      if (buttons) {
        createButtonsHtml(form.getFormId(), row);
      }
      formattedFormValues.add(row);
    }

    if (buttons) {
      return HtmlUtil.wrapResultTableWithHtmlTags(selectBoxes, HEADERS_W_BUTTONS, formattedFormValues);
    } else {
      return HtmlUtil.wrapResultTableWithHtmlTags(selectBoxes, HEADERS_WO_BUTTONS, formattedFormValues);
    }
  }

  /**
   * Create html buttons 
   * @param form
   * @param row
   * @throws UnsupportedEncodingException
   */ 
  private void createButtonsHtml(String formId, Row row) throws UnsupportedEncodingException {

    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.ODK_ID, formId);

    String resultButton = HtmlUtil.createHtmlButtonToGetServlet(FormSubmissionsServlet.ADDR,
        FormatConsts.RESULTS_BUTTON_TXT, properties);
    String csvButton = HtmlUtil.createHtmlButtonToGetServlet(CsvServlet.ADDR,
        FormatConsts.CSV_BUTTON_TXT, properties);
    String externalServiceButton = HtmlUtil.createHtmlButtonToGetServlet(
        ExternalServiceServlet.ADDR, FormatConsts.EXTERNAL_SERVICE_BUTTON_TXT, properties);

    String kmlButton = HtmlUtil.createHtmlButtonToGetServlet(KmlSettingsServlet.ADDR,
        FormatConsts.KML_BUTTON_TXT, properties);

    String queryButton = HtmlUtil.createHtmlButtonToGetServlet(QueryServlet.ADDR, 
        FormatConsts.QUERY_BUTTON_TXT, properties);
            
    
    Map<String, String> xmlProperties = new HashMap<String, String>();
    xmlProperties.put(ServletConsts.ODK_ID, formId);
    xmlProperties.put(ServletConsts.HUMAN_READABLE, BasicConsts.TRUE);
    String xmlButton = HtmlUtil.createHtmlButtonToGetServlet(FormXmlServlet.ADDR,
        FormatConsts.XML_BUTTON_TXT, xmlProperties);

    row.addFormattedValue(resultButton);
    row.addFormattedValue(queryButton);
    row.addFormattedValue(csvButton);
    row.addFormattedValue(externalServiceButton);
    row.addFormattedValue(kmlButton);
    row.addFormattedValue(xmlButton);
  }

}
