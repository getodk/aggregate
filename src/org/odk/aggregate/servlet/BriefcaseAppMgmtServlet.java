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

package org.odk.aggregate.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.BriefcaseAuth;

/**
 * Alters the status and resets the Briefcase application token needed by the
 * Briefcase application to access the data within Aggregate 0.9.x.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class BriefcaseAppMgmtServlet extends ServletUtilBase {

  public static final String PV_DISABLE = "disable";

  public static final String PV_ENABLE = "enable";

  public static final String PV_RESET = "reset";

  public static final String OPTION_PARAMETER = "option";

  /**
	 * 
	 */
  private static final long serialVersionUID = -5121979982542017091L;

  /**
   * URI from base
   */
  public static final String ADDR = "BriefcaseAppMgmt";

  /**
   * Copy of the body of Briefcase.html (see Briefcase project)
   */

  /**
   * Handler for HTTP Get request to create blank page that is navigable
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

    String option = req.getParameter(OPTION_PARAMETER);
    if (option.equals(PV_RESET)) {
      BriefcaseAuth.regenerateBriefcaseAuthToken();
    } else if (option.equals(PV_ENABLE)) {
      BriefcaseAuth.enableBriefcaseAuthToken(true);
    } else if (option.equals(PV_DISABLE)) {
      BriefcaseAuth.enableBriefcaseAuthToken(false);
    }
    resp.sendRedirect(BriefcaseServlet.ADDR);
  }
}
