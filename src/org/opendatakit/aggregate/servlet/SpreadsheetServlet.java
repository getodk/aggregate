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
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKExternalServiceAuthenticationError;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKExternalServiceNotAuthenticated;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.GoogleSpreadsheet;
import org.opendatakit.aggregate.externalservice.constants.ExternalServiceOption;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.task.WorksheetCreator;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.SpreadsheetEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class SpreadsheetServlet extends ServletUtilBase {

  // TODO: change code so a delay is not required
  private static final int DELAY = 15000;

  private static final String TOKEN_TYPE = "tokenType";

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 3675734774978838172L;

  /**
   * URI from base
   */
  public static final String ADDR = "spreadsheet";

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
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    // // verify user is logged in
    // if (!verifyCredentials(req, resp)) {
    // return;
    // }

      UserService userService = (UserService) ContextFactory.get().getBean(
          ServletConsts.USER_BEAN);
      User user = userService.getCurrentUser();
	  
    //
    // get parameter
    String spreadsheetName = getParameter(req,
        ServletConsts.SPREADSHEET_NAME_PARAM);
    String formId = getParameter(req, ServletConsts.ODK_ID);
    String docSessionToken = getParameter(req, ServletConsts.DOC_AUTH);
    String spreadSessionToken = getParameter(req, ServletConsts.SPREAD_AUTH);
    String pleaseWaitBool = getParameter(req, ServletConsts.PLEASE_WAIT_PARAM);
    String esTypeString = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);
    String tokenTypeString = getParameter(req, TOKEN_TYPE);

    Map<String, String> params = new HashMap<String, String>();
    params.put(ServletConsts.SPREADSHEET_NAME_PARAM, spreadsheetName);
    params.put(ServletConsts.ODK_ID, formId);
    params.put(ServletConsts.DOC_AUTH, docSessionToken);
    params.put(ServletConsts.SPREAD_AUTH, spreadSessionToken);
    params.put(ServletConsts.PLEASE_WAIT_PARAM, pleaseWaitBool);
    params.put(ServletConsts.EXTERNAL_SERVICE_TYPE, esTypeString);
   
    if (spreadsheetName == null || formId == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          ErrorConsts.MISSING_FORM_INFO);
      return;
    }

    TokenType tokenType = TokenType.NONE;

    if (tokenTypeString != null) {
      tokenType = TokenType.valueOf(tokenTypeString);
    }

    try {

      if (tokenType.equals(TokenType.DOC)) {
        try {
          docSessionToken = verifyGDataAuthorization(req, resp,
              ServletConsts.DOCS_SCOPE);
          params.put(ServletConsts.DOC_AUTH, docSessionToken);
        } catch (ODKExternalServiceAuthenticationError e) {
          return; // verifyGDataAuthroization function formats response
        } catch (ODKExternalServiceNotAuthenticated e) {
          // do nothing already set to null
        }
      }
      if (tokenType.equals(TokenType.SPREAD)) {
        try {
          spreadSessionToken = verifyGDataAuthorization(req, resp,
              ServletConsts.SPREADSHEET_SCOPE);
          params.put(ServletConsts.SPREAD_AUTH, spreadSessionToken);
        } catch (ODKExternalServiceAuthenticationError e) {
          return; // verifyGDataAuthroization function formats response
        } catch (ODKExternalServiceNotAuthenticated e) {
          // do nothing already set to null
        }
      }

      // still need to obtain more authorizations
      if (docSessionToken == null || spreadSessionToken == null) {
        beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
        if (docSessionToken == null) {
          params.put(TOKEN_TYPE, TokenType.DOC.toString());
          String authButton = generateAuthButton(ServletConsts.DOCS_SCOPE,
              ServletConsts.AUTHORIZE_SPREADSHEET_CREATION, params, req, resp);
          resp.getWriter().print(authButton);
        } else {
          resp.getWriter().print("Completed Doc Authorization <br>");
        }

        if (spreadSessionToken == null) {
          params.put(TOKEN_TYPE, TokenType.SPREAD.toString());
          String authButton = generateAuthButton(
              ServletConsts.SPREADSHEET_SCOPE,
              ServletConsts.AUTHORIZE_DATA_TRANSFER_BUTTON_TXT, params, req,
              resp);
          resp.getWriter().print(authButton);
        } else {
          resp.getWriter().print("Completed Spreadsheet Authorization <br>");
        }

        finishBasicHtmlResponse(resp);
        return;
      }

      if(pleaseWaitBool == null) {
          beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info);
          resp.getWriter().print("NOTE: It may take a little while for your spreadsheet to be created (Screeen will do nothing, just wait)");
          params.put(ServletConsts.PLEASE_WAIT_PARAM, "false");
          resp.getWriter().print(HtmlUtil.createHtmlButtonToGetServlet(SpreadsheetServlet.ADDR, "CREATE SPREADSHEET", params));
          finishBasicHtmlResponse(resp);
          return;
      }
  
      // setup service
      DocsService service = new DocsService(this.getServletContext()
          .getInitParameter("application_name"));
      service.setAuthSubToken(docSessionToken, null);

      // create spreadsheet
      DocumentListEntry createdEntry = new SpreadsheetEntry();
      createdEntry.setTitle(new PlainTextConstruct(spreadsheetName));

      DocumentListEntry updatedEntry = service.insert(new URL(
          ServletConsts.DOC_FEED), createdEntry);

      // get key
      String docKey = updatedEntry.getKey();
      String sheetKey = docKey.substring(docKey
          .lastIndexOf(ServletConsts.DOCS_PRE_KEY)
          + ServletConsts.DOCS_PRE_KEY.length());

      // get form
      Datastore ds = (Datastore) ContextFactory.get().getBean(
          ServletConsts.DATASTORE_BEAN);
 	  FormDefinition fd = null;
 	  Form form;
      try {
     	fd = FormDefinition.getFormDefinition(formId, ds, user);
     	form = Form.retrieveForm(formId, ds, user, userService.getCurrentRealm());
      } catch (ODKFormNotFoundException e) {
        odkIdNotFoundError(resp);
        return;
      }

      if ( fd == null ) {
    	  odkIdNotFoundError(resp);
    	  return;
      }
      
      ExternalServiceOption esType = ExternalServiceOption.valueOf(esTypeString);

      // create spreadsheet
      GoogleSpreadsheet spreadsheet;
      try {
        spreadsheet = new GoogleSpreadsheet(fd, spreadsheetName, sheetKey, spreadSessionToken,
        									esType, ds, user);
      } catch (ODKEntityPersistException e1) {
        // TODO FIGURE out how to handle exception
        e1.printStackTrace();
        return;
      } catch (ODKDatastoreException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return;
	}
      
      String appName = this.getServletContext().getInitParameter(
          "application_name");
      String serverURL = getServerURL(req);

      WorksheetCreator ws = (WorksheetCreator) ContextFactory.get().getBean(
          ServletConsts.WORKSHEET_BEAN);

      try {
        ws.createWorksheetTask(appName, serverURL, spreadsheetName, esType, DELAY, form,
            ds, user);
      } catch (ODKExternalServiceException e) {
  		e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
            .getMessage());
      } catch (ODKDatastoreException e) {
		e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
                .getMessage());
	}

      // remove docs permission no longer needed
      try {
        AuthSubUtil.revokeToken(docSessionToken, null);
      } catch (GeneralSecurityException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    } catch (AuthenticationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ServiceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    resp.sendRedirect(ServletConsts.WEB_ROOT);
  }

  private enum TokenType {
    NONE, DOC, SPREAD, PLEASE_WAIT;
  }

}
