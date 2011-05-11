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
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.spring.GrantedAuthorityHierarchyTable;
import org.opendatakit.common.security.spring.GrantedAuthorityNames;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.opendatakit.common.web.CallingContext;
import org.springframework.security.core.GrantedAuthority;

/**
 * Displays the groups to which a user belongs and supports the adding
 * and removal of group memberships.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class UserModifyMembershipsServlet extends ServletUtilBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8229711530612069326L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "access/user-modify-membership";
	
	public static final String TITLE_INFO = "Change Group Memberships";
	
	public static final String USERNAME = "username";
	public static final String GROUPNAME = "groupname";
	public static final String SHOWALL = "_showall";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		// get parameter
		String username = getParameter(req, USERNAME);
		if (username == null || username.length() == 0) {
			errorMissingParam(resp);
			return;
		}

		username = SecurityUtils.normalizeUsername(username, 
				cc.getUserService().getCurrentRealm().getMailToDomain());
		
		String showallString = getParameter(req, SHOWALL);
		boolean showall = (showallString != null && showallString.compareToIgnoreCase("true") == 0);
		
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		
		TreeSet<String> groups = new TreeSet<String>();
		TreeSet<String> allGroups;
		try {
			Set<GrantedAuthority> gas = UserGrantedAuthority.getGrantedAuthorities(username, ds, user);
			for ( GrantedAuthority ga : gas ) {
				groups.add(ga.getAuthority());
			}
			if ( showall ) {
				allGroups = new TreeSet<String>();
				TreeSet<String> assignables = GrantedAuthorityHierarchyTable.getAllPermissionsAssignableGrantedAuthorities(ds, user);
				for ( String group : assignables ) {
					// users cannot be assigned to any of the predefined groups -- that just happens
					if ( GrantedAuthorityNames.isSpecialName(group) ) continue;
					allGroups.add(group);
				}
			} else {
				allGroups = groups;
			}
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		}
		
		beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info

		PrintWriter out = resp.getWriter();

		out.write("<h3>" + SecurityUtils.getEmailAddress(username) + "</h3>");
        
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(USERNAME, username);
        properties.put(SHOWALL, !showall ? "true" : "false");
    
        String link = HtmlUtil.createHrefWithProperties(req.getRequestURI(), properties,
            !showall ? "Show all assignable groups" : "Show only the groups this user is a member of");
        out.print(link);
        out.write(HtmlConsts.LINE_BREAK);

		out.write(HtmlUtil.createFormBeginTag(cc
				.getWebApplicationURL(UserModifyMembershipsServlet.ADDR), 
										null, HtmlConsts.POST));
		out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, 
					UserModifyMembershipsServlet.USERNAME, username));

		out.print(HtmlConsts.BORDERLESS_TABLE_OPEN);
		for ( String group : allGroups ) {
			out.print(HtmlConsts.TABLE_ROW_OPEN);
	        out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
	        		HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_CHECKBOX, 
	        				GROUPNAME, group, groups.contains(group)) +
	        								createGroupModifyLink(group, cc)));
			out.print(HtmlConsts.TABLE_ROW_CLOSE);
		}
		out.print(HtmlConsts.TABLE_CLOSE);
        out.write(HtmlConsts.LINE_BREAK);
		out.write(HtmlUtil.createInput("submit", null, "Update"));
		out.write("</form>");
		finishBasicHtmlResponse(resp);
	}
	
	private final String createGroupModifyLink( String group, CallingContext cc ) {
		if ( group == null ) return null;
		if ( !GrantedAuthorityNames.permissionsCanBeAssigned(group) ) return group;
		Map<String,String> properties = new HashMap<String,String>();
		properties.put(GroupModifyServlet.GROUPNAME, group);
		return HtmlUtil.createHrefWithProperties(cc.getWebApplicationURL(GroupModifyServlet.ADDR), properties, group);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		CallingContext cc = ContextFactory.getCallingContext(this, req);

		// get parameter
		String[] groupnameArray = req.getParameterValues(GROUPNAME);
		List<String> desiredGrants = new ArrayList<String>();
		if ( groupnameArray != null ) {
			desiredGrants.addAll(Arrays.asList(groupnameArray));
		}
		if (desiredGrants.isEmpty()) {
			errorMissingParam(resp);
			return;
		}
		
		String username = req.getParameter(USERNAME);
		if (username == null || username.length() == 0) {
			errorMissingParam(resp);
			return;
		}

		try {
			// get the list of desired groups
			TreeSet<String> desiredGroups = new TreeSet<String>();
			for ( String grant : desiredGrants ) {
				// don't allow an individual user to be granted a low-level role...
				if ( !GrantedAuthorityNames.permissionsCanBeAssigned(grant) ) {
					errorBadParam(resp);
					return;
				}
				desiredGroups.add(grant);
			}
			
			UserGrantedAuthority.assertUserGrantedAuthorities(username, desiredGroups, cc);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		} finally {
			cc.getUserService().reloadPermissions();
		}

		Map<String,String> properties = new HashMap<String,String>();
		properties.put(USERNAME, username);
		String link = HtmlUtil.createLinkWithProperties(
				cc.getWebApplicationURL(UserModifyMembershipsServlet.ADDR), properties);
		resp.sendRedirect(link);
	}
	

}
