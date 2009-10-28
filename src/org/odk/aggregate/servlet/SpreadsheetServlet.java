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

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKGDataAuthenticationError;
import org.odk.aggregate.exception.ODKGDataServiceNotAuthenticated;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.remoteserver.GoogleSpreadsheet;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
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
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }

    // get parameter
    String spreadsheetName = getParameter(req, ServletConsts.SPREADSHEET_NAME_PARAM);
    String odkFormKey = getParameter(req, ServletConsts.ODK_FORM_KEY);
    String docSessionToken = getParameter(req, ServletConsts.DOC_AUTH);
    String spreadSessionToken = getParameter(req, ServletConsts.SPREAD_AUTH);
    String esType = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);
    String tokenTypeString = getParameter(req, TOKEN_TYPE);

    Map<String, String> params = new HashMap<String, String>();
    params.put(ServletConsts.SPREADSHEET_NAME_PARAM, spreadsheetName);
    params.put(ServletConsts.ODK_FORM_KEY, odkFormKey);
    params.put(ServletConsts.DOC_AUTH, docSessionToken);
    params.put(ServletConsts.SPREAD_AUTH, spreadSessionToken);
    params.put(ServletConsts.EXTERNAL_SERVICE_TYPE, esType);

    if (spreadsheetName == null || odkFormKey == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_INFO);
      return;
    }

    TokenType tokenType = TokenType.NONE;

    if (tokenTypeString != null) {
      tokenType = TokenType.valueOf(tokenTypeString);
    }


    try {

      if (tokenType.equals(TokenType.DOC)) {
        try {
          docSessionToken = verifyGDataAuthorization(req, resp, ServletConsts.DOCS_SCOPE);
          params.put(ServletConsts.DOC_AUTH, docSessionToken);
        } catch (ODKGDataAuthenticationError e) {
          return; // verifyGDataAuthroization function formats response
        } catch (ODKGDataServiceNotAuthenticated e) {
          // do nothing already set to null
        }
      }
      if (tokenType.equals(TokenType.SPREAD)) {
        try {
          spreadSessionToken = verifyGDataAuthorization(req, resp, ServletConsts.SPREADSHEET_SCOPE);
          params.put(ServletConsts.SPREAD_AUTH, spreadSessionToken);
        } catch (ODKGDataAuthenticationError e) {
          return; // verifyGDataAuthroization function formats response
        } catch (ODKGDataServiceNotAuthenticated e) {
          // do nothing already set to null
        }
      }

      // still need to obtain more authorizations
      if (docSessionToken == null || spreadSessionToken == null) {
        beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
        if (docSessionToken == null) {
          params.put(TOKEN_TYPE, TokenType.DOC.toString());
          String authButton =
              generateAuthButton(ServletConsts.DOCS_SCOPE,
                  ServletConsts.AUTHORIZE_SPREADSHEET_CREATION, params, req, resp);
          resp.getWriter().print(authButton);
        } else {
          resp.getWriter().print("Completed Doc Authorization <br>");
        }

        if (spreadSessionToken == null) {
          params.put(TOKEN_TYPE, TokenType.SPREAD.toString());
          String authButton =
              generateAuthButton(ServletConsts.SPREADSHEET_SCOPE,
                  ServletConsts.AUTHORIZE_DATA_TRANSFER_BUTTON_TXT, params, req, resp);
          resp.getWriter().print(authButton);
        } else {
          resp.getWriter().print("Completed Spreadsheet Authorization <br>");
        }

        finishBasicHtmlResponse(resp);
        return;
      }



      // setup service
      DocsService service =
          new DocsService(this.getServletContext().getInitParameter("application_name"));
      service.setAuthSubToken(docSessionToken, null);

      // create spreadsheet
      DocumentListEntry createdEntry = new SpreadsheetEntry();
      createdEntry.setTitle(new PlainTextConstruct(spreadsheetName));

      DocumentListEntry updatedEntry =
          service.insert(new URL(ServletConsts.DOC_FEED), createdEntry);

      // get key
      String docKey = updatedEntry.getKey();
      String sheetKey =
          docKey.substring(docKey.lastIndexOf(ServletConsts.DOCS_PRE_KEY)
              + ServletConsts.DOCS_PRE_KEY.length());

      // get form
      EntityManager em = EMFactory.get().createEntityManager();
      Key formKey = KeyFactory.stringToKey(odkFormKey);
      Form form = em.getReference(Form.class, formKey);

      // create spreadsheet
      GoogleSpreadsheet spreadsheet = new GoogleSpreadsheet(spreadsheetName, sheetKey);
      spreadsheet.setAuthToken(spreadSessionToken);

      form.addGoogleSpreadsheet(spreadsheet);
      em.close();

      // remove docs permission no longer needed
      try {
        AuthSubUtil.revokeToken(docSessionToken, null);
      } catch (GeneralSecurityException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      TaskOptions task = TaskOptions.Builder.url("/" + WorksheetServlet.ADDR);
      task.method(TaskOptions.Method.GET);
      task.countdownMillis(DELAY);
      task.param(ServletConsts.SPREADSHEET_NAME_PARAM, spreadsheetName);
      task.param(ServletConsts.ODK_FORM_KEY, odkFormKey);
      task.param(ServletConsts.EXTERNAL_SERVICE_TYPE, esType);

      Queue queue = QueueFactory.getDefaultQueue();
      try {
        queue.add(task);
      } catch (Exception e) {
        System.out.println("PROBLEM WITH TASK");
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
    NONE, DOC, SPREAD;
  }

}
