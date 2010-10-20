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

import java.util.HashMap;
import java.util.Map;

import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.aggregate.servlet.FormXmlServlet;
import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;

/**
 * Generates an xml description of forms for the servlets
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class FormXmlTable {

  private String requestURL;

  private QueryFormList forms;

  public FormXmlTable(QueryFormList formsToFormat, String webServerURL) {
    this.requestURL = HtmlUtil.createUrl(webServerURL) + FormXmlServlet.ADDR;
    this.forms = formsToFormat;
  }

  public String generateXmlListOfForms() {
    String xml = FormatConsts.BEGIN_FORMS_TAG + BasicConsts.NEW_LINE;

    // build XML table of form information
    for (Form form : forms.getForms()) {
      xml += generateFormXmlEntry(form.getFormId(), form.getViewableName()) + BasicConsts.NEW_LINE;
    }
    return xml + FormatConsts.END_FORMS_TAG;
  }

  private String generateFormXmlEntry(String odkId, String formName) {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.ODK_ID, odkId);
    String urlLink = HtmlUtil.createLinkWithProperties(requestURL, properties);
    return HtmlConsts.BEGIN_OPEN_TAG + FormatConsts.FORM_TAG + BasicConsts.SPACE
        + HtmlUtil.createAttribute(FormatConsts.URL_ATTR, urlLink) + HtmlConsts.END_TAG + formName
        + FormatConsts.END_FORM_TAG;
  }

}
