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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceOption;
import org.opendatakit.aggregate.constants.externalservice.SpreadsheetConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceAuthenticationError;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKExternalServiceNotAuthenticated;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.GoogleSpreadsheet;
import org.opendatakit.aggregate.externalservice.OAuthToken;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.MiscTasks.TaskType;
import org.opendatakit.aggregate.task.WorksheetCreator;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class SpreadsheetServlet extends ServletUtilBase {

  private static final String CALLBACK = "callback";

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 3675734774978838172L;

  /**
   * URI from base
   */
  public static final String ADDR = "extern/spreadsheet";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Create Google Doc Spreadsheet";

  /**
   * Handler for HTTP Get request to create a google spreadsheet
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, req);

    // collect and save all request parameters
    String spreadsheetName = getParameter(req, ExternalServiceConsts.EXT_SERV_ADDRESS);
    String formId = getParameter(req, ServletConsts.FORM_ID);
    String sessionToken = getParameter(req, SpreadsheetConsts.OAUTH_TOKEN);
    String sessionTokenSecret = getParameter(req, SpreadsheetConsts.OAUTH_TOKEN_SECRET);
    String esTypeString = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);
    boolean callback = Boolean.parseBoolean(getParameter(req, CALLBACK));

    Map<String, String> params = new HashMap<String, String>();
    params.put(ExternalServiceConsts.EXT_SERV_ADDRESS, spreadsheetName);
    params.put(ServletConsts.FORM_ID, formId);
    params.put(SpreadsheetConsts.OAUTH_TOKEN, sessionToken);
    params.put(SpreadsheetConsts.OAUTH_TOKEN_SECRET, sessionTokenSecret);
    params.put(ServletConsts.EXTERNAL_SERVICE_TYPE, esTypeString);

    if (spreadsheetName == null || formId == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_INFO);
      return;
    }

    // 2nd step: we are receiving the callback from Google, so we need to
    // retrieve the OAuth token
    if (callback) {
      try {
        OAuthToken oauthToken = verifyGDataAuthorization(req, resp);
        sessionToken = oauthToken.getToken();
        sessionTokenSecret = oauthToken.getTokenSecret();
        params.put(SpreadsheetConsts.OAUTH_TOKEN, sessionToken);
        params.put(SpreadsheetConsts.OAUTH_TOKEN_SECRET, sessionTokenSecret);
        params.remove(CALLBACK);
      } catch (ODKExternalServiceAuthenticationError e) {
        return; // verifyGDataAuthroization function formats response
      } catch (ODKExternalServiceNotAuthenticated e) {
        // do nothing already set to null
      }
    }

    // 1st step: generate the auth button that will send the oauth request to
    // Google and allow the user to grant us access
    if (sessionToken == null || sessionToken.isEmpty()) {
      beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info
      params.put(CALLBACK, Boolean.toString(true));
      String authButton = generateAuthButton(SpreadsheetConsts.AUTHORIZE_SPREADSHEET_CREATION,
          params, req, resp, SpreadsheetConsts.DOCS_SCOPE, SpreadsheetConsts.SPREADSHEETS_SCOPE);
      resp.getWriter().print(authButton);
      finishBasicHtmlResponse(resp);
      return;
    } else {
      resp.getWriter().write(SpreadsheetConsts.COMPLETED_AUTH);
    }

    // authorization is complete so now we can create the spreadsheet
    Form form = null;
    try {
      form = Form.retrieveForm(formId, cc);
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    }

    ExternalServiceOption esType = ExternalServiceOption.valueOf(esTypeString);

    try {
      OAuthToken authToken = new OAuthToken(sessionToken, sessionTokenSecret);
      GoogleSpreadsheet.createSpreadsheet(form, authToken, spreadsheetName, esType, cc);

      WorksheetCreator ws = (WorksheetCreator) cc.getBean(BeanDefs.WORKSHEET_BEAN);

      Map<String,String> parameters = new HashMap<String,String>();
      
      parameters.put(ExternalServiceConsts.EXT_SERV_ADDRESS, spreadsheetName);
      parameters.put(ServletConsts.EXTERNAL_SERVICE_TYPE, esType.toString());
      
      MiscTasks m = new MiscTasks(TaskType.WORKSHEET_CREATE, form, parameters, cc);
      m.persist(cc);
      
  	  CallingContext ccDaemon = ContextFactory.getCallingContext(this, ADDR, req);
	  ccDaemon.setAsDaemon(true);
      ws.createWorksheetTask(form, m.getSubmissionKey(), 1L, ccDaemon);
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      return;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      return;
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	}


    
    System.out.println("USING URL: " + cc.getServerURL());
    resp.sendRedirect(cc.getWebApplicationURL(FormsServlet.ADDR));
  }
}
