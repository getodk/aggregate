/*
 * Copyright (C) 2010 University of Washington Inc.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;

/**
 * Servlet to allow user to specify title/name of xform if one doesn't already
 * exist in the xform
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class FormTitleServlet extends ServletUtilBase {

  private static final String TITLE_OF_THE_XFORM = "Title of the Xform:";

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 4078944981520404557L;
  
  /**
   * URI from base
   */
  public static final String ADDR = "formTitle";
  
  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Xform Title Entry";
  
  /**
   * Handler for HTTP Get request to obtain form title
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }
   
    beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
    
    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.createFormBeginTag(FormUploadServlet.ADDR, ServletConsts.MULTIPART_FORM_DATA, ServletConsts.POST));
    out.write(TITLE_OF_THE_XFORM + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_TEXT, ServletConsts.FORM_NAME_PRAM, null));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.encodeFormInHiddenInput(req)); 
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, "Submit"));
    out.write(HtmlConsts.FORM_CLOSE);
    finishBasicHtmlResponse(resp);
  }
  
}
