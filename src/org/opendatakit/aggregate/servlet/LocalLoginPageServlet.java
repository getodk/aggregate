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

/**
 * Simple servlet for the initial local login.  It is needed to get 
 * GAE to process the Spring security restrictions to the page.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class LocalLoginPageServlet extends ServletUtilBase {

	/*
	 * Standard fields 
	 */
	
	private static final long serialVersionUID = 629046684126101849L;
	
	public static final String ADDR = "local_login.html";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

	    PrintWriter out = resp.getWriter();
	    out.print(
"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">" +
"<html>" +
"<head>" +
"<meta http-equiv=\"Refresh\" content=\"1; URL=www/index.html\"/>" +
"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>" +
"<link rel=\"shortcut icon\" href=\"favicon.ico\"/>" +
"<title>Login successful! Redirecting to forms page</title>" +
"</head>" +
"<body>" +
"Please click <a href=\"www/index.html\">here</a> to enter the site." +
"</body>" +
"</html>");
	}
}
