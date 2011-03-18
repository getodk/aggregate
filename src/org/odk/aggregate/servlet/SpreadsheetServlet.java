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
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.remoteserver.GoogleSpreadsheetOAuth;
import org.odk.aggregate.form.remoteserver.OAuthToken;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.SpreadsheetEntry;
import com.google.gdata.util.ServiceException;

public class SpreadsheetServlet extends ServletUtilBase {

  // TODO: change code so a delay is not required
  private static final int DELAY = 15000;

  private static final String CALLBACK = "callback";

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
    String odkIdParam = getParameter(req, ServletConsts.ODK_ID);
    String sessionToken = getParameter(req, ServletConsts.OAUTH_TOKEN);
    String sessionTokenSecret = getParameter(req, ServletConsts.OAUTH_TOKEN_SECRET);
    String esType = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);
    boolean callback = Boolean.parseBoolean(getParameter(req, CALLBACK));

    Map<String, String> params = new HashMap<String, String>();
    params.put(ServletConsts.SPREADSHEET_NAME_PARAM, spreadsheetName);
    params.put(ServletConsts.ODK_ID, odkIdParam);
    params.put(ServletConsts.OAUTH_TOKEN, sessionToken);
    params.put(ServletConsts.OAUTH_TOKEN_SECRET, sessionTokenSecret);
    params.put(ServletConsts.EXTERNAL_SERVICE_TYPE, esType);

    if (spreadsheetName == null || odkIdParam == null) {
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
        params.put(ServletConsts.OAUTH_TOKEN, sessionToken);
        params.put(ServletConsts.OAUTH_TOKEN_SECRET, sessionTokenSecret);
        params.remove(CALLBACK);
      } catch (IOException e) {
        return;
      }
    }

    // 1st step: generate the auth button that will send the oauth request to
    // Google and allow the user to grant us access
    if (sessionToken == null || sessionToken.isEmpty()) {
      beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
      params.put(CALLBACK, Boolean.toString(true));
      String authButton = generateAuthButton(ServletConsts.AUTHORIZE_SPREADSHEET_CREATION, params,
          req, resp, ServletConsts.DOCS_SCOPE, ServletConsts.DOCS_SCOPE);
      resp.getWriter().print(authButton);
      finishBasicHtmlResponse(resp);
      return;
    } else {
      resp.getWriter().write(ServletConsts.COMPLETED_AUTH);
    }

    // setup auth token
    OAuthToken authToken = new OAuthToken(sessionToken, sessionTokenSecret);

    // setup service
    DocsService service = new DocsService(this.getServletContext().getInitParameter(
        "application_name"));
    try {
      GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
      oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
      oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
      oauthParameters.setOAuthToken(authToken.getToken());
      oauthParameters.setOAuthTokenSecret(authToken.getTokenSecret());
      service.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
    } catch (OAuthException e) {
      // TODO: handle OAuth failure
      e.printStackTrace();
    }

    // create spreadsheet
    DocumentListEntry createdEntry = new SpreadsheetEntry();
    createdEntry.setTitle(new PlainTextConstruct(spreadsheetName));

    DocumentListEntry updatedEntry;
    try {
      updatedEntry = service.insert(new URL(ServletConsts.DOC_FEED), createdEntry);

      // get key
      String docKey = updatedEntry.getKey();
      String sheetKey = docKey.substring(docKey.lastIndexOf(ServletConsts.DOCS_PRE_KEY)
          + ServletConsts.DOCS_PRE_KEY.length());

      // get form
      EntityManager em = EMFactory.get().createEntityManager();

      Form form;
      try {
        form = Form.retrieveForm(em, odkIdParam);
      } catch (ODKFormNotFoundException e) {
        odkIdNotFoundError(resp);
        return;
      }

      // create spreadsheet
      GoogleSpreadsheetOAuth spreadsheet = new GoogleSpreadsheetOAuth(spreadsheetName, sheetKey);

      form.addGoogleSpreadsheet(spreadsheet);
      em.close();

      TaskOptions task = TaskOptions.Builder.withUrl("/" + WorksheetServlet.ADDR);
      task.method(TaskOptions.Method.GET);
      task.countdownMillis(DELAY);
      task.param(ServletConsts.SPREADSHEET_NAME_PARAM, spreadsheetName);
      task.param(ServletConsts.ODK_ID, odkIdParam);
      task.param(ServletConsts.EXTERNAL_SERVICE_TYPE, esType);

      Queue queue = QueueFactory.getDefaultQueue();
      try {
        queue.add(task);
      } catch (Exception e) {
        System.out.println("PROBLEM WITH TASK");
        e.printStackTrace();
      }
    } catch (ServiceException e) {
      e.printStackTrace();
    }

    // remove docs permission no longer needed
    try {
      GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
      oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
      oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
      oauthParameters.setOAuthToken(authToken.getToken());
      oauthParameters.setOAuthTokenSecret(authToken.getTokenSecret());
      GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
      oauthHelper.revokeToken(oauthParameters);
    } catch (OAuthException e) {
      e.printStackTrace();
    }

    resp.sendRedirect(ServletConsts.WEB_ROOT);
  }
}
