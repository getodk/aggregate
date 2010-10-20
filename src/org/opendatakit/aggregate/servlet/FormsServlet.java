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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.format.form.FormHtmlTable;
import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * Servlet generates a webpage with a list of forms
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class FormsServlet extends ServletUtilBase {
  
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -4405695604321586000L;

  /**
   * URI from base
   */
  public static final String ADDR = "forms";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Form Manager";

  /**
   * Handler for HTTP Get request that shows the list of forms
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }
    Datastore ds = (Datastore) ContextFactory.get().getBean(ServletConsts.DATASTORE_BEAN);
    UserService userService = (UserService) ContextFactory.get().getBean(ServletConsts.USER_BEAN);
    User user = userService.getCurrentUser();
    
    try {
      // ensure that Form table exists...
      QueryFormList formsList = new QueryFormList(false, ds, user);
      FormHtmlTable formFormatter = new FormHtmlTable(formsList);
      
      // generate html
      beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
      resp.getWriter().print(formFormatter.generateHtmlFormTable(true, false));
      finishBasicHtmlResponse(resp);
      
    } catch (ODKDatastoreException e) {
      errorRetreivingData(resp);
    } catch (ODKIncompleteSubmissionData e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
      errorRetreivingData(resp);
	}

  }
}
