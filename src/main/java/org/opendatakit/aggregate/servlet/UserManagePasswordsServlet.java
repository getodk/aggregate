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
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.CredentialsInfo;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.web.CallingContext;

/**
 * Used by the GWT layer to send change password requests over
 * https if https is available, regardless of whether the GWT
 * layer itself is running under http.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UserManagePasswordsServlet extends ServletUtilBase {

	/**
	 * Serial number for serialization
	 */
	private static final long serialVersionUID = 3078038743780061473L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "ssl/user-manage-passwords";
	
	public static final String USERNAME = "username";
	public static final String DIGEST_AUTH_HASH = "digestAuthHash";
	public static final String BASIC_AUTH_HASH = "basicAuthHash";
	public static final String BASIC_AUTH_SALT = "basicAuthSalt";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if ( req.getScheme().equals("http")) {
			Logger.getLogger(UserManagePasswordsServlet.class.getName()).warning("Setting user passwords over http");
		}
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		String username = req.getParameter(USERNAME);
		String digestAuthHash = req.getParameter(DIGEST_AUTH_HASH);
		String basicAuthHash = req.getParameter(BASIC_AUTH_HASH);
		String basicAuthSalt = req.getParameter(BASIC_AUTH_SALT);
		
		CredentialsInfo credential = new CredentialsInfo();
		credential.setUsername(username);
		credential.setDigestAuthHash(digestAuthHash);
		credential.setBasicAuthHash(basicAuthHash);
		credential.setBasicAuthSalt(basicAuthSalt);
		
		try {
			SecurityServiceUtil.setUserCredentials(credential, cc);
		} catch (AccessDeniedException e1) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad username");
			return;
		} catch (DatastoreFailureException e1) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		}
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}
}