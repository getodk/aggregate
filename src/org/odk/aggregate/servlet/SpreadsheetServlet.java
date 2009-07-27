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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.SpreadsheetEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKGDataAuthenticationError;
import org.odk.aggregate.exception.ODKGDataServiceNotAuthenticated;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.GoogleSpreadsheet;
import org.odk.aggregate.parser.MultiPartFormData;
import org.odk.aggregate.parser.MultiPartFormItem;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SpreadsheetServlet extends ServletUtilBase {

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
   * Handler for HTTP Get request to create xform upload page
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
    String odkFormKey = getParameter(req, ServletConsts.ODK_FORM_KEY);
    if(odkFormKey == null) {
      errorMissingKeyParam(resp);
      return;
    }

    // get form
    EntityManager em = EMFactory.get().createEntityManager();
    Key formKey = KeyFactory.stringToKey(odkFormKey);
    Form form = em.getReference(Form.class, formKey);

    
    beginBasicHtmlResponse(TITLE_INFO, resp, true); // header info
    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H3, ServletConsts.GOOGLE_DOC_EXPLANATION + "<FONT COLOR=0000FF>" + form.getViewableName() + "</FONT>"));
    
    em.close();
    
    String sessionToken;
    try {
      sessionToken = verifyGDataAuthorization(req, resp, ServletConsts.DOCS_SCOPE);
    } catch (ODKGDataAuthenticationError e) {
      return; // verifyGDataAuthroization function formats response
    } catch (ODKGDataServiceNotAuthenticated e) {
      out.write(HtmlConsts.LINE_BREAK);
      out.write(HtmlUtil.createFormBeginTag(generateAuthorizationURL(req, ServletConsts.DOCS_SCOPE), null,
          ServletConsts.POST));
      out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null,
          ServletConsts.AUTHORIZE_SPREADSHEET_CREATION));
      out.write(HtmlConsts.FORM_CLOSE);
      finishBasicHtmlResponse(resp);      
      return;
    }
 
    out.write(HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createFormBeginTag(ADDR, ServletConsts.MULTIPART_FORM_DATA,
        ServletConsts.POST));
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.ODK_FORM_KEY, encodeParameter(odkFormKey)));
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.TOKEN_PARAM, encodeParameter(sessionToken)));
    out.write(ServletConsts.SPEADSHEET_NAME_LABEL + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_TEXT,
        ServletConsts.SPREADSHEET_NAME_PARAM, null));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null,
        ServletConsts.CREATE_SPREADSHEET_BUTTON_LABEL));
    out.write(HtmlConsts.FORM_CLOSE);
    finishBasicHtmlResponse(resp);
  }

  /**
   * Handler for HTTP Post request that takes an xform, parses, and saves a
   * parsed version in the datastore
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }

    // verify request is multipart
    if (!ServletFileUpload.isMultipartContent(req)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.NO_MULTI_PART_CONTENT);
      return;
    }
    
    try {
      // process form
      MultiPartFormData uploadedFormItems = new MultiPartFormData(req);

      MultiPartFormItem spreadsheetNameData =
          uploadedFormItems.getFormDataByFieldName(ServletConsts.SPREADSHEET_NAME_PARAM);

      MultiPartFormItem tokenData = uploadedFormItems.getFormDataByFieldName(ServletConsts.TOKEN_PARAM);
      
      MultiPartFormItem formKeyData = uploadedFormItems.getFormDataByFieldName(ServletConsts.ODK_FORM_KEY);
      
      String spreadsheetName = null;
      if (spreadsheetNameData != null) {
        spreadsheetName = URLDecoder.decode(spreadsheetNameData.getStream().toString(), ServletConsts.ENCODE_SCHEME);
      }
      
      String token = null;
      if (tokenData != null) {
        token = URLDecoder.decode(tokenData.getStream().toString(), ServletConsts.ENCODE_SCHEME);
      }

      String odkFormKey = null; 
      if (formKeyData != null) {
        odkFormKey = URLDecoder.decode(formKeyData.getStream().toString(), ServletConsts.ENCODE_SCHEME);
      }

      
      if (spreadsheetName != null && token != null && odkFormKey != null) {
        // setup service
        DocsService service = new DocsService(this.getServletContext().getInitParameter("application_name"));
        service.setAuthSubToken(token, null);

        // create spreadsheet
        DocumentListEntry createdEntry = new SpreadsheetEntry();
        createdEntry.setTitle(new PlainTextConstruct(spreadsheetName));
        
        DocumentListEntry updatedEntry = service.insert(new URL(ServletConsts.DOC_FEED), createdEntry);

        // get key
        String docKey =  updatedEntry.getKey();
        String sheetKey = docKey.substring(docKey.lastIndexOf(ServletConsts.DOCS_PRE_KEY) + ServletConsts.DOCS_PRE_KEY.length());
        
        // get form
        EntityManager em = EMFactory.get().createEntityManager();
        Key formKey = KeyFactory.stringToKey(odkFormKey);
        Form form = em.getReference(Form.class, formKey);        
        form.addExternalRepo(new GoogleSpreadsheet(spreadsheetName, sheetKey));
        em.close();

        // remove docs permission no longer needed
        try {
          AuthSubUtil.revokeToken(token, null);
        } catch (GeneralSecurityException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(ServletConsts.ODK_FORM_KEY, KeyFactory.keyToString(form.getKey()));
        properties.put(ServletConsts.SPREADSHEET_NAME_PARAM, spreadsheetName);
        
        resp.sendRedirect(HtmlUtil.createLinkWithProperties(WorksheetServlet.ADDR, properties));
        
      } else {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_INFO);
        return;
      }
      
    } catch (FileUploadException e) {
      e.printStackTrace(resp.getWriter());
    } catch (AuthenticationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ServiceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
