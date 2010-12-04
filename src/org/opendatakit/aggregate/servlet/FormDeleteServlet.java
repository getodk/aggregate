/*
 * Copyright (C) 2010 University of Washington
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.format.form.FormHtmlTable;
import org.opendatakit.aggregate.process.ProcessType;
import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
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
    Datastore ds = (Datastore) ContextFactory.get().getBean(BeanDefs.DATASTORE_BEAN);
    UserService userService = (UserService) ContextFactory.get().getBean(BeanDefs.USER_BEAN);
    User user = userService.getCurrentUser();
    
    PrintWriter out = resp.getWriter();
 
    try {
      
      QueryFormList formsList = new QueryFormList(true, ds, user);
      FormHtmlTable formFormatter = new FormHtmlTable(formsList);

      beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
      out.print(HtmlUtil.createFormBeginTag(ConfirmServlet.ADDR, HtmlConsts.MULTIPART_FORM_DATA, HtmlConsts.POST));
      out.print(formFormatter.generateHtmlFormTable(false, true));
      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.PROCESS_NUM_RECORDS, Integer.toString(formFormatter.getNumberForms())));
      out.print(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, ServletConsts.PROCESS_TYPE, ProcessType.DELETE_FORM.getButtonText()));
      out.print(HtmlConsts.LINE_BREAK);
      out.print(HtmlConsts.FORM_CLOSE);

      finishBasicHtmlResponse(resp);
    } catch (ODKDatastoreException e) {
	      errorRetreivingData(resp);
    } catch (ODKIncompleteSubmissionData e) {
		e.printStackTrace();
	      errorRetreivingData(resp);
	}
  }
}
