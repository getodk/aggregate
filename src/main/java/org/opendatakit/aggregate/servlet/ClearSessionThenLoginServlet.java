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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.HttpUtils;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.web.CallingContext;

/**
 * Simple servlet used to clear the session cookie of a client and present the
 * multimode_login.html page to them. This allows for an anonymous user to choose
 * to provide credentials.
 *
 * @author mitchellsundt@gmail.com
 */
public class ClearSessionThenLoginServlet extends ServletUtilBase {

  /*
   * Standard fields
   */

  public static final String ADDR = "relogin.html";
  public static final String TITLE_INFO = "ODK Aggregate";
  private static final long serialVersionUID = 629046684126101848L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
      IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    UserService userService = cc.getUserService();
    boolean isAnon = !userService.isUserLoggedIn();

    HttpSession s = req.getSession();
    if (s != null) {
      s.invalidate();
      // insert delay to let this propagate out?
      // attempt to fix non-responsiveness of the Logger In
      // button upon initial page load...
      try {
        Thread.sleep(1000L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    String newUrl;
    if (isAnon) {
      // anonymous user -- go to the login page...
      newUrl = cc.getWebApplicationURL("multimode_login.html");
    } else {
      // we are logged in via token-based or basic or digest auth.
      // redirect to Spring's logout url...
      newUrl = cc.getWebApplicationURL("/" + cc.getUserService().createLogoutURL());
    }
    // preserve the query string (helps with GWT debugging)
    String query = req.getQueryString();
    if (query != null && query.length() != 0) {
      newUrl += "?" + query;
    }
    HttpUtils.redirect(resp, newUrl);
  }
}
