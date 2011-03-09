/*
 * Copyright (C) 2010 University of Washington
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.spring.GrantedAuthorityNames;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

/**
 * Displays the registered users of the system and supports
 * adding or deleting the registered user.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UserManagementServlet extends ServletUtilBase {

	/**
	 * Serial number for serialization
	 */
	private static final long serialVersionUID = 3078038743780061473L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "access/user-management";

	/**
	 * Title for generated webpage
	 */
	public static final String TITLE_INFO = "Manage Registered Users";

	public static final String CENTERED_YES = "<div align=\"CENTER\">yes</div>";
	public static final String CENTERED_NO = "<div align=\"CENTER\">no</div>";
	/**
	 * Handler for HTTP Get request that responds with an XML list of forms to
	 * download
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		RegisteredUsersTable relation;
		List<? extends CommonFieldsBase> usersList;
		try {
			relation = RegisteredUsersTable.assertRelation(ds, user);
			Query query = ds.createQuery(relation, user);
			query.addSort(relation.primaryKey, Direction.ASCENDING);
			usersList = query.executeQuery(0);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		}

		beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info

		PrintWriter out = resp.getWriter();
		
		out.write("<p>Add the users of your system to the list of registered users (below). " +
				"Then administer the rights for those users by assigning them to groups " +
				"and granting specific authority to specific groups.</p>");
		/**
		 * Add new registered user...
		 */
		out.write("<hr/><h2>Add or Update User</h2>");
		out.write(HtmlUtil.createFormBeginTag(cc
				.getWebApplicationURL(UserModifyServlet.ADDR), null,
				HtmlConsts.GET));

		out.write("Username (e-mail address) to add or update:" + HtmlConsts.LINE_BREAK);
		out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_TEXT,
				UserModifyServlet.USERNAME, null));
		out.write("<p/><p>If no '@domain' is specified, '@" +
				cc.getUserService().getCurrentRealm().getMailToDomain() +
				"' will be appended</p>");
		out.write(HtmlUtil.createInput("submit", null, "Add or update"));
		out.write("</form>");

		/**
		 * Modify status of existing registered users.
		 */
		
		out.write("<hr/><h2>All Registered Users</h2>");
		
		out.print(HtmlConsts.TABLE_OPEN);

		String[] headers = new String[] { "", "Username<br/>(e-mail address)", 
							"Recognize as a<br/>registered user", 
							"Accept<br>locally-managed (device)<br/>credentials",
							"Assigned groups"};

		for (String header : headers) {
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_HEADER, header));
	    }

		int recordNum = 0;
		for ( CommonFieldsBase b : usersList ) {
			++recordNum;
			out.print(HtmlConsts.TABLE_ROW_OPEN);
			RegisteredUsersTable registeredUser = (RegisteredUsersTable) b;
			
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,
						createEditButton(registeredUser.getUriUser(), cc)));
			
            out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
            		userNameAndMembershipLink(registeredUser.getUriUser(), cc)));
            out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,
            		cleanBoolean(registeredUser.getIsEnabled()) ? CENTERED_YES : CENTERED_NO));
            out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,
            		cleanBoolean(registeredUser.getIsCredentialNonExpired()) ? CENTERED_YES : CENTERED_NO));
            try {
				out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
						groupMemberships(registeredUser.getUriUser(), cc)));
			} catch (ODKDatastoreException e) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
						ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
				return;
			}

            out.print(HtmlConsts.TABLE_ROW_CLOSE);
		}
		out.print(HtmlConsts.TABLE_CLOSE);
		finishBasicHtmlResponse(resp);
	}
	
	private String userNameAndMembershipLink(String username, CallingContext cc) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(UserModifyMembershipsServlet.USERNAME, username);
        String display = username;
        if ( username != null && username.startsWith(SecurityUtils.MAILTO_COLON)) {
        	display = username.substring(SecurityUtils.MAILTO_COLON.length());
        }
        return display + "<br/>" + HtmlUtil.createHrefWithProperties(
    		cc.getWebApplicationURL(UserModifyMembershipsServlet.ADDR), properties,
        	"View or update this user's group memberships");
	}
	
	private String groupNameLink( String groupname, CallingContext cc ) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(GroupModifyServlet.GROUPNAME, groupname);
    
        return HtmlUtil.createHrefWithProperties(
        		cc.getWebApplicationURL(GroupModifyServlet.ADDR), properties,
        		groupname );
	}

	private String groupMemberships(String uriUser, CallingContext cc ) throws ODKDatastoreException {
		TreeSet<String> groupNames = new TreeSet<String>();
		Set<GrantedAuthority> authorities = UserGrantedAuthority.getGrantedAuthorities(uriUser, 
											cc.getDatastore(), cc.getCurrentUser());
		// alphabetize...
		for ( GrantedAuthority a : authorities ) {
			groupNames.add(a.getAuthority());
		}
		return buildList(groupNames, cc);
	}
	
	private String buildList(TreeSet<String> orderedSet, CallingContext cc) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for ( String g : orderedSet ) {
			if ( !first ) {
				b.append("<br/>");
			}
			first = false;
			if ( GrantedAuthorityNames.permissionsCanBeAssigned(g)) {
				b.append(groupNameLink(g, cc));
			} else {
				b.append(g);
			}
		}
		return b.toString();
	}

	public boolean cleanBoolean(Boolean b ) {
		if ( b == null ) return false;
		return b;
	}
	
	public String createEditButton(String uriUser, CallingContext cc) {
		StringBuilder html = new StringBuilder();
		html.append(HtmlConsts.LINE_BREAK);
		html.append(HtmlUtil.createFormBeginTag(cc
				.getWebApplicationURL(UserModifyServlet.ADDR), null,
				HtmlConsts.GET));

		html.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, UserModifyServlet.USERNAME, uriUser));
		html.append(HtmlUtil.createInput("submit", null, "Edit"));
		html.append("</form>");
		return html.toString();
	}
}
