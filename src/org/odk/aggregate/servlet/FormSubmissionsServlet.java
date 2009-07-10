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

import org.odk.aggregate.PMFactory;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.table.SubmissionHtmlTable;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet generates a webpage with a list of submissions from a specified form
 *
 * @author wbrunette@gmail.com
 *
 */
public class FormSubmissionsServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 2514025048848108669L;

  /**
   * URI from base
   */
  public static final String ADDR = "formSubmissions";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Form Submissions Results";

  /**
   * Handler for HTTP Get request that responds list of submissions from a
   * specified form
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // get parameter
    String odkId = getParameter(req, ServletConsts.ODK_ID);
    
    if(odkId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }

    // header info
    beginBasicHtmlResponse(TITLE_INFO, resp, true);
    
    PersistenceManager pm = PMFactory.get().getPersistenceManager();
    
    try {
      resp.getWriter().print(new SubmissionHtmlTable(odkId, pm).generateHtmlSubmissionResultsTable());

      // footer info
      finishBasicHtmlResponse(resp);
      
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
    } catch (ODKIncompleteSubmissionData e) {
      errorRetreivingData(resp);
    } finally {
      pm.close();
    }
  }
}
