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
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.parser.MultiPartFormData;
import org.opendatakit.aggregate.parser.MultiPartFormItem;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Servlet to upload, parse, and save an XForm
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class ServiceAccountPrivateKeyUploadServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -3784460108221008112L;

  /**
   * URI from base
   */
  public static final String ADDR = UIConsts.SERVICE_ACCOUNT_PRIVATE_KEY_UPLOAD_ADDR;

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Google API Credentials Upload";

  private static final String SIMPLE_API_KEY_PARAM = "simple_api_key";
  private static final String PRIVATE_KEY_FILE_PARAM = "private_key_file";
  private static final String CLIENT_ID_PARAM = "client_id";
  private static final String SERVICE_ACCOUNT_EMAIL_PARAM = "service_account_email";

  private static final String UPLOAD_PAGE_BODY_START =
      "<form id=\"service_account_form\""
      + " accept-charset=\"UTF-8\" method=\"POST\" encoding=\"multipart/form-data\" enctype=\"multipart/form-data\""
      + " action=\"";// emit the ADDR
  private static final String UPLOAD_PAGE_BODY_MIDDLE = "\">"
	  + "<div style=\"overflow: auto;\"><h2>Google API Credentials</h2>"
	  + "<p>Please refer to the documentation at <a href=\"http://opendatakit.org/use/aggregate/oauth2-service-account/\" target=\"_blank\">Service Account Configuration</a>.</p>"
      + "<h2>Google Simple API Key</h2>"
      + "<p>Specifying a Simple API Key is recommended but not required for Google Maps visualizations.</p>"
      + "	  <table id=\"uploadTable\">"
      + "	  	<tr>"
      + "	  		<td><label for=\"simple_api_key\">Simple API Key:</label></td>"
      + "	  		<td><input id=\"simple_api_key\" type=\"text\" size=\"80\" name=\"simple_api_key\" /></td>"
      + "	  	</tr>\n"
      + "<tr><td colspan=\"2\"><h2>Google API Service Account information</h2></td></tr>"
      + "<tr><td colspan=\"2\"><p>Google API Service accounts are required when "
      +                  "publishing to Google Spreadsheets and Google FusionTables.</p></td></tr>"
      + "	  	<tr>"
      + "	  		<td><label for=\"private_key_file\">Private key file (.p12 file):</label></td>"
      + "	  		<td><input id=\"private_key_file\" type=\"file\" size=\"80\" class=\"gwt-Button\""
      + "	  			name=\"private_key_file\" /></td>"
      + "	  	</tr>\n"
      + "	  	<tr>"
      + "	  		<td><label for=\"client_id\">Client ID:</label></td>"
      + "	  		<td><input id=\"client_id\" type=\"text\" size=\"80\" name=\"client_id\" /></td>"
      + "	  	</tr>"
      + "      <tr>"
      + "         <td><label for=\"service_account_email\">Email address:</label></td>"
      + "         <td><input id=\"service_account_email\" type=\"text\" size=\"80\" name=\"service_account_email\" /></td>"
      + "      </tr>"
      + "	  	<tr>"
      + "	  		<td><input type=\"submit\" name=\"button\" class=\"gwt-Button\" value=\"Upload Google Credentials\" /></td>"
      + "	  		<td />"
      + "	  	</tr>"
      + "	  </table>\n"
      + "	  </form>"
      + "<br></div>\n";

  private static final Log logger = LogFactory.getLog(ServiceAccountPrivateKeyUploadServlet.class);

  /**
   * Handler for HTTP Get request to create xform upload page
   *
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    StringBuilder headerString = new StringBuilder();
    headerString.append("<script type=\"application/javascript\" src=\"");
    headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_SCRIPT_RESOURCE));
    headerString.append("\"></script>");
    headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
    headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_STYLE_RESOURCE));
    headerString.append("\" />");
    headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
    headerString.append(cc.getWebApplicationURL(ServletConsts.UPLOAD_BUTTON_STYLE_RESOURCE));
    headerString.append("\" />");
    headerString.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
    headerString.append(cc.getWebApplicationURL(ServletConsts.AGGREGATE_STYLE));
    headerString.append("\" />");

    // header info
    beginBasicHtmlResponse(TITLE_INFO, headerString.toString(), resp, cc);
    PrintWriter out = resp.getWriter();
    out.write(UPLOAD_PAGE_BODY_START);
    out.write(cc.getWebApplicationURL(ADDR));
    out.write(UPLOAD_PAGE_BODY_MIDDLE);
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
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    // verify request is multipart
    if (!ServletFileUpload.isMultipartContent(req)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.NO_MULTI_PART_CONTENT);
      return;
    }

    try {
      // process form
      MultiPartFormData uploadedFormItems = new MultiPartFormData(req);

      String simpleApiKey = uploadedFormItems.getSimpleFormField(SIMPLE_API_KEY_PARAM);
      if ( simpleApiKey != null ) {
    	  simpleApiKey = simpleApiKey.trim();
      	  if ( simpleApiKey.trim().length() == 0 ) {
      		  simpleApiKey = null;
      	  }
      }
      String clientId = uploadedFormItems.getSimpleFormField(CLIENT_ID_PARAM);
      if ( clientId != null ) {
    	  clientId = clientId.trim();
      	  if ( clientId.trim().length() == 0 ) {
      		clientId = null;
      	  }
      }
      String serviceAccountEmail = uploadedFormItems.getSimpleFormField(SERVICE_ACCOUNT_EMAIL_PARAM);
      if ( serviceAccountEmail != null ) {
    	  serviceAccountEmail = serviceAccountEmail.trim();
      	  if ( serviceAccountEmail.trim().length() == 0 ) {
      		serviceAccountEmail = null;
      	  }
      }
      MultiPartFormItem privateKeyFileData = uploadedFormItems
          .getFormDataByFieldName(PRIVATE_KEY_FILE_PARAM);

      byte[] p12FileContent = null;

      if (privateKeyFileData != null) {
        p12FileContent = privateKeyFileData.getStream().toByteArray();
      }

      if ( clientId == null || serviceAccountEmail == null || p12FileContent == null ||
           clientId.length() == 0 || serviceAccountEmail.length() == 0 || p12FileContent.length == 0 ) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_PARAMS);
        return;
      }

      try {
        ServerPreferencesProperties.setGoogleSimpleApiKey(cc, simpleApiKey);
        ServerPreferencesProperties.setServerPreferencesProperty(cc, ServerPreferencesProperties.GOOGLE_API_CLIENT_ID, clientId);
        ServerPreferencesProperties.setServerPreferencesProperty(cc, ServerPreferencesProperties.GOOGLE_API_SERVICE_ACCOUNT_EMAIL, serviceAccountEmail);
        ServerPreferencesProperties.setServerPreferencesProperty(cc, ServerPreferencesProperties.PRIVATE_KEY_FILE_CONTENTS, Base64.encodeBase64String(p12FileContent));
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(HtmlConsts.RESP_TYPE_HTML);
        resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
        PrintWriter out = resp.getWriter();
        out.write(HtmlConsts.HTML_OPEN);
        out.write(HtmlConsts.BODY_OPEN);
        out.write("<p>Successful private key information upload.</p>");
        out.write(HtmlConsts.BODY_CLOSE);
        out.write(HtmlConsts.HTML_CLOSE);
      } catch (ODKEntityNotFoundException e) {
        logger.warn("Set private key information error: " + e.getMessage());
          resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
              ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
      } catch (ODKOverQuotaException e) {
        logger.error("Set private key information error: " + e.getMessage());
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            ErrorConsts.QUOTA_EXCEEDED + "\n" + e.getMessage());
      }
    } catch (FileUploadException e) {
      logger.error("Set private key information error: " + e.getMessage());
      e.printStackTrace(resp.getWriter());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.UPLOAD_PROBLEM);
    }
  }

}
