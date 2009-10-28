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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKGDataAuthenticationError;
import org.odk.aggregate.exception.ODKGDataServiceNotAuthenticated;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.AuthenticationException;

/**
 * Base class for Servlets that contain useful utilities
 * 
 */
@SuppressWarnings("serial")
public class ServletUtilBase extends HttpServlet {

  private static final String HOST_HEADER = "Host";

  /**
   * Takes the request and displays request in plain text in the response
   * 
   * @param req The HTTP request received at the server
   * @param resp The HTTP response to be sent to client
   * @throws IOException
   */
  protected void printRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      BufferedReader received = req.getReader();

      String line = received.readLine();
      while (line != null) {
        resp.getWriter().println(line);
        line = received.readLine();
      }
    } catch (Exception e) {
      e.printStackTrace(resp.getWriter());
    }
  }

  /** 
   * Removes HTML sensitive characters and replaces them with the 
   * HTML versions that can be properly displayed.
   * For example: '<' becomes '&lt;'
   * 
   * @param htmlString
   *    string of HTML that will have its HTML sensitive characters removed
   * @return
   *    string of HTML that has been replace with proper HTML display characters
   */
  protected String formatHtmlString(String htmlString) {
    String formatted = htmlString;
    formatted = formatted.replace(BasicConsts.LESS_THAN, HtmlConsts.LESS_THAN);
    formatted = formatted.replace(BasicConsts.GREATER_THAN, HtmlConsts.GREATER_THAN);
    formatted = formatted.replace(BasicConsts.NEW_LINE, HtmlConsts.LINE_BREAK);
    formatted = formatted.replace(BasicConsts.TAB, HtmlConsts.TAB);
    formatted = formatted.replace(BasicConsts.SPACE, HtmlConsts.SPACE);
    return formatted;
  }

  /**
   * Takes request and verifies the user has logged in. If the user has not
   * logged in generates the appropriate text for response to user
   * 
   * @param req The HTTP request received at the server
   * @param resp The HTTP response to be sent to client
   * @return boolean value of whether the user is logged in
   * @throws IOException Throws IO Exception if problem occurs creating the
   *         login link in response
   */
  protected boolean verifyCredentials(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      beginBasicHtmlResponse("Login Required", resp, req, false);
      String returnUrl = req.getRequestURI() + ServletConsts.BEGIN_PARAM + req.getQueryString();
      String loginHtml =
          HtmlUtil.wrapWithHtmlTags(HtmlConsts.P, "Please "
              + HtmlUtil.createHref(userService.createLoginURL(returnUrl), "log in"));
      resp.getWriter().print(loginHtml);
      finishBasicHtmlResponse(resp);
      return false;
    }
    return true;
  }

  /**
   * Generate HTML header string for web responses. NOTE: beginBasicHtmlResponse
   * and finishBasicHtmlResponse are a paired set of functions.
   * beginBasicHtmlResponse should be called first before adding other
   * information to the http response. When response is finished
   * finishBasicHtmlResponse should be called.
   * 
   * @param pageName name that should appear on the top of the page
   * @param resp http response to have the information appended to
   * @param req TODO
   * @param displayLinks display links accross the top
   * @throws IOException
   */
  protected void beginBasicHtmlResponse(String pageName, HttpServletResponse resp,
      HttpServletRequest req, boolean displayLinks) throws IOException {
    resp.addHeader(HOST_HEADER, getServerURL(req));
    resp.setContentType(ServletConsts.RESP_TYPE_HTML);
    resp.setCharacterEncoding(ServletConsts.ENCODE_SCHEME);
    PrintWriter out = resp.getWriter();
    out.write(HtmlConsts.HTML_OPEN);
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.HEAD, HtmlUtil.wrapWithHtmlTags(
        HtmlConsts.TITLE, BasicConsts.APPLICATION_NAME)));
    out.write(HtmlConsts.BODY_OPEN);
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H2, "<FONT COLOR=330066>" + BasicConsts.APPLICATION_NAME) + "</FONT>");
    if (displayLinks) {
      UserService userService = UserServiceFactory.getUserService();
      out.write(generateNavigationInfo());
      out.write(HtmlConsts.TAB + HtmlConsts.TAB);
      out.write(HtmlUtil.createHref(userService.createLogoutURL("/"), "Log Out from "
          + userService.getCurrentUser().getNickname()));
    }
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H1, pageName));
  }

  /**
   * Generate HTML footer string for web responses
   * 
   * @param resp http response to have the information appended to
   * @throws IOException
   */
  protected void finishBasicHtmlResponse(HttpServletResponse resp) throws IOException {
    resp.getWriter().write(HtmlConsts.BODY_CLOSE + HtmlConsts.HTML_CLOSE);
  }

  /**
   * Generate error response for ODK ID not found
   * 
   * @param resp The HTTP response to be sent to client
   * @throws IOException caused by problems writing error information to
   *         response
   */
  protected void odkIdNotFoundError(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_NOT_FOUND, ErrorConsts.ODKID_NOT_FOUND);
  }

  /**
   * Generate error response for missing the Key parameter
   * 
   * @param resp The HTTP response to be sent to client
   * @throws IOException caused by problems writing error information to
   *         response
   */
  protected void errorMissingKeyParam(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.ODK_KEY_PROBLEM);
  }

  /**
   * Generate error response for missing the Key parameter
   * 
   * @param resp The HTTP response to be sent to client
   * @throws IOException caused by problems writing error information to
   *         response
   */
  protected void errorRetreivingData(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.INCOMPLETE_DATA);
  }

  /**
   * Generate error response for missing parameters in request
   * 
   * @param resp The HTTP response to be sent to client
   * @throws IOException caused by problems writing error information to
   *         response
   */
  protected void sendErrorNotEnoughParams(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.INSUFFIECENT_PARAMS);
  }

  /**
   * Extract the parameter from HTTP request and return the decoded value.
   * Returns null if parameter not present
   * 
   * @param req HTTP request that contains the parameter
   * @param parameterName the name of the parameter to be retrieved
   * @return Parameter's decoded value or null if not found
   * 
   * @throws UnsupportedEncodingException
   */
  protected String getParameter(HttpServletRequest req, String parameterName)
      throws UnsupportedEncodingException {
    String encodedParamter = req.getParameter(parameterName);
    String parameter = null;

    if (encodedParamter != null) {
      parameter = URLDecoder.decode(encodedParamter, ServletConsts.ENCODE_SCHEME);
    }
    
    // TODO: consider if aggregate should really be passing nulls in parameters
    // TODO: FIX!!! as null happens when parameter not present, but what about passing nulls?
    if(parameter != null) {
      if(parameter.equals(BasicConsts.NULL)) {
        return null;
      }
    } 
    return parameter;
  }


  protected String encodeParameter(String parameter) throws UnsupportedEncodingException {
    return URLEncoder.encode(parameter, ServletConsts.ENCODE_SCHEME);
  }


  /**
   * Generate common navigation links
   * 
   * @return a string with href links
   */
  public String generateNavigationInfo() {
    String html = HtmlUtil.createHref(FormsServlet.ADDR, ServletConsts.FORMS_LINK_TEXT);
    html += HtmlConsts.TAB + HtmlConsts.TAB;
    html += HtmlUtil.createHref(FormUploadServlet.ADDR, ServletConsts.UPLOAD_FORM_LINK_TEXT);
    html += HtmlConsts.TAB + HtmlConsts.TAB;
    html +=
        HtmlUtil.createHref(ServletConsts.UPLOAD_SUBMISSION_ADDR,
            ServletConsts.UPLOAD_SUB_LINK_TEXT);
    return html + HtmlConsts.TAB;
  }

  protected void setDownloadFileName(HttpServletResponse resp, String filename) {
    resp.setHeader(ServletConsts.CONTENT_DISPOSITION, ServletConsts.ATTACHMENT_FILENAME_TXT
        + filename + BasicConsts.QUOTE + BasicConsts.SEMI_COLON);
  }

  protected String getServerURL(HttpServletRequest req) {
    String serverName = req.getServerName();
    int port = req.getServerPort();
    if (port != HtmlConsts.WEB_PORT) {
      serverName += BasicConsts.COLON + Integer.toString(port);
    }
    return serverName;
  }

  protected String verifyGDataAuthorization(HttpServletRequest req, HttpServletResponse resp,
      String scope) throws IOException, ODKGDataAuthenticationError,
      ODKGDataServiceNotAuthenticated {
    String onetimeUseToken = AuthSubUtil.getTokenFromReply(req.getQueryString());

    String sessionToken = null;

    if (onetimeUseToken == null) {
      throw new ODKGDataServiceNotAuthenticated();
    } else {

      try {
        sessionToken = AuthSubUtil.exchangeForSessionToken(onetimeUseToken, null);
      } catch (AuthenticationException e) {
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Server rejected one time use token.");
        throw new ODKGDataAuthenticationError();

      } catch (GeneralSecurityException e) {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Security error while retrieving session token.");
        throw new ODKGDataAuthenticationError();
      }

      try {
        Map<String, String> tokenInfo = AuthSubUtil.getTokenInfo(sessionToken, null);

        String tokenScope = tokenInfo.get("Scope");

        if (!tokenScope.equals(scope)) {
          resp.sendRedirect(generateAuthorizationURL(req, scope));
          throw new ODKGDataServiceNotAuthenticated();
        }
      } catch (AuthenticationException e) {
        resp.sendRedirect(generateAuthorizationURL(req, scope));
        throw new ODKGDataAuthenticationError();
      } catch (GeneralSecurityException e) {
        resp.sendRedirect(generateAuthorizationURL(req, scope));
        throw new ODKGDataAuthenticationError();
      }
    }

    return sessionToken;
  }

  // TODO: see if can integrate better with helper functions in SpreadsheetServlet
  protected String generateAuthorizationURL(HttpServletRequest req, String scope) {
    String returnUrl =
        "http://" + getServerURL(req) + req.getRequestURI() + ServletConsts.BEGIN_PARAM
            + req.getQueryString();
    String requestUrl = AuthSubUtil.getRequestUrl(returnUrl, scope, false, true);
    return requestUrl;
  }

protected String generateAuthButton(String scope, String buttonText, Map<String, String> params,
		HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		    String returnUrl =
		        "http://"
		            + HtmlUtil.createLinkWithProperties(getServerURL(req) + req.getRequestURI(), params);
		
		    String requestUrl = AuthSubUtil.getRequestUrl(returnUrl, scope, false, true);
		
		    StringBuilder form = new StringBuilder();
		    form.append(HtmlConsts.LINE_BREAK);
		    form.append(HtmlUtil.createFormBeginTag(requestUrl, null, ServletConsts.POST));
		    form.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, buttonText));
		    form.append(HtmlConsts.FORM_CLOSE);
		
		    return form.toString();
		  }
  
}
