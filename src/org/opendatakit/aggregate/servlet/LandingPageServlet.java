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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.common.web.CallingContext;

/**
 * Simple servlet used to capture the initial login and determine whether
 * to redirect to the actual web pages or to the access configuration 
 * servlet if site access has not yet been configured.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class LandingPageServlet extends ServletUtilBase {

	/*
	 * Standard fields 
	 */
	
	private static final long serialVersionUID = 629046684126101848L;
	
	public static final String ADDR = "www/index.html";
	
	public static final String TITLE_INFO = "ODK Aggregate";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		// determine if the system has not been configured...
		if ( cc.getUserService().isAccessManagementConfigured() ) {
			resp.sendRedirect(cc.getWebApplicationURL(FormsServlet.ADDR));
		} else {
			resp.sendRedirect(cc.getWebApplicationURL(AccessConfigurationServlet.ADDR));
		}
	}
}
