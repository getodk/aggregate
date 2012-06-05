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

package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ParserConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.QueryResult;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.utils.WebCursorUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

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
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    // get parameters

    // the formId of the form submissions to download
    String formId = getParameter(req, ServletConsts.FORM_ID);
    if (formId == null) {
      errorMissingKeyParam(resp);
      return;
    }
    if ( formId.contains(ParserConsts.FORWARD_SLASH) ) {
      formId = formId.replaceAll(ParserConsts.FORWARD_SLASH, ParserConsts.FORWARD_SLASH_SUBSTITUTION);
    }
    
    // the cursor string
    String websafeCursorString = getParameter(req, ServletConsts.CURSOR);
    QueryResumePoint cursor = WebCursorUtils.parseCursorParameter(websafeCursorString);

    // the number of entries
    int numEntries = DEFAULT_NUM_ENTRIES;
    String numEntriesString = getParameter(req, ServletConsts.NUM_ENTRIES);
    if (numEntriesString != null && numEntriesString.trim().length() != 0) {
      numEntries = Integer.parseInt(numEntriesString.trim());
    }

    IForm form;
    try {
      form = FormFactory.retrieveFormByFormId(formId, cc);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      odkIdNotFoundError(resp);
      return;
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      quotaExceededError(resp);
      return;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      datastoreError(resp);
      return;
    }

    if (!form.hasValidFormDefinition()) {
      errorRetreivingData(resp);
      return; // ill-formed definition
    }

    addOpenRosaHeaders(resp);
    try {
      TopLevelDynamicBase tbl = (TopLevelDynamicBase) form.getTopLevelGroupElement()
          .getFormDataModel().getBackingObjectPrototype();

      // Query by lastUpdateDate, ordered by lastUpdateDate and secondarily by
      // uri
      // Submissions may be partially uploaded and are marked completed once
      // they
      // are fully uploaded. We snarf everything.
      Query query = cc.getDatastore().createQuery(tbl, "SubmissionDownloadListServlet.doGet", cc.getCurrentUser());
      query.addSort(tbl.lastUpdateDate, Query.Direction.ASCENDING);
      query.addFilter(tbl.isComplete, FilterOperation.EQUAL, true);

      QueryResult result = query.executeQuery(cursor, numEntries);
      List<String> uriList = new ArrayList<String>();
      for (CommonFieldsBase cb : result.getResultList()) {
        uriList.add(cb.getUri());
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

      QueryResumePoint qrp = result.getResumeCursor();
      if ( qrp == null ) {
        websafeCursorString = null;
      } else {
        websafeCursorString = qrp.asWebsafeCursor();
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
      resp.setContentType(HtmlConsts.RESP_TYPE_XML);
      addOpenRosaHeaders(resp);

      PrintWriter output = resp.getWriter();
      serializer.setOutput(output);
      // setting the response content type emits the xml header.
      // just write the body here...
      d.writeChildren(serializer);
      serializer.flush();
      resp.setStatus(HttpServletResponse.SC_OK);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      errorRetreivingData(resp);
    }
  }

}
