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

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.format.FormTableConsts;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.aggregate.servlet.CsvServlet;
import org.opendatakit.aggregate.servlet.ExternalServiceServlet;
import org.opendatakit.aggregate.servlet.FormSubmissionsServlet;
import org.opendatakit.aggregate.servlet.FormXmlServlet;
import org.opendatakit.aggregate.servlet.KmlSettingsServlet;
import org.opendatakit.aggregate.servlet.QueryServlet;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.constants.BasicConsts;

/**
 * Generates an html table of forms for the servlets
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormHtmlTable {


  /**
   * Array containing the table header names
   */
  private static List<String> HEADERS_W_BUTTONS = Arrays.asList(FormTableConsts.FT_HEADER_NAME,
      FormTableConsts.FT_HEADER_FORM_ID, FormTableConsts.FT_HEADER_USER, FormTableConsts.FT_HEADER_RESULTS,
      FormTableConsts.FT_HEADER_QUERY, FormTableConsts.FT_HEADER_CSV,
      FormTableConsts.FT_HEADER_EXTERNAL_SERVICE, FormTableConsts.FT_HEADER_KML,
      FormTableConsts.FT_HEADER_XFORM);

  private static List<String> HEADERS_WO_BUTTONS = Arrays.asList(FormTableConsts.FT_HEADER_NAME,
      FormTableConsts.FT_HEADER_FORM_ID, FormTableConsts.FT_HEADER_USER);
 
  private final QueryFormList forms;
  private final CallingContext cc;

  public FormHtmlTable(QueryFormList formsToFormat, CallingContext cc)  {
    forms = formsToFormat;
    this.cc = cc;
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
      SubmissionKey submissionKey = form.getSubmissionKey();
      Row row = new Row(submissionKey);

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
    properties.put(ServletConsts.FORM_ID, formId);

    String resultButton = HtmlUtil.createHtmlButtonToGetServlet(cc.getWebApplicationURL(FormSubmissionsServlet.ADDR),
        FormTableConsts.RESULTS_BUTTON_TXT, properties);
    
    String csvButton = HtmlUtil.createHtmlButtonToGetServlet(cc.getWebApplicationURL(CsvServlet.ADDR),
        FormTableConsts.CSV_BUTTON_TXT, properties);
    
    String externalServiceButton = HtmlUtil.createHtmlButtonToGetServlet(cc.getWebApplicationURL(ExternalServiceServlet.ADDR),
    		FormTableConsts.EXTERNAL_SERVICE_BUTTON_TXT, properties);

    String kmlButton = HtmlUtil.createHtmlButtonToGetServlet(cc.getWebApplicationURL(KmlSettingsServlet.ADDR),
        FormTableConsts.KML_BUTTON_TXT, properties);

    String queryButton = HtmlUtil.createHtmlButtonToGetServlet(cc.getWebApplicationURL(QueryServlet.ADDR), 
        FormTableConsts.QUERY_BUTTON_TXT, properties);
            
    
    Map<String, String> xmlProperties = new HashMap<String, String>();
    xmlProperties.put(ServletConsts.FORM_ID, formId);
    xmlProperties.put(ServletConsts.HUMAN_READABLE, BasicConsts.TRUE);
    String xmlButton = HtmlUtil.createHtmlButtonToGetServlet(cc.getWebApplicationURL(FormXmlServlet.WWW_ADDR),
        FormTableConsts.XML_BUTTON_TXT, xmlProperties);

    row.addFormattedValue(resultButton);
    row.addFormattedValue(queryButton);
    row.addFormattedValue(csvButton);
    row.addFormattedValue(externalServiceButton);
    row.addFormattedValue(kmlButton);
    row.addFormattedValue(xmlButton);
  }

}
