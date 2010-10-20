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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.task.FormDelete;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

public class FormDeleteTaskServlet extends ServletUtilBase {
 
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 8219849865201422548L;

  /**
   * URI from base
   */
  public static final String ADDR = "formDeleteTask";
  
  /**
   * Handler for HTTP Get request that shows the list of forms
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserService userService = (UserService) ContextFactory.get().getBean(ServletConsts.USER_BEAN);
    User user = userService.getCurrentUser();
    // get parameter
	
    String odkId = getParameter(req, ServletConsts.ODK_ID);
    if (odkId == null) {
      errorMissingKeyParam(resp);
      return;
    }
    
    Datastore datastore = (Datastore) ContextFactory.get().getBean(ServletConsts.DATASTORE_BEAN);
    Form form;
	try {
		form = Form.retrieveForm(odkId, datastore, user, userService.getCurrentRealm());
	} catch (ODKFormNotFoundException e) {
		e.printStackTrace();
		odkIdNotFoundError(resp);
		return;
	}

    FormDelete formDelete = (FormDelete) ContextFactory.get().getBean(
        ServletConsts.FORM_DELETE_BEAN);
    try {
      formDelete.deleteForm(form, user);
    } catch (Exception e) {
      resp.getWriter().print(ErrorConsts.TASK_PROBLEM);
    }
  }
}

