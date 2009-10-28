/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.form.Form;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * Servlet to generate the XML file in plain text
 *
 * @author wbrunette@gmail.com
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
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // get parameters
    String odkFormKey = getParameter(req, ServletConsts.ODK_FORM_KEY);
    if(odkFormKey == null) {
      errorMissingKeyParam(resp);
      return;
    }

    String readable =  getParameter(req, ServletConsts.HUMAN_READABLE);
    boolean humanReadable = false;
    if(readable != null) {
      humanReadable = Boolean.parseBoolean(readable);
    }

    Key formKey = KeyFactory.stringToKey(odkFormKey);
    EntityManager em = EMFactory.get().createEntityManager();
    Form form = em.getReference(Form.class, formKey);
    String xmlString = null;
    
    if (form != null) {
      xmlString = form.getOriginalForm();
    } else {
      odkIdNotFoundError(resp);
    }

    PrintWriter out = resp.getWriter();
    
    resp.setCharacterEncoding("UTF-8");
    
    if(humanReadable) {
      Map<String, String> properties = new HashMap<String, String>();
      properties.put(ServletConsts.ODK_FORM_KEY, URLEncoder.encode(odkFormKey, ServletConsts.ENCODE_SCHEME));
      String downloadXmlButton =
          HtmlUtil.createHtmlButtonToGetServlet(ADDR, ServletConsts.DOWNLOAD_XML_BUTTON_TXT, properties);

      beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
      out.println("<h3>Form Name: <FONT COLOR=0000FF>" + form.getViewableName() + "</FONT></h3>");
      out.println("<h3>File Name: <FONT COLOR=0000FF>" + form.getFileName() + "</FONT></h3>");
      out.println(downloadXmlButton); // download button
      out.print(formatHtmlString(xmlString));// form xml
      finishBasicHtmlResponse(resp); // footer info
    } else {
      resp.setContentType(ServletConsts.RESP_TYPE_XML);
      setDownloadFileName(resp, form.getFileName());
      out.print(xmlString);
    }
    em.close();

  }
}
