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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.parser.MultiPartFormData;
import org.opendatakit.aggregate.parser.MultiPartFormItem;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.spring.GrantedAuthorityNames;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
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
	public static final String GROUPNAME = "_groupname";
	public static final String SHOWALL = "_showall";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, ADDR, req);

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

		TreeSet<String> users;
		TreeSet<String> allUsers;
		try {
			users = UserGrantedAuthority.getUriUsers(new GrantedAuthorityImpl(groupname), ds, user);
			if ( showall ) {
				allUsers = new TreeSet<String>();
				RegisteredUsersTable relation = RegisteredUsersTable.assertRelation(ds, user);
				Query query = ds.createQuery(relation, user);
				List<?> registeredUsers = query.executeDistinctValueForDataField(relation.primaryKey);
				for ( Object u : registeredUsers ) {
					allUsers.add((String) u);
				}
			} else {
				allUsers = users;
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
										HtmlConsts.MULTIPART_FORM_DATA,
										HtmlConsts.POST));
		out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, 
					GroupModifyMembersServlet.GROUPNAME, groupname));

		out.print(HtmlConsts.BORDERLESS_TABLE_OPEN);
		for ( String uriUser : allUsers ) {
			out.print(HtmlConsts.TABLE_ROW_OPEN);
	        out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
	        		HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_CHECKBOX, 
	        				uriUser, uriUser,
	        								users.contains(uriUser)) +
	        								createUserModifyLink(uriUser, cc)));
			out.print(HtmlConsts.TABLE_ROW_CLOSE);
		}
		out.print(HtmlConsts.TABLE_CLOSE);
        out.write(HtmlConsts.LINE_BREAK);
		out.write(HtmlUtil.createInput("submit", null, "Update"));
		out.write("</form>");
		finishBasicHtmlResponse(resp);
	}
	
	private final String createUserModifyLink( String uriUser, CallingContext cc ) {
		if ( uriUser == null ) return null;
		if ( !uriUser.startsWith(SecurityUtils.MAILTO_COLON) ) return uriUser;
		String display = SecurityUtils.getEmailAddress(uriUser);
		Map<String,String> properties = new HashMap<String,String>();
		properties.put(UserModifyServlet.USERNAME, uriUser);
		return HtmlUtil.createHrefWithProperties(cc.getWebApplicationURL(UserModifyServlet.ADDR), 
						properties, display);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		CallingContext cc = ContextFactory.getCallingContext(this, ADDR, req);

		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		String groupname;
		try {
			MultiPartFormData mfd = new MultiPartFormData(req);
			groupname = mfd.getFormDataByFieldName(GROUPNAME).getStream().toString();
			if (groupname == null || groupname.length() == 0) {
				errorMissingParam(resp);
				return;
			}
			
			// get the list of desired members
			TreeSet<String> desiredMembers = new TreeSet<String>();
			for ( Map.Entry<String,MultiPartFormItem> e : mfd.getFieldNameEntrySet() ) {
				String key = e.getKey();
				if ( GROUPNAME.equals(key) ) continue;
				if ( groupname.equals(key) ) continue;
				desiredMembers.add(key);
			}
			
			UserGrantedAuthority relation = UserGrantedAuthority.assertRelation(ds, user);
			
			// get the members as currently defined for this group 
			List<? extends CommonFieldsBase> membersList;
			Query query = ds.createQuery(relation, user);
			query.addFilter(relation.grantedAuthority, FilterOperation.EQUAL, groupname);
			membersList = query.executeQuery(0);

			// OK we have the desired and actual members lists for this groupname.
			// find the set of members to remove...
			List<EntityKey> deleted = new ArrayList<EntityKey>();
			for ( CommonFieldsBase b : membersList ) {
				UserGrantedAuthority t = (UserGrantedAuthority) b;
				String uriUser = t.getUser();
				if ( desiredMembers.contains(uriUser) ) {
					desiredMembers.remove(uriUser);
				} else {
					deleted.add(new EntityKey(t, t.getUri()));
				}
			}
			// we now have the list of desiredMembers to insert, and the list of 
			// existing records to delete...
			GrantedAuthority group = new GrantedAuthorityImpl(groupname);
			List<UserGrantedAuthority> added = new ArrayList<UserGrantedAuthority>();
			for ( String uriUser : desiredMembers ) {
				UserGrantedAuthority t = ds.createEntityUsingRelation(relation, user);
				t.setUser(uriUser);
				t.setGrantedAuthority(group);
				added.add(t);
			}

			// we now have the list of EntityKeys to delete, and the list of records to add -- do it.
			ds.putEntities(added, user);
			ds.deleteEntities(deleted, user);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		} catch (FileUploadException e) {
			e.printStackTrace();
			errorBadParam(resp);
			return;
		}

		Map<String,String> properties = new HashMap<String,String>();
		properties.put(GROUPNAME, groupname);
		String link = HtmlUtil.createLinkWithProperties(
				cc.getWebApplicationURL(GroupModifyMembersServlet.ADDR), properties);
		resp.sendRedirect(link);
	}

}
