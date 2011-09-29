/*
 * Copyright (C) 2011 University of Washington.
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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.odk.aggregate.BriefcaseAuth;
import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.PersistConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.form.Form;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * Servlet to generate the XML list of submission instanceIDs for a given form.
 * This is a full list of all submissions. It is assumed that ODK will have the
 * VM space to be able to emit this list.
 * <p>
 * The server request takes three parameters:
 * </p>
 * <ol>
 * <li>FormId of the form submissions to download.</li>
 * <li>A websafe cursor string containing a startDate and a primary key after
 * which to begin returning results (may also be null).</li>
 * <li>A numEntries value specifying the total number of entries to retrieve.</li>
 * </ol>
 * <p>
 * 10MB string space / 55 char per uuid = 181,818 records. == numEntries
 * </p>
 * <p>
 * The returned submissions are ordered by:
 * </p>
 * <ol>
 * <li>lastUpdateDate (ascending) and</li>
 * <li>URI (ascending).</li>
 * </ol>
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class SubmissionDownloadListServlet extends ServletUtilBase {

  private static final String ID_FRAGMENT_TAG = "idChunk";

  private static final String ID_LIST_TAG = "idList";

  private static final String CURSOR_TAG = "resumptionCursor";

  private static final String ID_TAG = "id";
  private static int DEFAULT_NUM_ENTRIES = 180000;

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 13236849409070038L;

  /**
   * URI from base
   */
  public static final String ADDR = "view/submissionList";

  private static final String XML_TAG_NAMESPACE = "http://opendatakit.org/submissions";

  /**
   * Handler for HTTP Get request that responds with an XML list of instanceIDs
   * on the system.
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    String briefcaseParam = req.getHeader(ServletConsts.BRIEFCASE_APP_TOKEN_HEADER);

    // copied from FormUploadServlet
    String authParam = getParameter(req, ServletConsts.AUTHENTICATION);

    User user = null;

    if (authParam != null && authParam.equalsIgnoreCase(ServletConsts.AUTHENTICATION_OAUTH)) {
      // Try OAuth authentication
      try {
        OAuthService oauth = OAuthServiceFactory.getOAuthService();
        user = oauth.getCurrentUser();
        if (user == null) {
          resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ErrorConsts.OAUTH_ERROR);
          return;
        }
      } catch (OAuthRequestException e) {
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ErrorConsts.OAUTH_ERROR + "\n Reason: "
            + e.getLocalizedMessage());
        return;
      }
    } else if (briefcaseParam != null && briefcaseParam.length() != 0) {
      // Verify briefcase token
      if (!BriefcaseAuth.verifyBriefcaseAuthToken(briefcaseParam)) {
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid briefcase application token");
        return;
      }
    } else {
      // Try User Service authentication
      UserService userService = UserServiceFactory.getUserService();
      user = userService.getCurrentUser();

      // verify user is logged in
      if (!verifyCredentials(req, resp)) {
        return;
      }
    }

    EntityManager em = EMFactory.get().createEntityManager();

    // get parameters

    // the formId of the form submissions to download
    String formId = getParameter(req, ServletConsts.FORM_ID);
    if (formId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    // the cursor string
    String websafeCursorString = getParameter(req, ServletConsts.CURSOR);

    // the number of entries
    int numEntries = DEFAULT_NUM_ENTRIES;
    String numEntriesString = getParameter(req, ServletConsts.NUM_ENTRIES);
    if (numEntriesString != null && numEntriesString.trim().length() != 0) {
      numEntries = Integer.parseInt(numEntriesString.trim());
    }

    Form form;
    try {
      form = Form.retrieveForm(em, formId);
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    }

    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(numEntries);
    if (websafeCursorString != null && websafeCursorString.length() != 0) {
      fetchOptions.startCursor(Cursor.fromWebSafeString(websafeCursorString));
    }

    Query keyQuery = new Query(form.getOdkId());
    keyQuery.addSort(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.SortDirection.ASCENDING);
    keyQuery.addFilter(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG,
        Query.FilterOperator.GREATER_THAN, TableConsts.EPOCH);
    keyQuery.setKeysOnly();

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = ds.prepare(keyQuery);

    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);

    List<String> uriList = new ArrayList<String>();
    for (Entity entity : results) {
      uriList.add(KeyFactory.keyToString(entity.getKey()));
    }

    // if we had no results, return the websafe cursor that came in...
    if (uriList.size() != 0) {
      websafeCursorString = results.getCursor().toWebSafeString();
    }

    Document d = new Document();
    d.setStandalone(true);
    d.setEncoding(HtmlConsts.UTF8_ENCODE);
    Element eWrapper = d.createElement(XML_TAG_NAMESPACE, ID_FRAGMENT_TAG);
    eWrapper.setPrefix(null, XML_TAG_NAMESPACE);
    d.addChild(0, Node.ELEMENT, eWrapper);
    Element eList = d.createElement(XML_TAG_NAMESPACE, ID_LIST_TAG);
    eList.setPrefix(null, XML_TAG_NAMESPACE);
    eWrapper.addChild(0, Node.ELEMENT, eList);
    int idx = 0;
    for (String uri : uriList) {
      Element e = eList.createElement(XML_TAG_NAMESPACE, ID_TAG);
      e.setPrefix(null, XML_TAG_NAMESPACE);
      e.addChild(0, Node.TEXT, uri);
      eList.addChild(idx++, Node.ELEMENT, e);
      eList.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
      --numEntries;
      if (numEntries <= 0)
        break;
    }

    if (websafeCursorString != null) {
      // emit the cursor value...
      Element eCursorContinue = d.createElement(XML_TAG_NAMESPACE, CURSOR_TAG);
      eCursorContinue.setPrefix(null, XML_TAG_NAMESPACE);
      eCursorContinue.addChild(0, Node.TEXT, websafeCursorString);
      eWrapper.addChild(1, Node.ELEMENT, eCursorContinue);
    }

    KXmlSerializer serializer = new KXmlSerializer();

    resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
    resp.setContentType(ServletConsts.RESP_TYPE_XML);
    addOpenRosaHeaders(resp);

    PrintWriter output = resp.getWriter();
    serializer.setOutput(output);
    // setting the response content type emits the xml header.
    // just write the body here...
    d.writeChildren(serializer);
    serializer.flush();
    resp.setStatus(HttpServletResponse.SC_OK);

    em.close();
  }

}
