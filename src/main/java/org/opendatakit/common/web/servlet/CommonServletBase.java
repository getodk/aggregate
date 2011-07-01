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

package org.opendatakit.common.web.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.constants.HtmlUtil;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.web.CallingContext;

/**
 * Base class for Servlets that contain useful utilities
 * 
 */
@SuppressWarnings("serial")
public abstract class CommonServletBase extends HttpServlet {
  public static final String INSUFFIECENT_PARAMS = "Insuffiecent Parameters Received";

  protected static final String LOGOUT = "Log off ";
  protected static final String LOG_IN = "log in";
  protected static final String PLEASE = "Please ";
  protected static final String LOGIN_REQUIRED = "Login Required";
  protected static final String HOST_HEADER = "Host";

  private final String applicationName;
  
  protected CommonServletBase(String applicationName) {
     this.applicationName = applicationName;
  }
  
  /**
   * Takes the request and displays request in plain text in the response
   * 
   * @param req The HTTP request received at the server
   * @param resp The HTTP response to be sent to client
   * @throws IOException
   */
  protected final void printRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
   * Generate HTML header string for web responses. NOTE: beginBasicHtmlResponse
   * and finishBasicHtmlResponse are a paired set of functions.
   * beginBasicHtmlResponse should be called first before adding other
   * information to the http response. When response is finished
   * finishBasicHtmlResponse should be called.
   * 
   * @param pageName name that should appear on the top of the page
   * @param resp http response to have the information appended to
   * @param req request
   * @param displayLinks display links accross the top
   * @throws IOException
   */
  protected void beginBasicHtmlResponse(String pageName, HttpServletResponse resp,
      boolean displayLinks, CallingContext cc) throws IOException {
          beginBasicHtmlResponse(pageName, BasicConsts.EMPTY_STRING, resp, displayLinks, cc );
  }

  protected PrintWriter beginBasicHtmlResponsePreamble(String headContent, HttpServletResponse resp, CallingContext cc) throws IOException {
	    resp.addHeader(HOST_HEADER, cc.getServerURL());
	    resp.setContentType(HtmlConsts.RESP_TYPE_HTML);
	    resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
	    PrintWriter out = resp.getWriter();
	    out.write(HtmlConsts.HTML_OPEN);
	    out.write("<link rel=\"shortcut icon\" href=\"" + cc.getWebApplicationURL("favicon.ico") + "\">");

	    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.HEAD, headContent + HtmlUtil.wrapWithHtmlTags(
	        HtmlConsts.TITLE, applicationName)));
	    out.write(HtmlConsts.BODY_OPEN);
	    return out;
  }
  /**
   * Generate HTML header string for web responses. NOTE: beginBasicHtmlResponse
   * and finishBasicHtmlResponse are a paired set of functions.
   * beginBasicHtmlResponse should be called first before adding other
   * information to the http response. When response is finished
   * finishBasicHtmlResponse should be called.
   * 
   * @param pageName name that should appear on the top of the page
   * @param headContent additional head content emitted before title
   * @param resp http response to have the information appended to
   * @param req request
   * @param displayLinks display links accross the top
   * @throws IOException
   */
  protected void beginBasicHtmlResponse(String pageName, String headContent, HttpServletResponse resp,
              boolean displayLinks, CallingContext cc) throws IOException {
	PrintWriter out = beginBasicHtmlResponsePreamble(headContent, resp, cc);
    out.write(HtmlConsts.PAGE_HEADING_TABLE_OPEN);
    out.write(HtmlConsts.TABLE_ROW_OPEN);
   // icon
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.HEADING_IMAGE_TABLE_DATA,
         "<img src='" + cc.getWebApplicationURL("odk_color.png") + "'/>"));
   // title
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.HEADING_RIGHT_TABLE_DATA,
         HtmlUtil.wrapWithHtmlTags(HtmlConsts.B, "<FONT COLOR=330066 size=7>" + applicationName + "</FONT>")));
   // version
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.HEADING_TABLE_DATA,
         getVersionString(cc)));
   // logout
    UserService userService = cc.getUserService();
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.HEADING_RIGHT_TABLE_DATA,
         HtmlUtil.createHref(cc.getWebApplicationURL(userService.createLogoutURL()),
                        LOGOUT + userService.getCurrentUser().getNickname())));

    out.write(HtmlConsts.TABLE_ROW_CLOSE);
    out.write(HtmlConsts.TABLE_CLOSE);

    out.write(HtmlUtil.createSelfClosingTag(HtmlConsts.HEADING_HR));
    emitPageHeader(out, displayLinks, cc);
    out.write(HtmlUtil.createSelfClosingTag(HtmlConsts.TITLE_HR));
    out.write(HtmlUtil.createBeginTag(HtmlConsts.CENTERING_DIV));
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H1, pageName));
    out.write(HtmlUtil.createEndTag(HtmlConsts.DIV));
  }
  
  /**
   * Determine the OpenRosa version number on this request.
   * @param req
   * @return null if unspecified (1.1.5 and earlier); otherwise, e.g., "1.0"
   */
  protected final Double getOpenRosaVersion(HttpServletRequest req) {
   String value = req.getHeader(ServletConsts.OPEN_ROSA_VERSION_HEADER);
   if ( value == null || value.length() == 0 ) return null;
   Double d = Double.valueOf(value);
   return d;
  }
  
  protected final void addOpenRosaHeaders(HttpServletResponse resp) {
   resp.setHeader(ServletConsts.OPEN_ROSA_VERSION_HEADER, ServletConsts.OPEN_ROSA_VERSION );
    GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    g.setTime(new Date());
    SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zz");
    formatter.setCalendar(g);
    resp.setHeader(ServletConsts.OPEN_ROSA_DATE_HEADER,  formatter.format(new Date()));
  }

  protected abstract void emitPageHeader(PrintWriter out,  boolean displayLinks, CallingContext cc);
  
  protected abstract String getVersionString(CallingContext cc);
  /**
   * Generate HTML footer string for web responses
   * 
   * @param resp http response to have the information appended to
   * @throws IOException
   */
  protected final void finishBasicHtmlResponse(HttpServletResponse resp) throws IOException {
    resp.getWriter().write(HtmlConsts.BODY_CLOSE + HtmlConsts.HTML_CLOSE);
  }

  /**
   * Generate error response for missing parameters in request
   * 
   * @param resp The HTTP response to be sent to client
   * @throws IOException caused by problems writing error information to
   *         response
   */
  protected final void sendErrorNotEnoughParams(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, INSUFFIECENT_PARAMS);
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
  protected final String getParameter(HttpServletRequest req, String parameterName)
      throws UnsupportedEncodingException {
    String encodedParamter = req.getParameter(parameterName);
    String parameter = null;

    if (encodedParamter != null) {
      parameter = URLDecoder.decode(encodedParamter, HtmlConsts.UTF8_ENCODE);
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


  protected final String encodeParameter(String parameter) throws UnsupportedEncodingException {
    return URLEncoder.encode(parameter, HtmlConsts.UTF8_ENCODE);
  }
}
