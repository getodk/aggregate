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

package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * Servlet to generate the XML file in plain text
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormXmlServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -5861240658170389989L;

  /**
   * URI from base
   */
  public static final String ADDR = "formXml";
  public static final String WWW_ADDR = "www/formXml";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Form Xml Viewer";

  /**
   * Handler for HTTP Get request that responds with the XML in plain
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    
    // get parameters
    String formId = getParameter(req, ServletConsts.FORM_ID);
    if (formId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    String readable = getParameter(req, ServletConsts.HUMAN_READABLE);
    boolean humanReadable = false;
    if (readable != null) {
      humanReadable = Boolean.parseBoolean(readable);
    }

    // servlet has two different paths to it -- depdending upon whether it is human readable or not.
    CallingContext cc = ContextFactory.getCallingContext(this, humanReadable ? WWW_ADDR : ADDR, req);

    Form form;
    try {
      form = Form.retrieveForm(formId, cc);
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    }
    String xmlString = null;

    try {
	    if (form != null) {
			xmlString = form.getFormXml();
	    } else {
	      odkIdNotFoundError(resp);
	    }
	
	    PrintWriter out = resp.getWriter();
	
	    resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
	
	    if (humanReadable) {
	      Map<String, String> properties = new HashMap<String, String>();
	      properties.put(ServletConsts.FORM_ID, formId);
	      String downloadXmlButton = HtmlUtil.createHtmlButtonToGetServlet(
	    		  cc.getWebApplicationURL(ADDR),
	          ServletConsts.DOWNLOAD_XML_BUTTON_TXT, properties);
	
	      beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info
	      out.println("<h3>Form Name: <FONT COLOR=0000FF>" + form.getViewableName()
	          + "</FONT></h3>");
	      out.println("<h3>File Name: <FONT COLOR=0000FF>" + form.getFormFilename()
	          + "</FONT></h3>");
	      out.println(downloadXmlButton); // download button
	      out.println("<PRE>");
	      StringEscapeUtils.escapeHtml(out, xmlString);// form xml
	      out.println("</PRE>");
	      finishBasicHtmlResponse(resp); // footer info
	    } else {
	      resp.setContentType(HtmlConsts.RESP_TYPE_XML);
	      setDownloadFileName(resp, form.getFormFilename());
	      out.print(xmlString);
	    }
	} catch (ODKDatastoreException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	      odkIdNotFoundError(resp);
	}
  }
}
