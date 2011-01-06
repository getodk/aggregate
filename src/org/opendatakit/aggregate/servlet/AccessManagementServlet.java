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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.common.constants.HtmlConsts;

/**
 * Top-level webpage for managing permissions and users
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class AccessManagementServlet extends ServletUtilBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4029016295391953345L;
	/**
	 * URI from base
	 */
	public static final String ADDR = "access/access-management";
	
	public static final String TITLE_INFO = "Manage Site Access";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, ADDR, req);

		beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info

		PrintWriter out = resp.getWriter();
		
		out.print("<p>By default, anyone with a gmail account can log in and " +
				"do anything with your site.  Additionally, anyone, without logging " +
				"in at all, can upload data to your site and download forms from " +
				"it.  This is consistent with Aggregate 0.9.x.</p>\n<p>Your first task " +
				"in securing the site is to restrict access to this page and the other " +
				"pages that enable you to control access to the site.  To do this,</p>" +
				"<ol>" +
				"<li>Define an 'administrators' group, granting it the ROLE_ACCESS_ADMIN permission</li>" +
				"<li>Add yourself as a registered user of the site</li>" +
				"<li>Add yourself to that 'administrators' group</li>" +
				"<li>Remove the ROLE_ACCESS_ADMIN permission from all other groups</li></ol>\n" +
				"<p>After having done this, only you can access these web pages and manage " +
				"your site's access permissions.  You should repeat steps 2 and 3 to add " +
				"at least one other person to the 'administrators' group.</p>\n" +
				"<p>To further secure your site, you need to decide which permissions (roles) you want " +
				"an anonymous user (someone who has not logged in) to have, which roles you " +
				"want users that log in with a gmail.com account to have, and which roles you " +
				"want certain specified users to have. The later is accomplished by " +
				"defining a group with the roles you want to assign, entering those specified " +
				"users into the system as registered users, and assigning them to that group. " +
				"</p><p>These are exactly the steps you did for the 'administrators' group, " +
				"above.</p>\n" +
				"Every registered user automatically belongs to the USER_IS_REGISTERED group;" +
				"if you want all registered users to have a given set of roles, then you can " +
				"assign those roles to that group.</p>");
		
		List<String[]> tableEntries = new ArrayList<String[]>();
		tableEntries.add(new String[] { "Add, remove or modify user groups",
				HtmlUtil.createHref(cc.getWebApplicationURL(GroupManagementServlet.ADDR),
						GroupManagementServlet.TITLE_INFO),
						"Permissions (roles) are granted to user groups.  Restrict " +
						"access to your data and website by granting these roles to " +
						"specific user groups. "});
		tableEntries.add(new String[] { "Add, remove or modify registered users",
				HtmlUtil.createHref(cc.getWebApplicationURL(UserManagementServlet.ADDR),
						UserManagementServlet.TITLE_INFO),
				"Permissions can be granted to anonymous users (those not logged in), " +
				"to all logged-in users (e.g., anyone with a gmail.com account), and " +
				"to groups of registered users.  Define register users to the " +
				"system though this web page."});
		tableEntries.add(new String[] { "Add or remove registered users from user groups",
				"This can be done through either of the above pages",
				"Permissions (roles) are granted to user groups.  Users possess the " +
				"roles of the groups they belong to.  Manage group memberships to " +
				"manage the access rights of the users."});
		
		out.print(HtmlConsts.TABLE_OPEN);
		
		for ( String[] sa : tableEntries ) {
			out.print(HtmlConsts.TABLE_ROW_OPEN);
			for ( String s : sa ) {
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, s));
			}
			out.print(HtmlConsts.TABLE_ROW_CLOSE);
		}

		out.print(HtmlConsts.TABLE_CLOSE);
		
		finishBasicHtmlResponse(resp);
	}
}
