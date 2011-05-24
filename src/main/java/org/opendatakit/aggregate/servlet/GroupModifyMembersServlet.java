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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.spring.GrantedAuthorityNames;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.opendatakit.common.web.CallingContext;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

/**
 * Displays the users assigned to the group and supports adding and 
 * removing users from the group.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class GroupModifyMembersServlet extends ServletUtilBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5472568225259472060L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "access/group-modify-members";
	
	public static final String TITLE_INFO = "Modify User Membership for: ";
	
	/**
	 * Parameter names...
	 */
	public static final String GROUPNAME = "groupname";
	
	public static final String USERNAME = "username";
	
	public static final String SHOWALL = "_showall";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		// get parameter
		String groupname = getParameter(req, GROUPNAME);
		if (groupname == null || groupname.length() == 0) {
			errorMissingParam(resp);
			return;
		}
		for ( GrantedAuthorityNames n : GrantedAuthorityNames.values() ) {
			if ( groupname.equals(n.name()) ) {
				errorBadParam(resp);
				return;
			}
		}
		
		String showallString = getParameter(req, SHOWALL);
		boolean showall = (showallString != null && showallString.compareToIgnoreCase("true") == 0);
		
		// ROLE_ values are reserved, as are RUN_AS_ values.
		if ( !GrantedAuthorityNames.permissionsCanBeAssigned(groupname) ) {
			errorBadParam(resp);
			return;
		}
		
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();

		Set<String> uriUsers;
		List<RegisteredUsersTable> allUsers = new ArrayList<RegisteredUsersTable>();
		try {
			uriUsers = UserGrantedAuthority.getUriUsers(new GrantedAuthorityImpl(groupname), ds, user);
			
			Query query = RegisteredUsersTable.createQuery(ds, user);
			RegisteredUsersTable.applyNaturalOrdering(query);
			List<? extends CommonFieldsBase> registeredUsers = query.executeQuery(0);
			for ( CommonFieldsBase c : registeredUsers ) {
				RegisteredUsersTable t = (RegisteredUsersTable) c;
				if ( showall || uriUsers.contains(t.getUri()) ) {
					allUsers.add(t);
				}
			}
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		}
		
		beginBasicHtmlResponse(TITLE_INFO + groupname, resp, true, cc); // header info

		PrintWriter out = resp.getWriter();

		out.write("<h3>Add or remove users from this group</h3>");
        
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(GROUPNAME, groupname);
        properties.put(SHOWALL, !showall ? "true" : "false");
    
        String link = HtmlUtil.createHrefWithProperties(req.getRequestURI(), properties,
            !showall ? "Show all users" : "Show only users in this group");
        out.print(link);
        out.write(HtmlConsts.LINE_BREAK);

		out.write(HtmlUtil.createFormBeginTag(cc
				.getWebApplicationURL(GroupModifyMembersServlet.ADDR), 
										null,
										HtmlConsts.POST));
		out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, 
										GROUPNAME, groupname));

		out.print(HtmlConsts.BORDERLESS_TABLE_OPEN);
		for ( RegisteredUsersTable u : allUsers ) {
			out.print(HtmlConsts.TABLE_ROW_OPEN);
	        out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
	        		HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_CHECKBOX, 
	        							USERNAME, u.getUri(),
	        								uriUsers.contains(u.getUri())) +
	        								createUserModifyLink(u, cc)));
			out.print(HtmlConsts.TABLE_ROW_CLOSE);
		}
		out.print(HtmlConsts.TABLE_CLOSE);
        out.write(HtmlConsts.LINE_BREAK);
		out.write(HtmlUtil.createInput("submit", null, "Update"));
		out.write("</form>");
		finishBasicHtmlResponse(resp);
	}
	
	private final String createUserModifyLink( RegisteredUsersTable u, CallingContext cc ) {
		String display = u.getDisplayName();
		Map<String,String> properties = new HashMap<String,String>();
		properties.put(UserModifyServlet.USERNAME, u.getUsername());
		return HtmlUtil.createHrefWithProperties(cc.getWebApplicationURL(UserModifyServlet.ADDR), 
						properties, display);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		CallingContext cc = ContextFactory.getCallingContext(this, req);

		// get parameter
		String[] usernameArray = req.getParameterValues(USERNAME);
		TreeSet<String> allDesiredMembers = new TreeSet<String>();
		if ( usernameArray != null ) {
			allDesiredMembers.addAll(Arrays.asList(usernameArray));
		}
		if (allDesiredMembers.isEmpty()) {
			errorMissingParam(resp);
			return;
		}
		
		String groupname = req.getParameter(GROUPNAME);
		if (groupname == null || groupname.length() == 0) {
			errorMissingParam(resp);
			return;
		}

		try {
			UserGrantedAuthority.assertGrantedAuthoryMembers(new GrantedAuthorityImpl(groupname),
															allDesiredMembers, cc);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		}

		Map<String,String> properties = new HashMap<String,String>();
		properties.put(GROUPNAME, groupname);
		String link = HtmlUtil.createLinkWithProperties(
				cc.getWebApplicationURL(GroupModifyMembersServlet.ADDR), properties);
		resp.sendRedirect(link);
	}

}
