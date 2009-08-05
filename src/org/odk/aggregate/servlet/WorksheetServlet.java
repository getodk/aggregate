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
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
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
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.GoogleSpreadsheet;
import org.odk.aggregate.parser.MultiPartFormData;
import org.odk.aggregate.parser.MultiPartFormItem;
import org.odk.aggregate.table.SubmissionSpreadsheetTable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WorksheetServlet extends ServletUtilBase {

  private static final int PROPOGATION_DELAY = 10000;

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 3054003683995535651L;

  /**
   * URI from base
   */
  public static final String ADDR = "worksheet";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Populate Worksheet with Data";


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
    if (odkFormKey == null) {
      errorMissingKeyParam(resp);
      return;
    }

    String spreadsheetName = getParameter(req, ServletConsts.SPREADSHEET_NAME_PARAM);
    if (spreadsheetName == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }

    // TODO: Remove horrible hack once task API is available
    // App must wait an indeterminate amount of time until spreadsheet has 
    // propogated and is ready to be accessed
    try {
      Thread.sleep(PROPOGATION_DELAY);
    } catch (InterruptedException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    
    // get form
    EntityManager em = EMFactory.get().createEntityManager();
    Key formKey = KeyFactory.stringToKey(odkFormKey);
    Form form = em.getReference(Form.class, formKey);

    beginBasicHtmlResponse(TITLE_INFO, resp, true); // header info
    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H3, ServletConsts.SPREADSHEET_EXPLANATION + "<FONT COLOR=0000FF>" + form.getViewableName() + "</FONT>"));

    String sessionToken;
    try {
      sessionToken = verifyGDataAuthorization(req, resp, ServletConsts.SPREADSHEET_SCOPE);
    } catch (ODKGDataAuthenticationError e) {
      return; // verifyGDataAuthroization function formats response
    } catch (ODKGDataServiceNotAuthenticated e) {
      out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
      out.write(HtmlUtil.createFormBeginTag(generateAuthorizationURL(req,
          ServletConsts.SPREADSHEET_SCOPE), null, ServletConsts.POST));
      out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null,
          ServletConsts.AUTHORIZE_DATA_TRANSFER_BUTTON_TXT));
      out.write(HtmlConsts.FORM_CLOSE);
      finishBasicHtmlResponse(resp);

      return;
    }

    // one time transfer of data to spreadsheet
    createFormButtonWithParams(out, odkFormKey, sessionToken, spreadsheetName);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
        ServletConsts.CONTINUOUS_TRANSFER_PARAM, Boolean.FALSE.toString()));
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null,
        ServletConsts.ONE_TIME_DATA_TRANSFER_BUTTON_TXT));
    out.write(HtmlConsts.FORM_CLOSE);

    // continuous transfer data to spreadsheet as new data comes in
    createFormButtonWithParams(out, odkFormKey, sessionToken, spreadsheetName);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
        ServletConsts.CONTINUOUS_TRANSFER_PARAM, Boolean.TRUE.toString()));
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null,
        ServletConsts.CONTINUOUS_DATA_TRANSFER_BUTTON_TXT));
    out.write(HtmlConsts.LINE_BREAK + ServletConsts.ODK_PERMANENT_ACCESS_WARNING);
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(HtmlConsts.FORM_CLOSE);

    finishBasicHtmlResponse(resp);

    em.close();

  }

  private void createFormButtonWithParams(PrintWriter out, String odkFormKey, String sessionToken,
      String spreadsheetName) throws UnsupportedEncodingException {
    out.write(HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createFormBeginTag(ADDR, ServletConsts.MULTIPART_FORM_DATA,
        ServletConsts.POST));
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.ODK_FORM_KEY,
        encodeParameter(odkFormKey)));
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, ServletConsts.TOKEN_PARAM,
        encodeParameter(sessionToken)));
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
        ServletConsts.SPREADSHEET_NAME_PARAM, encodeParameter(spreadsheetName)));
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

    String spreadsheetName = null;
    Boolean continuous = false;
    String token = null;
    String odkFormKey = null;

    // process form
    try {
      MultiPartFormData uploadedFormItems = new MultiPartFormData(req);

      MultiPartFormItem spreadsheetNameData =
          uploadedFormItems.getFormDataByFieldName(ServletConsts.SPREADSHEET_NAME_PARAM);
      if (spreadsheetNameData != null) {
        spreadsheetName =
            URLDecoder.decode(spreadsheetNameData.getStream().toString(),
                ServletConsts.ENCODE_SCHEME);
      }

      MultiPartFormItem continuousData =
          uploadedFormItems.getFormDataByFieldName(ServletConsts.CONTINUOUS_TRANSFER_PARAM);
      if (continuousData != null) {
        String value =
            URLDecoder.decode(continuousData.getStream().toString(), ServletConsts.ENCODE_SCHEME);
        continuous = Boolean.parseBoolean(value);
      }

      MultiPartFormItem tokenData =
          uploadedFormItems.getFormDataByFieldName(ServletConsts.TOKEN_PARAM);
      if (tokenData != null) {
        token = URLDecoder.decode(tokenData.getStream().toString(), ServletConsts.ENCODE_SCHEME);
      }

      MultiPartFormItem formKeyData =
          uploadedFormItems.getFormDataByFieldName(ServletConsts.ODK_FORM_KEY);
      if (formKeyData != null) {
        odkFormKey =
            URLDecoder.decode(formKeyData.getStream().toString(), ServletConsts.ENCODE_SCHEME);
      }

    } catch (FileUploadException e) {
      e.printStackTrace(resp.getWriter());
    }


    try {
      if (spreadsheetName != null && token != null && odkFormKey != null) {
        // get form
        EntityManager em = EMFactory.get().createEntityManager();
        Key formKey = KeyFactory.stringToKey(odkFormKey);
        Form form = em.getReference(Form.class, formKey);

        GoogleSpreadsheet spreadsheet = form.getExternalRepoWithName(spreadsheetName);

        // verify form has a spreadsheet element
        if (spreadsheet == null) {
          errorRetreivingData(resp);
          return;
        }

        SpreadsheetService service = new SpreadsheetService(this
            .getServletContext().getInitParameter("application_name"));
        service.setAuthSubToken(token, null);

        // TODO: REMOVE after bug is fixed
        // http://code.google.com/p/gdata-java-client/issues/detail?id=103
        service.setProtocolVersion(SpreadsheetService.Versions.V1);

        
        try {
          SubmissionSpreadsheetTable subResults =
              new SubmissionSpreadsheetTable(form, req.getServerName(), em, this
                  .getServletContext().getInitParameter("application_name"));

          // TODO: make more robust (currently assuming nothing has touched the
          // sheet)
          // get worksheet
          WorksheetEntry worksheet =
              subResults.getWorksheet(service, spreadsheet.getSpreadsheetKey(), "Sheet 1");

          // verify worksheet was found
          if (worksheet == null) {
            resp
                .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "COULD NOT FIND WORKSHEET");
            return;
          }
          subResults.generateWorksheet(service, worksheet);
        } catch (ODKIncompleteSubmissionData e1) {
          errorRetreivingData(resp);
          return;
        }

        if (continuous) {
          spreadsheet.setAuthToken(token);
          spreadsheet.updateReadyValue();
        } else {
          form.removeExternalRepo(spreadsheet);

          // remove spreadsheet permission as no longer needed
          try {
            AuthSubUtil.revokeToken(token, null);
          } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

        }
        resp.sendRedirect(ServletConsts.WEB_ROOT);
        em.close();

      } else {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_INFO);
        return;
      }

    } catch (ServiceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }



}
