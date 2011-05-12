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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Stupid class to wrap the Aggregate.html page that GWT uses for 
 * all its UI presentation.  Needed so that access to the page can 
 * be managed by Spring Security.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class AggregateHtmlServlet extends ServletUtilBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5811797423869654357L;

	public static final String ADDR = "Aggregate.html";
	
	public static final String PAGE_CONTENTS = 
"<!doctype html>" +
"<!-- The DOCTYPE declaration above will set the    -->" +
"<!-- browser's rendering engine into               -->" +
"<!-- \"Standards Mode\". Replacing this declaration  -->" +
"<!-- with a \"Quirks Mode\" doctype may lead to some -->" +
"<!-- differences in layout.                        -->" +
"" +
"<html>" +
"  <head>" +
"	<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
"	<title>ODK Aggregate</title>" +
"	<script type=\"text/javascript\" src=\"javascript/jquery-1.5.1.min.js\"></script>" +
"	<script type=\"text/javascript\" src=\"javascript/resize.js\"></script>" +
"	<script type=\"text/javascript\" src=\"javascript/main.js\"></script>" +
"    <script type=\"text/javascript\" language=\"javascript\" src=\"aggregateui/aggregateui.nocache.js\"></script>" +
"    <link type=\"text/css\" rel=\"stylesheet\" href=\"AggregateUI.css\">" +
"    <link type=\"text/css\" rel=\"stylesheet\" href=\"stylesheets/button.css\">" +
"    <link type=\"text/css\" rel=\"stylesheet\" href=\"stylesheets/table.css\">" +
"    <link type=\"text/css\" rel=\"stylesheet\" href=\"stylesheets/navigation.css\">" +
"  </head>" +
"  <body>" +
"    <iframe src=\"javascript:''\" id=\"__gwt_historyFrame\" tabIndex='-1' style=\"position:absolute;width:0;height:0;border:0\"></iframe>" +
"    <noscript>" +
"      <div style=\"width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif\">" +
"        Your web browser must have JavaScript enabled" +
"        in order for this application to display correctly." +
"      </div>" +
"    </noscript>" +
"	<div id=\"dynamic_content\"></div>" +
"  </body>" +
"</html>";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {


	    PrintWriter out = resp.getWriter();
	    out.print(PAGE_CONTENTS);
	}

}
