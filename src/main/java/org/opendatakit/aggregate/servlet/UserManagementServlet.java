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

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.common.GrantedAuthorityNames;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.opendatakit.common.web.CallingContext;
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
		List<? extends CommonFieldsBase> usersList;
		try {
			Query query = RegisteredUsersTable.createQuery(ds, user);
			RegisteredUsersTable.applyNaturalOrdering(query);
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
		out.write("<p>Users are identified by their ODK Aggregate usernames.  To simplify data entry, " +
				"you can cut-and-paste e-mail addresses into the box below; the username " +
				"of the e-mail address will be used as the ODK Aggregate username. " +
				"Individual e-mail addresses should be separated by " +
				"whitespace, commas or semicolons; in general, you should be able to " +
				"cut-and-paste the To: line from your " +
				"e-mail program into the box below, and things should work fine.</p>" +
				"<p>E-mail addresses can be of either form:</p>" +
				"<ul><li>mitchellsundt@gmail.com</li>" +
				"<li>\"Mitch Sundt\" &lt;mitchellsundt@gmail.com&gt;</li></ul>" +
				"<p>Alternatively, if you simply " +
				"enter usernames separated by whitespace, commas or semicolons, " +
				"the system will create those usernames without any associated e-mail " +
				"address.</p>" +
				"<p>If you enter duplicate usernames or two or more e-mails that collapse to " +
				"the same username, only one ODK Aggregate username will be created.</p>" +
				"<hr/>");
		out.write(HtmlUtil.createFormBeginTag(cc
				.getWebApplicationURL(UserBulkModifyServlet.ADDR), null,
				HtmlConsts.POST));

		out.write("Usernames (or e-mail addresses) to add or update:" +
				"<br/><textarea  name=\"" + UserBulkModifyServlet.EMAIL_ADDRESSES + "\" rows=\"20\" cols=\"60\"></textarea>");
		out.write(HtmlUtil.createInput("submit", null, "Add or update"));
		out.write("</form>");

		/**
		 * Modify status of existing registered users.
		 */
		
		out.write("<hr/><h2>All Registered Users</h2>");
		
		out.print("<p>Authenticated submissions from ODK Collect 1.1.6 (or higher) require the use of passwords that are " +
				"held on this server and that are specific to this server (referred to as <em>Aggregate passwords</em>). " +
				"Site administrators can set or change Aggregate passwords <a href=\"" + 
				cc.getWebApplicationURL(UserManagePasswordsServlet.ADDR) + "\">here</a>.  By default," +
				" users are not assigned an Aggregate password and so will not be able to do authenticated " +
				"submissions until a site administrator assigns them a password or until they log in using OpenID " +
				"and set their own password. Logging in via OpenID currently requires a gmail.com account.</p>" +
				"<p>If usernames are not associated with gmail accounts, those users can only " +
				"log in with their Aggregate password. For those users, a site administrator must " +
				"visit the above link to set an Aggregate password before they can gain access " +
				"to the system.</p>");
		out.print("<p>Users, once logged in, can reset their Aggregate passwords by visiting the 'Change Password' page.</p>");

		out.print(HtmlConsts.TABLE_OPEN);

		String[] headers = new String[] { "", "Username<br/>(e-mail address)", 
							"Recognize as a<br/>registered user", 
							"Accept<br>Aggregate<br/>passwords",
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
						createEditButton(registeredUser.getUsername(), cc)));
			
            out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
            		userNameAndMembershipLink(registeredUser, cc)));
            out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,
            		cleanBoolean(registeredUser.getIsEnabled()) ? CENTERED_YES : CENTERED_NO));
            out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,
            		cleanBoolean(registeredUser.getIsCredentialNonExpired()) ? CENTERED_YES : CENTERED_NO));
            try {
				out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
						groupMemberships(registeredUser.getUri(), cc)));
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
	
	private String userNameAndMembershipLink(RegisteredUsersTable u, CallingContext cc) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(UserModifyMembershipsServlet.USERNAME, u.getUsername());
        String display = u.getDisplayName();
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
	
	public String createEditButton(String username, CallingContext cc) {
		StringBuilder html = new StringBuilder();
		html.append(HtmlConsts.LINE_BREAK);
		html.append(HtmlUtil.createFormBeginTag(cc
				.getWebApplicationURL(UserModifyServlet.ADDR), null,
				HtmlConsts.GET));

		html.append(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, 
					UserModifyServlet.USERNAME, username));
		html.append(HtmlUtil.createInput("submit", null, "Edit"));
		html.append("</form>");
		return html.toString();
	}
}
