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
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.constants.ExternalServiceOption;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.task.WorksheetCreator;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

public class WorksheetServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 3054003683995535651L;

  /**
   * URI from base
   */
  public static final String ADDR = "worksheet";

  /**
   * Handler for HTTP Get request to create xform upload page
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

      UserService userService = (UserService) ContextFactory.get().getBean(
          ServletConsts.USER_BEAN);
      User user = userService.getCurrentUser();

    // get parameter
    String odkId = getParameter(req, ServletConsts.ODK_ID);
    if (odkId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    String spreadsheetName = getParameter(req,
        ServletConsts.SPREADSHEET_NAME_PARAM);
    if (spreadsheetName == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }

    String esTypeString = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);
    if (esTypeString == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }
    ExternalServiceOption esType = ExternalServiceOption.valueOf(esTypeString);
    if ( esType == null ) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid " + 
        		 ServletConsts.EXTERNAL_SERVICE_TYPE);
        return;
    }

    Datastore ds = (Datastore) ContextFactory.get().getBean(
        ServletConsts.DATASTORE_BEAN);

    // get form
    Form form;
    try {
      form = Form.retrieveForm(odkId, ds, user, userService.getCurrentRealm());
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    }
    String appName = this.getServletContext().getInitParameter(
        "application_name");
    String serverURL = getServerURL(req);

    WorksheetCreator ws = (WorksheetCreator) ContextFactory.get().getBean(
        ServletConsts.WORKSHEET_BEAN);

    try {
      ws.worksheetCreator(appName, serverURL, spreadsheetName, esType,
          form, ds, user);
    } catch (ODKExternalServiceException e) {
		e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
          .getMessage());
    } catch (ODKDatastoreException e) {
		e.printStackTrace();
	      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
	              .getMessage());
	}

  }

}
