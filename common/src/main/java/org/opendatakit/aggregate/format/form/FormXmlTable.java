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
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.format.FormTableConsts;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.servlet.FormXmlServlet;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Generates an xml description of forms for the servlets
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormXmlTable {

  private String requestURL;

  private List<IForm> forms;

  public FormXmlTable(List<IForm> formsToFormat, String webServerUrl) {
    this.requestURL = webServerUrl + BasicConsts.FORWARDSLASH + FormXmlServlet.ADDR;
    this.forms = formsToFormat;
  }

  public String generateXmlListOfForms() {
    String xml = FormTableConsts.BEGIN_FORMS_TAG + BasicConsts.NEW_LINE;

    // build XML table of form information
    for (IForm form : forms) {
    	if ( !form.getDownloadEnabled() ) continue;

      xml += generateFormXmlEntry(form.getFormId(), form.getViewableName()) + BasicConsts.NEW_LINE;
    }
    return xml + FormTableConsts.END_FORMS_TAG;
  }

  private String generateFormXmlEntry(String odkId, String formName) {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.FORM_ID, odkId);
    String urlLink = HtmlUtil.createLinkWithProperties(requestURL, properties);
    return HtmlConsts.BEGIN_OPEN_TAG + FormTableConsts.FORM_TAG + BasicConsts.SPACE
        + HtmlUtil.createAttribute(FormTableConsts.URL_ATTR, urlLink) + HtmlConsts.END_TAG + formName
        + FormTableConsts.END_FORM_TAG;
  }

}
