/*
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple servlet for the initial local login. It is needed to get GAE to
 * process the Spring security restrictions to the page.
 *
 * @author mitchellsundt@gmail.com
 */
public class LocalLoginPageServlet extends ServletUtilBase {

  public static final String ADDR = "local_login.html";

  /*
   * Standard fields
   */
  private static final Logger logger = LoggerFactory.getLogger(LocalLoginPageServlet.class);
  private static final long serialVersionUID = 629046684126101849L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
      IOException {

    String redirectParamString = getRedirectUrl(req, AggregateHtmlServlet.ADDR);

    // check for XSS attacks. The redirect string is emitted within single and double
    // quotes. It is a URL with :, /, ? and # characters. But it should not contain 
    // quotes, parentheses or semicolons.
    String cleanString = redirectParamString.replaceAll(BAD_PARAMETER_CHARACTERS, "");
    if (!cleanString.equals(redirectParamString)) {
      logger.warn("XSS cleanup -- redirectParamString has forbidden characters: " + redirectParamString);
      redirectParamString = cleanString;
    }

    resp.setContentType(HtmlConsts.RESP_TYPE_HTML);
    resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
    resp.addHeader(HtmlConsts.X_FRAME_OPTIONS, HtmlConsts.X_FRAME_SAMEORIGIN);
    PrintWriter out = resp.getWriter();
    out.print("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
        + "<html>"
        + "<head>"
        + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"
        + "<link rel=\"icon\" href=\"favicon.ico\"/>"
        + "<title>Login successful! Redirecting...</title>"
        + "<link type=\"text/css\" rel=\"stylesheet\" href=\"AggregateUI.css\">"
        + "<link type=\"text/css\" rel=\"stylesheet\" href=\"stylesheets/button.css\">"
        + "<link type=\"text/css\" rel=\"stylesheet\" href=\"stylesheets/table.css\">"
        + "<link type=\"text/css\" rel=\"stylesheet\" href=\"stylesheets/navigation.css\">"
        + "<script type=\"text/javascript\">"
        + "<!--\n"
        + "function redirector() {"
        + "   window.location = '" + redirectParamString + "' + window.location.hash;"
        + "}"
        + "\n-->"
        + "</script>"
        + "</head>"
        + "<body onLoad=\"setTimeout('redirector()', 1000)\">"
        + "<table width=\"100%\" cellspacing=\"30\"><tr>"
        + "<td align=\"LEFT\" width=\"10%\"><img src=\"odk_color.png\" id=\"odk_aggregate_logo\" /></td>"
        + "<td align=\"LEFT\" width=\"90%\"><font size=\"7\">Successful Login</font></td></tr></table>"
        + "Please click "
        + "<script type=\"text/javascript\">"
        + "<!--\n"
        + "document.write('<a href=\"" + redirectParamString + "' + window.location.hash + '\">here</a>');"
        + "\n-->"
        + "</script>"
        + " to enter the site."
        + "</body>"
        + "</html>");
  }
}
