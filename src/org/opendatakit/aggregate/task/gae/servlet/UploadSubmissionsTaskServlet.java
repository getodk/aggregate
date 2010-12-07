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
package org.opendatakit.aggregate.task.gae.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.servlet.ServletUtilBase;
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.aggregate.task.gae.UploadSubmissionsImpl;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UploadSubmissionsTaskServlet extends ServletUtilBase{
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 4295412985320942608L;

  /**
   * URI from base
   */
  public static final String ADDR = "uploadSubmissionsTask";

  /**
   * Handler for HTTP Get request to create xform upload page
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    // TODO: talk to MITCH about the fact the user will be incorrect
    UserService userService = (UserService) ContextFactory.get().getBean(BeanDefs.USER_BEAN);
    User user = userService.getCurrentUser();

    // get parameter
    String fscUri = getParameter(req, ExternalServiceConsts.FSC_URI_PARAM);
    if (fscUri == null) {
      errorMissingKeyParam(resp);
      return;
    }
    System.out.println("STARTING UPLOAD SUBMISSION TASK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    Datastore ds = (Datastore) ContextFactory.get().getBean(BeanDefs.DATASTORE_BEAN);
    FormServiceCursor fsc;
    try {
      fsc = FormServiceCursor.getFormServiceCursor(fscUri, ds, user);
    } catch (ODKEntityNotFoundException e1) {
      // TODO: fix bug we should not be generating tasks for fsc that don't exist
      // however not critical bug as execution path dies with this try/catch
      System.err.println("BUG: we generated an task for a form service cursor that didn't exist");
      return;
    }
    
    try {
      UploadSubmissions worker = new UploadSubmissionsImpl();
      worker.uploadSubmissions(fsc, getServerURL(req), ds, user);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }
}
