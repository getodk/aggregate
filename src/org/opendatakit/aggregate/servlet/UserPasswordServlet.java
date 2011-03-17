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
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.SecurityBeanDefs;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;

/**
 * Allows the logged-in user to change their local password.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UserPasswordServlet extends ServletUtilBase {

	/**
	 * Serial number for serialization
	 */
	private static final long serialVersionUID = 3078038743780061473L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "ssl/user-password";
	
	public static final String TITLE_INFO = "Change User Password";
	
	public static final String USERNAME = "username";

	public static final String PASSWORD_1 = "password-1";
	
	public static final String PASSWORD_2 = "password-2";

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

		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		RegisteredUsersTable userDefinition = null;
		try {
			RegisteredUsersTable relation = RegisteredUsersTable.assertRelation(ds, user);
		
			userDefinition = ds.getEntity(relation, user.getUriUser(), user);
		} catch ( ODKDatastoreException e ) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		}

		beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info

		PrintWriter out = resp.getWriter();
		
		out.write("<p>Logins from a device (e.g., ODK Collect) require a password that is " +
				"held on this server and that is specific to this server (an <em>Aggregate password</em). " +
				"Set or change that password here. The password is stored " +
				"as both a randomly-salted sha-1 hash and as a deterministically-salted md5 hash. " +
				"The plaintext password is not retained. </p>");
		out.write(HtmlConsts.LINE_BREAK);
		out.write("<h3>" + SecurityUtils.getEmailAddress(userDefinition.getUriUser()) + "</h3>");
		
		out.write(HtmlUtil.createFormBeginTag(cc
				.getWebApplicationURL(UserPasswordServlet.ADDR), null,
				HtmlConsts.POST));
		
		out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN,
				UserPasswordServlet.USERNAME, userDefinition.getUriUser()));
		out.write("<table>");
		out.write(HtmlConsts.TABLE_ROW_OPEN);
		out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, "Password:"));
		out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,
					HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_PASSWORD,
							UserPasswordServlet.PASSWORD_1, userDefinition.getUriUser())));
		out.write(HtmlConsts.TABLE_ROW_CLOSE);
		out.write(HtmlConsts.TABLE_ROW_OPEN);
		out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, "Password (again):"));
		out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,
					HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_PASSWORD,
							UserPasswordServlet.PASSWORD_2, userDefinition.getUriUser())));
		out.write(HtmlConsts.TABLE_ROW_CLOSE);
		out.write(HtmlConsts.TABLE_ROW_OPEN);
		out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,
					HtmlUtil.createInput("submit", null, "Update")));
		out.write("</td>");
		out.write(HtmlConsts.TABLE_ROW_CLOSE);
		out.write(HtmlConsts.TABLE_CLOSE);
		out.write("</form>");
		finishBasicHtmlResponse(resp);
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
		
		String pwOne = getParameter(req, PASSWORD_1);
		if (pwOne == null || pwOne.length() == 0) {
			errorMissingParam(resp);
			return;
		}

		String pwTwo = getParameter(req, PASSWORD_2);
		if (pwTwo == null || pwTwo.length() == 0) {
			errorMissingParam(resp);
			return;
		}

		if ( !pwOne.equals(pwTwo) ) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, 
					"Entered password strings are not the same!!");
			return;
		}
		
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		RegisteredUsersTable userDefinition = null;
		try {
			RegisteredUsersTable relation = RegisteredUsersTable.assertRelation(ds, user);
		
			userDefinition = ds.getEntity(relation, username, user);

			MessageDigestPasswordEncoder mde = (MessageDigestPasswordEncoder) cc.getBean(SecurityBeanDefs.BASIC_AUTH_PASSWORD_ENCODER);
			String salt = UUID.randomUUID().toString().substring(0,8);
			String fullPass = mde.encodePassword(pwOne, salt);
			userDefinition.setBasicAuthPassword(fullPass);
			userDefinition.setBasicAuthSalt(salt);
			String fullDigestAuthPass = SecurityUtils.getDigestAuthenticationPasswordHash(
												userDefinition.getUriUser(),
												pwOne, 
												cc.getUserService().getCurrentRealm() );
            userDefinition.setDigestAuthPassword(fullDigestAuthPass);
			ds.putEntity(userDefinition, user);
		} catch ( ODKDatastoreException e ) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		}

		// redirect to landing page to force change in https status.
		resp.sendRedirect(cc.getWebApplicationURL(LandingPageServlet.ADDR));
	}
}