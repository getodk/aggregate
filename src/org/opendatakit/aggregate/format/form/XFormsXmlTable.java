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

import org.apache.commons.lang.StringEscapeUtils;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.format.XFormsTableConsts;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.aggregate.servlet.FormXmlServlet;
import org.opendatakit.aggregate.servlet.XFormsManifestServlet;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;

/**
 * Generates an OpenRosa-compliant xml description of forms for the servlet
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class XFormsXmlTable {

  private final String downloadRequestURL;
  private final String manifestRequestURL;

  private QueryFormList forms;

  public XFormsXmlTable(QueryFormList formsToFormat, String webServerURL) {
    this.downloadRequestURL = HtmlUtil.createUrl(webServerURL) + FormXmlServlet.ADDR;
    this.manifestRequestURL = HtmlUtil.createUrl(webServerURL) + XFormsManifestServlet.ADDR;
    this.forms = formsToFormat;
  }

  public String generateXmlListOfForms() {
    StringBuilder b = new StringBuilder();

    b.append(HtmlConsts.BEGIN_OPEN_TAG);
    b.append(XFormsTableConsts.XFORMS_TAG);
    b.append(BasicConsts.SPACE);
    b.append(HtmlUtil.createAttribute("xmlns", "http://openrosa.org/xforms/xformsList"));
    b.append(HtmlConsts.END_TAG);
    b.append(BasicConsts.NEW_LINE);

    // build XML table of form information
    for (Form form : forms.getForms()) {
    	if ( form.getFormId().equals(Form.URI_FORM_ID_VALUE_FORM_INFO)) continue;
    	if ( form.getFormId().equals(PersistentResults.FORM_ID_PERSISTENT_RESULT)) continue;
    	if ( form.getFormId().equals(MiscTasks.FORM_ID_MISC_TASKS)) continue;
    	generateFormXmlEntry(b, form);
    }
    b.append(HtmlUtil.createEndTag(XFormsTableConsts.XFORMS_TAG));
    return b.toString();
  }

  private void generateFormXmlEntry(StringBuilder b, Form form) {
	  String formName = form.getViewableName();
	  String description = form.getDescription();
	  String descriptionUrl = form.getDescriptionUrl();
		  
	b.append(HtmlUtil.createBeginTag(XFormsTableConsts.XFORM_TAG));
    b.append(BasicConsts.NEW_LINE);
	b.append(HtmlUtil.createBeginTag(XFormsTableConsts.FORM_ID_TAG));
	b.append(StringEscapeUtils.escapeXml(form.getFormId()));
	b.append(HtmlUtil.createEndTag(XFormsTableConsts.FORM_ID_TAG));
    b.append(BasicConsts.NEW_LINE);
	b.append(HtmlUtil.createBeginTag(XFormsTableConsts.FORM_NAME_TAG));
	b.append(StringEscapeUtils.escapeXml(formName));
	b.append(HtmlUtil.createEndTag(XFormsTableConsts.FORM_NAME_TAG));
    b.append(BasicConsts.NEW_LINE);
    b.append(HtmlUtil.createBeginTag(XFormsTableConsts.MAJOR_MINOR_VERSION_TAG));
    b.append(form.getMajorMinorVersionString());
    b.append(HtmlUtil.createEndTag(XFormsTableConsts.MAJOR_MINOR_VERSION_TAG));
    b.append(BasicConsts.NEW_LINE);
    if ( description != null ) {
		b.append(HtmlUtil.createBeginTag(XFormsTableConsts.DESCRIPTION_TEXT_TAG));
		b.append(StringEscapeUtils.escapeXml(description));
		b.append(HtmlUtil.createEndTag(XFormsTableConsts.DESCRIPTION_TEXT_TAG));
	    b.append(BasicConsts.NEW_LINE);
    }
    if ( descriptionUrl != null ) {
		b.append(HtmlUtil.createBeginTag(XFormsTableConsts.DESCRIPTION_URL_TAG));
		b.append(descriptionUrl);
		b.append(HtmlUtil.createEndTag(XFormsTableConsts.DESCRIPTION_URL_TAG));
	    b.append(BasicConsts.NEW_LINE);
    }
	b.append(HtmlUtil.createBeginTag(XFormsTableConsts.DOWNLOAD_URL_TAG));
	{
		Map<String, String> properties = new HashMap<String, String>();
	    properties.put(ServletConsts.FORM_ID, form.getFormId());
	    String urlLink = HtmlUtil.createLinkWithProperties(downloadRequestURL, properties);
	    b.append(urlLink);
	}
	b.append(HtmlUtil.createEndTag(XFormsTableConsts.DOWNLOAD_URL_TAG));
    b.append(BasicConsts.NEW_LINE);
    if ( form.hasManifestFileset() ) {
    	b.append(HtmlUtil.createBeginTag(XFormsTableConsts.MANIFEST_URL_TAG));
    	Map<String, String> properties = new HashMap<String, String>();
        properties.put(ServletConsts.FORM_ID, form.getFormId());
        String urlLink = HtmlUtil.createLinkWithProperties(manifestRequestURL, properties);
        b.append(urlLink);
    	b.append(HtmlUtil.createEndTag(XFormsTableConsts.MANIFEST_URL_TAG));
        b.append(BasicConsts.NEW_LINE);
    }
	b.append(HtmlUtil.createEndTag(XFormsTableConsts.XFORM_TAG));
    b.append(BasicConsts.NEW_LINE);
  }
}
