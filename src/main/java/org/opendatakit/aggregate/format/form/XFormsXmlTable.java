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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.format.XFormsTableConsts;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.servlet.FormXmlServlet;
import org.opendatakit.aggregate.servlet.XFormsManifestServlet;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Generates an OpenRosa-compliant xml description of forms for the servlet
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class XFormsXmlTable {

  private static final String XML_TAG_NAMESPACE = "http://openrosa.org/xforms/xformsList";
private final String downloadRequestURL;
  private final String manifestRequestURL;

  private List<Form> forms;

  public XFormsXmlTable(List<Form> formsToFormat, String webServerUrl) {
    this.downloadRequestURL = webServerUrl + BasicConsts.FORWARDSLASH + FormXmlServlet.ADDR;
    this.manifestRequestURL = webServerUrl + BasicConsts.FORWARDSLASH + XFormsManifestServlet.ADDR;
    this.forms = formsToFormat;
  }

  public void generateXmlListOfForms(PrintWriter output) throws IOException {
    Document d = new Document();
	d.setStandalone(true);
	d.setEncoding(HtmlConsts.UTF8_ENCODE);
	Element e = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.XFORMS_TAG);
	e.setPrefix(null, XML_TAG_NAMESPACE);
	d.addChild(0, Node.ELEMENT, e);
	int idx = 0;
	e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);

    // build XML table of form information
    for (Form form : forms) {
    	if ( !form.getDownloadEnabled() ) continue;

    	idx = generateFormXmlEntry(d, e, idx, form);
    }

	KXmlSerializer serializer = new KXmlSerializer();
	serializer.setOutput(output);
	// setting the response content type emits the xml header.
	// just write the body here...
	d.writeChildren(serializer); 
  }

  private int generateFormXmlEntry(Document d, Element e, int idx, Form form) {
	  
	  int xfIdx = 0;
	  Element xformElement = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.XFORM_TAG);
	  e.addChild(idx++, Node.ELEMENT, xformElement);
	  
	  Element formIdElement = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.FORM_ID_TAG);
	  xformElement.addChild(xfIdx++, Node.ELEMENT, formIdElement);
	  formIdElement.addChild(0, Node.TEXT, form.getFormId());
      xformElement.addChild(xfIdx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
      
	  Element formNameElement = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.FORM_NAME_TAG);
	  xformElement.addChild(xfIdx++, Node.ELEMENT, formNameElement);
	  formNameElement.addChild(0, Node.TEXT, form.getViewableName());
      xformElement.addChild(xfIdx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
      
	  Element majorMinorVersionElement = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.MAJOR_MINOR_VERSION_TAG);
	  xformElement.addChild(xfIdx++, Node.ELEMENT, majorMinorVersionElement);
	  majorMinorVersionElement.addChild(0, Node.TEXT, form.getMajorMinorVersionString());
      xformElement.addChild(xfIdx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
	  
      String description = form.getDescription();
      if ( description != null ) {
    	  Element descriptionElement = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.DESCRIPTION_TEXT_TAG);
    	  xformElement.addChild(xfIdx++, Node.ELEMENT, descriptionElement);
    	  descriptionElement.addChild(0, Node.TEXT, description);
          xformElement.addChild(xfIdx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
      }
	  String descriptionUrl = form.getDescriptionUrl();
	  if ( descriptionUrl != null ) {
    	  Element descriptionUrlElement = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.DESCRIPTION_URL_TAG);
    	  xformElement.addChild(xfIdx++, Node.ELEMENT, descriptionUrlElement);
    	  descriptionUrlElement.addChild(0, Node.TEXT, descriptionUrl);
          xformElement.addChild(xfIdx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
	  }

	  {
		  Map<String, String> properties = new HashMap<String, String>();
		  properties.put(ServletConsts.FORM_ID, form.getFormId());
		  String urlLink = HtmlUtil.createLinkWithProperties(downloadRequestURL, properties);
		  
		  Element downloadUrlElement = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.DOWNLOAD_URL_TAG);
		  xformElement.addChild(xfIdx++, Node.ELEMENT, downloadUrlElement);
		  downloadUrlElement.addChild(0, Node.TEXT, urlLink);
	      xformElement.addChild(xfIdx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
	  }
	  
	  if ( form.hasManifestFileset() ) {
		  Map<String, String> properties = new HashMap<String, String>();
		  properties.put(ServletConsts.FORM_ID, form.getFormId());
	      String urlLink = HtmlUtil.createLinkWithProperties(manifestRequestURL, properties);
		  
		  Element manifestUrlElement = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.MANIFEST_URL_TAG);
		  xformElement.addChild(xfIdx++, Node.ELEMENT, manifestUrlElement);
		  manifestUrlElement.addChild(0, Node.TEXT, urlLink);
	      xformElement.addChild(xfIdx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
	  }
      e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
      return idx;
  }
}
