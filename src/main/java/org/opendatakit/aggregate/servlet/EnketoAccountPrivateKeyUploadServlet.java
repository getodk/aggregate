/*
 * Copyright (C) 2013-2014 University of Washington
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
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.parser.MultiPartFormData;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnketoAccountPrivateKeyUploadServlet extends ServletUtilBase {

  /**
   * URI from base
   */
  public static final String ADDR = UIConsts.ENKETO_SERVICE_ACCOUNT_PRIVATE_KEY_UPLOAD_ADDR;
  private static final long serialVersionUID = -3784460108221008112L;
  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Enketo API Configuration";
  private static final String ENKETO_API_URL = "enketo_api_url";
  private static final String ENKETO_API_TOKEN = "enketo_api_token";

  private static final String UPLOAD_PAGE_BODY_START = "<form id=\"service_account_form\""
      + " accept-charset=\"UTF-8\" method=\"POST\" encoding=\"multipart/form-data\" enctype=\"multipart/form-data\""
      + " action=\"";// emit the ADDR
  private static final String UPLOAD_PAGE_BODY_MIDDLE_TO_URL = "\">"
      + "<div style=\"overflow: auto;\">"
      + "<p>See <a href=\"https://accounts.enketo.org/support/aggregate/\" target=\"_blank\">instructions</a> on how to obtain the Enketo API URL and Token</p>"
      + "<h2>Enketo API URL</h2>"
      + "<p>The URL of the Enketo service's API</p>"
      + "     <table id=\"uploadTableEnketo\">"
      + "       <tr>"
      + "           <td><input id=\"enketo_api_url\" type=\"text\" size=\"80\" name=\"enketo_api_url\" value=\"";
  private static final String UPLOAD_PAGE_BODY_MIDDLE_URL_TO_TOKEN = "\"/></td>"
      + "      </tr>\n"
      + "<tr>\n   <td colspan=\"2\"><h2>Enketo API token</h2></td></tr>"
      + "<tr><td colspan=\"2\"><p>Neccessary for authentication with the Enketo service. Obtain this form the Enketo service</p></td></tr>"
      + "      <tr>"
      + "         <td><input id=\"enketo_api_token\" type=\"text\" size=\"80\" name=\"enketo_api_token\" value=\"";
  private static final String UPLOAD_PAGE_BODY_MIDDLE_TOKEN_ONWARD = "\"/></td>"
      + "      </tr>"
      + "      <tr>\n"
      + "         <td><input type=\"submit\" name=\"button\" class=\"gwt-Button\" value=\"Save\" /></td>"
      + "         <td />" + "    </tr>" + "    </table>\n" + "   </form>" + "<br></div>\n";

  private static final Logger logger = LoggerFactory.getLogger(ServiceAccountPrivateKeyUploadServlet.class);

  /**
   * Handler for HTTP Get request to create xform upload page
   *
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *     javax.servlet.http.HttpServletResponse)
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

    String enketoApiUrl = "";
    String enketoApiToken = "";
    try {
      enketoApiUrl = ServerPreferencesProperties.getEnketoApiUrl(cc);
    } catch (ODKEntityNotFoundException e) {
      // treat this as non-existence...
      e.printStackTrace();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      quotaExceededError(resp);
      return;
    }
    try {
      enketoApiToken = ServerPreferencesProperties.getEnketoApiToken(cc);
    } catch (ODKEntityNotFoundException e) {
      // treat this as non-existence...
      e.printStackTrace();
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      quotaExceededError(resp);
      return;
    }

    // emit everything...
    // header info
    beginBasicHtmlResponse(TITLE_INFO, headerString.toString(), resp, cc);
    PrintWriter out = resp.getWriter();
    out.write(UPLOAD_PAGE_BODY_START);
    out.write(cc.getWebApplicationURL(ADDR));
    out.write(UPLOAD_PAGE_BODY_MIDDLE_TO_URL);
    if (enketoApiUrl != null) {
      out.write(enketoApiUrl);
    }
    out.write(UPLOAD_PAGE_BODY_MIDDLE_URL_TO_TOKEN);
    if (enketoApiToken != null) {
      out.write(enketoApiToken);
    }
    out.write(UPLOAD_PAGE_BODY_MIDDLE_TOKEN_ONWARD);
    finishBasicHtmlResponse(resp);
  }

  /**
   * Handler for HTTP Post request that takes an xform, parses, and saves a
   * parsed version in the datastore
   *
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *     javax.servlet.http.HttpServletResponse)
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

      String enketoApiURL = uploadedFormItems.getSimpleFormField(ENKETO_API_URL);
      if (enketoApiURL != null) {
        enketoApiURL = enketoApiURL.trim();
        if (enketoApiURL.trim().length() == 0) {
          enketoApiURL = null;
        }
      }
      String enketoApiToken = uploadedFormItems.getSimpleFormField(ENKETO_API_TOKEN);
      if (enketoApiToken != null) {
        enketoApiToken = enketoApiToken.trim();
        if (enketoApiToken.trim().length() == 0) {
          enketoApiToken = null;
        }
      }

      try {
        ServerPreferencesProperties.setEnketoApiUrl(cc, enketoApiURL);
        ServerPreferencesProperties.setEnketoApiToken(cc, enketoApiToken);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(HtmlConsts.RESP_TYPE_HTML);
        resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
        PrintWriter out = resp.getWriter();
        out.write(HtmlConsts.HTML_OPEN);
        out.write(HtmlConsts.BODY_OPEN);
        out.write("<p>Successful change of Enketo Webform Integration settings.</p>");
        out.write(HtmlConsts.BODY_CLOSE);
        out.write(HtmlConsts.HTML_CLOSE);
      } catch (ODKEntityNotFoundException e) {
        logger.warn("Enketo Webform Integration settings-change error: " + e.getMessage());
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
      } catch (ODKOverQuotaException e) {
        logger.error("Enketo Webform Integration settings-change error: " + e.getMessage());
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.QUOTA_EXCEEDED
            + "\n" + e.getMessage());
      }
    } catch (FileUploadException e) {
      logger.error("Enketo Webform Integration settings-change error: " + e.getMessage());
      e.printStackTrace(resp.getWriter());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.UPLOAD_PROBLEM);
    }
  }
}
