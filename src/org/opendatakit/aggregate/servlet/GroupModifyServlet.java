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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.spring.GrantedAuthorityHierarchyTable;
import org.opendatakit.common.security.spring.GrantedAuthorityNames;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

/**
 * Displays the inheritance of a group and the roles directly granted
 * to a group.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class GroupModifyServlet extends ServletUtilBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -613768029221696145L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "access/group-modify";
	
	public static final String TITLE_INFO = "Modify Permissions for: ";
	
	public static final String GROUPNAME = "groupname";
	public static final String ROLENAME = "rolename";
	
	/**
	 * Handler for HTTP Get request that adds a new groupname to the user groups.
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		// get parameter
		String groupname = getParameter(req, GROUPNAME);
		if (groupname == null || groupname.length() == 0) {
			errorMissingParam(resp);
			return;
		}
		
		// ROLE_ values are reserved, as are RUN_AS_ values.
		if ( !GrantedAuthorityNames.permissionsCanBeAssigned(groupname) ) {
			errorBadParam(resp);
			return;
		}
		
		TreeSet<String> groups = new TreeSet<String>();
		TreeSet<String> permissionsAssignableGroups = new TreeSet<String>();

		try {
			groups = GrantedAuthorityHierarchyTable.getSubordinateGrantedAuthorities(new GrantedAuthorityImpl(groupname), cc);
			permissionsAssignableGroups = GrantedAuthorityHierarchyTable.getAllPermissionsAssignableGrantedAuthorities(
					cc.getDatastore(), cc.getCurrentUser());
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		}

		// construct the sets of groups and roles that can be selected
		TreeSet<String> roles = new TreeSet<String>();
		
		// add the logged-in user's mailto domain in case it is different...
		String mailtoAuthority = GrantedAuthorityNames.getMailtoGrantedAuthorityName(cc.getCurrentUser().getUriUser());
		if ( mailtoAuthority != null ) {
			permissionsAssignableGroups.add(mailtoAuthority);
		}
		
		// add the well-known granted authority names.
		for ( GrantedAuthorityNames name : GrantedAuthorityNames.values()) {
			if ( !GrantedAuthorityNames.permissionsCanBeAssigned(name.toString()) ) {
				roles.add(name.toString());
			} else {
				permissionsAssignableGroups.add(name.toString());
			}
		}
		
		// make sure the group itself is not in the list of selectable groups...
		permissionsAssignableGroups.remove(groupname);
		
		beginBasicHtmlResponse(TITLE_INFO + groupname, resp, true, cc); // header info

		PrintWriter out = resp.getWriter();
		
		out.write("<h2>Current Privileges</h2>");
		
		out.print(HtmlConsts.TABLE_OPEN);

		String[] headers = new String[] { 
							"Inherits some Access Rights<br/>from these User Groups", 
							"Directly Granted Access Rights", 
							"Full Set of Granted Access Rights" };

		for (String header : headers) {
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_HEADER, header));
	    }
		
		out.print(HtmlConsts.TABLE_ROW_OPEN);
		out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
				organizeGroups(groups, false, cc)));
		out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
				organizeGroups(groups, true, cc)));
		out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
				fetchFullSet(groupname, cc)));

		out.print(HtmlConsts.TABLE_ROW_CLOSE);

		out.print(HtmlConsts.TABLE_CLOSE);

		out.write("<h2>Update Privileges</h2>");

		out.write(HtmlUtil.createFormBeginTag(cc
				.getWebApplicationURL(GroupModifyServlet.ADDR), null,
				HtmlConsts.POST));
		out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, GroupModifyServlet.GROUPNAME, groupname));

		out.write("<h3>Select the user groups from which to inherit access rights</h3>");
		
		String[] ugHeaders = new String[] { 
				"User Group", 
				"Full Set of Granted Access Rights" };

		out.print(HtmlConsts.TABLE_OPEN);
		for (String header : ugHeaders) {
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_HEADER, header));
		}

		for ( String inheritablegroup : permissionsAssignableGroups ) {
			out.print(HtmlConsts.TABLE_ROW_OPEN);
	        out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
	        		HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_CHECKBOX, 
	        								ROLENAME, inheritablegroup,
	        								groups.contains(inheritablegroup)) +
	        								groupNameLink(inheritablegroup,cc)));
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
					fetchFullSet(inheritablegroup, cc)));
			out.print(HtmlConsts.TABLE_ROW_CLOSE);
		}
		out.print(HtmlConsts.TABLE_CLOSE);

		out.write("<h3>Select the access rights directly given to this group</h3>");
		
		String[] rHeaders = new String[] { 
				"Access Right", 
				"Description" };

		out.print(HtmlConsts.TABLE_OPEN);
		for (String header : rHeaders) {
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_HEADER, header));
		}

		for ( String rolename : roles ) {
			GrantedAuthorityNames n = GrantedAuthorityNames.valueOf(rolename);
			out.print(HtmlConsts.TABLE_ROW_OPEN);
	        out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
	        		HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_CHECKBOX, 
	        				ROLENAME, rolename, groups.contains(rolename)) + rolename));
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
					n.getDescription()));
			out.print(HtmlConsts.TABLE_ROW_CLOSE);
		}
		out.print(HtmlConsts.TABLE_CLOSE);
		
		out.write(HtmlConsts.LINE_BREAK);
		out.write(HtmlUtil.createInput("submit", null, "Update"));
		out.write("</form>");
		finishBasicHtmlResponse(resp);
	}
	
	private String groupNameLink( String groupname, CallingContext cc ) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(GroupModifyServlet.GROUPNAME, groupname);
    
        return HtmlUtil.createHrefWithProperties(
        		cc.getWebApplicationURL(GroupModifyServlet.ADDR), properties,
        		groupname );
	}

	private String fetchFullSet(String key, CallingContext cc) {
		TreeSet<String> orderedSet = fetchGrantedAuthoritySet(key, cc);

		return buildList(orderedSet, cc);
	}

	private String organizeGroups(Collection<String> directGrants, boolean rolesOnly, CallingContext cc) {
		TreeSet<String> orderedSet = new TreeSet<String>();
		for ( String g : directGrants ) {
			boolean role = !GrantedAuthorityNames.permissionsCanBeAssigned(g);
			if ( rolesOnly ? role : !role ) {
				orderedSet.add(g);
			}
		}
		return buildList(orderedSet, cc);
	}

	private String buildList(TreeSet<String> orderedSet, CallingContext cc) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for ( String g : orderedSet ) {
			if ( !first ) {
				b.append("<br/>");
			}
			first = false;
			if ( !GrantedAuthorityNames.permissionsCanBeAssigned(g) ) {
				b.append(g);
			} else {
				b.append(groupNameLink(g,cc));
			}
		}
		return b.toString();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		// get parameter
		String[] usernameArray = req.getParameterValues(ROLENAME);
		List<String> desiredGrants = new ArrayList<String>();
		if ( usernameArray != null ) {
			desiredGrants.addAll(Arrays.asList(usernameArray));
		}
		
		// NOTE: if the desiredGrants is empty, then delete the group.
		String groupname = req.getParameter(GROUPNAME);
		if (groupname == null || groupname.length() == 0) {
			errorMissingParam(resp);
			return;
		}

		try {
			GrantedAuthority grant = new GrantedAuthorityImpl(groupname);
			
			GrantedAuthorityHierarchyTable.assertGrantedAuthorityHierarchy( 
												grant, desiredGrants, cc );
			
			if ( desiredGrants.isEmpty() ) {
				// delete all users assigned to this group...
				List<String> empty = Collections.emptyList();
				UserGrantedAuthority.assertGrantedAuthoryMembers(grant, empty, cc);
			}
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		}

		resp.sendRedirect(cc.getWebApplicationURL(GroupManagementServlet.ADDR));
	}
}
