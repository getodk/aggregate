/*
 * Copyright (C) 2009 University of Washington
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

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.process.ProcessType;
import org.odk.aggregate.table.FormHtmlTable;

public class FormDeleteServlet extends ServletUtilBase {
 
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -7359069138151879679L;

  /**
   * URI from base
   */
  public static final String ADDR = "formDelete";
  
  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Form Delete";
  
  /**
   * Handler for HTTP Get request that shows the list of forms
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }
    EntityManager em = EMFactory.get().createEntityManager();
    PrintWriter out = resp.getWriter();
    FormHtmlTable forms = new FormHtmlTable(em);
    forms.generateHtmlFormTable(false, true);
    beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
    out.print(HtmlUtil.createFormBeginTag(ConfirmServlet.ADDR, ServletConsts.MULTIPART_FORM_DATA, ServletConsts.POST));
    out.print(forms.getHtml());
    out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.PROCESS_NUM_RECORDS, Integer.toString(forms.getNumberForms())));
    out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, ServletConsts.PROCESS_TYPE, ProcessType.DELETE_FORM.getButtonText()));
    out.print(HtmlConsts.LINE_BREAK);
    out.print(HtmlConsts.FORM_CLOSE);
    
    finishBasicHtmlResponse(resp);
    em.close();
  }
}
