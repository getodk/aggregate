/*
 * Copyright (C) 2011 University of Washington
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple servlet to display the openId login page. Invalidates the user's
 * session before displaying the page.
 *
 * @author user
 */
public class MultimodeLoginPageServlet extends ServletUtilBase {

  public static final String ADDR = "multimode_login.html";
  /**
   *
   */
  private static final long serialVersionUID = -1036419513113652548L;
  private static final Logger logger = LoggerFactory.getLogger(MultimodeLoginPageServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    // Check to make sure we are using the canonical server name.
    // If not, redirect to that name.  This ensures that authentication
    // cookies will have the proper realm(s) established for them.
    String newUrl = cc.getServerURL() + BasicConsts.FORWARDSLASH + ADDR;
    String query = req.getQueryString();
    if (query != null && query.length() != 0) {
      newUrl += "?" + query;
    }
    URL url = new URL(newUrl);
    if (!url.getHost().equalsIgnoreCase(req.getServerName())) {
      logger.info("Incoming servername: " + req.getServerName() + " expected: " + url.getHost() + " -- redirecting.");
      // try to get original destination URL from Spring...
      String redirectUrl = getRedirectUrl(req, ADDR);
      try {
        URI uriChangeable = new URI(redirectUrl);
        URI newUri = new URI(url.getProtocol(), null, url.getHost(), url.getPort(), uriChangeable.getPath(), uriChangeable.getQuery(), uriChangeable.getFragment());
        newUrl = newUri.toString();
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
      // go to the proper page (we'll most likely be redirected back to here for authentication)
      resp.sendRedirect(newUrl);
      return;
    }

    // OK. We are using the canonical server name.
    String redirectParamString = getRedirectUrl(req, AggregateHtmlServlet.ADDR);
    // we need to appropriately cleanse this string for the OpenID login
    // strip off the server pathname portion
    if (redirectParamString.startsWith(cc.getSecureServerURL())) {
      redirectParamString = redirectParamString.substring(cc.getSecureServerURL().length());
    } else if (redirectParamString.startsWith(cc.getServerURL())) {
      redirectParamString = redirectParamString.substring(cc.getServerURL().length());
    }
    while (redirectParamString.startsWith("/")) {
      redirectParamString = redirectParamString.substring(1);
    }

    // check for XSS attacks. The redirect string is emitted within single and double
    // quotes. It is a URL with :, /, ? and # characters. But it should not contain 
    // quotes, parentheses or semicolons.
    String cleanString = redirectParamString.replaceAll(BAD_PARAMETER_CHARACTERS, "");
    if (!cleanString.equals(redirectParamString)) {
      logger.warn("XSS cleanup -- redirectParamString has forbidden characters: " + redirectParamString);
      redirectParamString = cleanString;
    }

    logger.info("Invalidating login session " + req.getSession().getId());
    // Invalidate session.
    HttpSession s = req.getSession();
    if (s != null) {
      s.invalidate();
    }
    // Display page.
    resp.setContentType(HtmlConsts.RESP_TYPE_HTML);
    resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
    resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    resp.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
    resp.setHeader("Pragma", "no-cache");
    resp.addHeader(HtmlConsts.X_FRAME_OPTIONS, HtmlConsts.X_FRAME_SAMEORIGIN);
    PrintWriter out = resp.getWriter();
    out.print("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
        + "<html>"
        + "<head>"
        + "<meta http-equiv=\"cache-control\" content=\"no-store, no-cache, must-revalidate\"/>"
        + "<meta http-equiv=\"expires\" content=\"Mon, 26 Jul 1997 05:00:00 GMT\"/>"
        + "<meta http-equiv=\"pragma\" content=\"no-cache\"/>"
        + "<link rel=\"icon\" href=\"favicon.ico\"/>"
        + "<title>Log onto Aggregate</title>"
        + "<link type=\"text/css\" rel=\"stylesheet\" href=\"AggregateUI.css\">"
        + "<link type=\"text/css\" rel=\"stylesheet\" href=\"stylesheets/button.css\">"
        + "<link type=\"text/css\" rel=\"stylesheet\" href=\"stylesheets/table.css\">"
        + "<link type=\"text/css\" rel=\"stylesheet\" href=\"stylesheets/navigation.css\">"
        + "<script type=\"text/javascript\">"
        + "window.onbeforeunload=function() {\n"
        + "var e=document.getElementById(\"stale\");\n"
        + "e.value=\"yes\";\n"
        + "}\n"
        + "window.onload=function(){\n"
        + "var e=document.getElementById(\"stale\");\n"
        + "if(e.value==\"yes\") {window.location.reload(true);}\n"
        + "}\n"
        + "</script>"
        + "</head>"
        + "<body>"
        + "<input type=\"hidden\" id=\"stale\" value=\"no\">"
        + "<table width=\"100%\" cellspacing=\"30\"><tr>"
        + "<td align=\"LEFT\" width=\"10%\"><img src=\"odk_color.png\" id=\"odk_aggregate_logo\" /></td>"
        + "<td align=\"LEFT\" width=\"90%\"><font size=\"7\">Log onto Aggregate</font></td></tr></table>"
        + "<table cellspacing=\"20\">"
        + "<tr><td valign=\"top\">"
        + "<form action=\"local_login.html\" method=\"get\">"
        + "<script type=\"text/javascript\">"
        + "<!--\n"
        + "document.write('<input name=\"redirect\" type=\"hidden\" value=\"" + redirectParamString + "' + window.location.hash + '\"/>');"
        + "\n-->"
        + "</script>"
        + "<input class=\"gwt-Button\" type=\"submit\" value=\"Sign in with Aggregate password\"/>"
        + "</form></td>"
        + "<td valign=\"top\">Click this button to log onto Aggregate using the username "
        + "and password that have been assigned to you by the Aggregate site administrator.</td></tr>"
        + "<tr><td valign=\"top\">"
        + "<script type=\"text/javascript\">"
        + "<!--\n"
        + "document.write('<form action=\"" + redirectParamString + "' + window.location.hash + '\" method=\"get\">');"
        + "document.write('<input class=\"gwt-Button\" type=\"submit\" value=\"Anonymous Access\"/></form>');"
        + "\n-->"
        + "</script>"
        + "</td>"
        + "<td valign=\"top\">Click this button to access Aggregate without logging in.</td></tr>"
        + "</table>" + "</body>" + "</html>");
  }

}
