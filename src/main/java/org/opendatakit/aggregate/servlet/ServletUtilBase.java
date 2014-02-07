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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.opendatakit.common.web.servlet.CommonServletBase;

/**
 * Base class for Servlets that contain useful utilities
 *
 */
@SuppressWarnings("serial")
public class ServletUtilBase extends CommonServletBase {

  protected ServletUtilBase() {
    super(ServletConsts.APPLICATION_NAME);
  }

  /**
   * Generate error response for ODK ID not found
   *
   * @param resp
   *          The HTTP response to be sent to client
   * @throws IOException
   *           caused by problems writing error information to response
   */
  protected void odkIdNotFoundError(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_NOT_FOUND, ErrorConsts.ODKID_NOT_FOUND);
  }

  /**
   * Generate error response for quota exceeded.
   *
   * @param resp
   * @throws IOException
   *           caused by problems writing error information to response
   */
  protected void quotaExceededError(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, ErrorConsts.QUOTA_EXCEEDED);
  }

  /**
   * Generate error response for datastore access issues.
   *
   * @param resp
   * @throws IOException
   *           caused by problems writing error information to response
   */
  protected void datastoreError(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
  }

  /**
   * Generate error response for missing the Key parameter
   *
   * @param resp
   *          The HTTP response to be sent to client
   * @throws IOException
   *           caused by problems writing error information to response
   */
  protected void errorMissingKeyParam(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.ODK_KEY_PROBLEM);
  }

  protected void errorMissingParam(HttpServletResponse resp) throws IOException {
	    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_PARAMS);
  }
  /**
   * Generate error response for invalid parameters
   *
   * @param resp
   *          The HTTP response to be sent to client
   * @throws IOException
   *           caused by problems writing error information to response
   */
  protected void errorBadParam(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.INVALID_PARAMS);
  }

  /**
   * Generate error response for missing the Key parameter
   *
   * @param resp
   *          The HTTP response to be sent to client
   * @throws IOException
   *           caused by problems writing error information to response
   */
  protected void errorRetreivingData(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.INCOMPLETE_DATA);
  }

  // GWT required fields...
  private static final String AGGREGATEUI_STYLE_RESOURCE = "AggregateUI.css";
  private static final String BUTTON_STYLE_RESOURCE = "stylesheets/button.css";
  private static final String TABLE_STYLE_RESOURCE = "stylesheets/table.css";
  private static final String UPLOAD_STYLE_RESOURCE = "stylesheets/navigation.css";


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
    resp.setHeader(ApiConstants.DATE_HEADER,  formatter.format(new Date()));
    resp.setHeader(ServletConsts.OPEN_ROSA_ACCEPT_CONTENT_LENGTH_HEADER, "10485760"); // 10MB
  }

  protected final void addOpenDataKitHeaders(HttpServletResponse resp) {
    resp.setHeader(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION);
    GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    g.setTime(new Date());
    SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zz");
    formatter.setCalendar(g);
    resp.setHeader(ApiConstants.DATE_HEADER,  formatter.format(new Date()));
  }

  @Override
  protected void beginBasicHtmlResponse(String pageName, HttpServletResponse resp,
	      CallingContext cc) throws IOException {

	StringBuilder headerString = new StringBuilder();
	headerString.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"");
	headerString.append(cc.getWebApplicationURL(AGGREGATEUI_STYLE_RESOURCE));
	headerString.append("\" />");
	headerString.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"");
	headerString.append(cc.getWebApplicationURL(BUTTON_STYLE_RESOURCE));
	headerString.append("\" />");
	headerString.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"");
	headerString.append(cc.getWebApplicationURL(TABLE_STYLE_RESOURCE));
	headerString.append("\" />");
	headerString.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"");
	headerString.append(cc.getWebApplicationURL(UPLOAD_STYLE_RESOURCE));
	headerString.append("\" />");

	PrintWriter out = beginBasicHtmlResponsePreamble( headerString.toString(), resp, cc );
    out.write(HtmlUtil.createBeginTag(HtmlConsts.CENTERING_DIV));
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H1, pageName));
    out.write(HtmlUtil.createEndTag(HtmlConsts.DIV));
  }
}
