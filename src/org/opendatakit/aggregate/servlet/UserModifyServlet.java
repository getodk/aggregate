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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.security.spring.UserGrantedAuthority;

/**
 * Displays the enabled/disabled state of an individual user.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UserModifyServlet extends ServletUtilBase {

	/**
	 * Serial number for serialization
	 */
	private static final long serialVersionUID = 3078038743780061473L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "access/user-modify";
	
	public static final String TITLE_INFO = "Edit User Information";
	
	public static final String USERNAME = "username";
	
	public static final String DELETE = "delete";
	
	public static final String IS_ENABLED = "is-enabled";
	
	public static final String CREDENTIALS_EXPIRED = "credentials-expired";
	

	/**
	 * Handler for HTTP Get request that adds a new username to the registered users list.
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		// get parameter
		String username = getParameter(req, USERNAME);
		if (username == null || username.length() == 0) {
			errorMissingParam(resp);
			return;
		}
		
		username = SecurityUtils.normalizeUsername(username, 
				cc.getUserService().getCurrentRealm().getMailToDomain());
		
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		RegisteredUsersTable userDefinition = null;
		try {
			RegisteredUsersTable relation = RegisteredUsersTable.assertRelation(ds, user);
		
			try {
				userDefinition = ds.getEntity(relation, username, user);
			} catch ( ODKEntityNotFoundException e ) {
				userDefinition = ds.createEntityUsingRelation(relation, user);
				userDefinition.setUriUser(username);
				userDefinition.setIsCredentialNonExpired(false); // this refers to both passwords...
				userDefinition.setIsEnabled(true);
				ds.putEntity(userDefinition, user);
			}
		} catch ( ODKDatastoreException e ) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
		}
		

		beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info

		PrintWriter out = resp.getWriter();

		String eMail = SecurityUtils.getEmailAddress(userDefinition.getUriUser());

		out.write("<h3>" + eMail + "</h3>");

		out.write(HtmlUtil.createFormBeginTag(cc
				.getWebApplicationURL(UserModifyServlet.ADDR), null,
				HtmlConsts.POST));
		out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
				UserModifyServlet.USERNAME, userDefinition.getUriUser()));
		out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
				UserModifyServlet.DELETE, UserModifyServlet.DELETE));
		out.write(HtmlUtil.createInput("submit", null, "Delete " + eMail));
		out.write("</form>");
		
		out.write(HtmlConsts.LINE_BREAK);

		Map<String,String> properties = new HashMap<String,String>();
		properties.put(UserModifyMembershipsServlet.USERNAME, username);
		out.write(HtmlUtil.createHrefWithProperties(
				cc.getWebApplicationURL(UserModifyMembershipsServlet.ADDR),
				properties, "View or modify group memberships"));
		
		out.write(HtmlConsts.LINE_BREAK);

		out.write(HtmlUtil.createFormBeginTag(cc
				.getWebApplicationURL(UserModifyServlet.ADDR), null,
				HtmlConsts.POST));
		
		out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
				UserModifyServlet.USERNAME, userDefinition.getUriUser()));
		
		out.write(HtmlConsts.LINE_BREAK);
		out.write(HtmlUtil.createRadio(IS_ENABLED, "enable", "Recognize this account as a registered user", userDefinition.getIsEnabled()));
		out.write(HtmlUtil.createRadio(IS_ENABLED, "disable", "Do not recognize this account as a registered user", !userDefinition.getIsEnabled()));
		out.write(HtmlConsts.LINE_BREAK);
		out.write(HtmlUtil.createRadio(CREDENTIALS_EXPIRED, "active", "Allow locally-authenticated (device) logins on this account", userDefinition.getIsCredentialNonExpired()));
		out.write(HtmlUtil.createRadio(CREDENTIALS_EXPIRED, "expired", "Do not allow locally-authenticated (device) logins on this account", !userDefinition.getIsCredentialNonExpired()));
		out.write(HtmlConsts.LINE_BREAK);
		out.write(HtmlUtil.createInput("submit", null, "Update"));
		out.write("</form>");
		finishBasicHtmlResponse(resp);
	}

	private void deleteUser(HttpServletResponse resp, String username,
			CallingContext cc) throws IOException, ODKDatastoreException {
		
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		
		// first, delete all group memberships.
		{
			UserGrantedAuthority relation = UserGrantedAuthority.assertRelation(ds, user);
			Query query = ds.createQuery(relation, user);
			query.addFilter(relation.user, FilterOperation.EQUAL, username);
			List<?> keys = query.executeDistinctValueForDataField(relation.primaryKey);
			List<EntityKey> memberships = new ArrayList<EntityKey>();
			for ( Object o : keys ) {
				String uri = (String) o;
				memberships.add(new EntityKey(relation, uri));
			}
			ds.deleteEntities(memberships, user);
		}

		// now delete the user's entry itself...
		try {
			RegisteredUsersTable relation = RegisteredUsersTable.assertRelation(ds, user);
			RegisteredUsersTable userDefinition = ds.getEntity(relation, username, user);
			// delete it if found...
			ds.deleteEntity(new EntityKey(relation, userDefinition.getUriUser()), user);
		} catch ( ODKEntityNotFoundException e ) {
			// it is fine if it isn't there...
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
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

		// handle delete requests...
		String delete = getParameter(req, DELETE);
		if (delete != null && delete.length() != 0) {
			try {
				deleteUser(resp, username, cc);
			} catch ( ODKDatastoreException e ) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
						ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
				return;
			}

			resp.sendRedirect(cc.getWebApplicationURL(UserManagementServlet.ADDR));
			return;
		}
			
		
		String isEnabled = getParameter(req, IS_ENABLED);
		if (isEnabled == null || isEnabled.length() == 0) {
			errorMissingParam(resp);
			return;
		}
		boolean enabled = false;
		enabled = isEnabled.compareToIgnoreCase("enable") == 0;
		
		
		String isCredentialExpired = getParameter(req, CREDENTIALS_EXPIRED);
		if ( isCredentialExpired == null || isCredentialExpired.length() == 0) {
			errorMissingParam(resp);
			return;
		}
		boolean credentialExpired = false;
		credentialExpired = isCredentialExpired.compareToIgnoreCase("expired") == 0;
		
		
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		RegisteredUsersTable userDefinition = null;
		try {
			RegisteredUsersTable relation = RegisteredUsersTable.assertRelation(ds, user);
		
			try {
				userDefinition = ds.getEntity(relation, username, user);
				userDefinition.setIsEnabled(enabled);
				userDefinition.setIsCredentialNonExpired(!credentialExpired);
				ds.putEntity(userDefinition, user);
			} catch ( ODKEntityNotFoundException e ) {
				userDefinition = ds.createEntityUsingRelation(relation, user);
				userDefinition.setUriUser(username);
				userDefinition.setIsCredentialNonExpired(!credentialExpired); // this refers to both passwords...
				userDefinition.setIsEnabled(enabled);
				ds.putEntity(userDefinition, user);
			}
		} catch ( ODKDatastoreException e ) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		} finally {
			cc.getUserService().reloadPermissions();
		}

		resp.sendRedirect(cc.getWebApplicationURL(UserManagementServlet.ADDR));
	}
}