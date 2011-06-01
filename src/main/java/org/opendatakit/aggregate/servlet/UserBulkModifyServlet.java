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
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.security.common.EmailParser.Email;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.web.CallingContext;

/**
 * Supports creating a set of users and setting their credential values
 * as a single operation.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UserBulkModifyServlet extends ServletUtilBase {

	/**
	 * Serial number for serialization
	 */
	private static final long serialVersionUID = 3078038743780061473L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "access/user-bulk-modify";
	
	public static final String TITLE_INFO = "Define User Information for a Group of Users";
	
	public static final String EMAIL_ADDRESSES = "emailAddresses";

	/**
	 * Handler for HTTP Post request that adds a set of usernames to the registered users list.
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		// get parameter
		String usernames = getParameter(req, EMAIL_ADDRESSES);
		if (usernames == null || usernames.length() == 0) {
			errorMissingParam(resp);
			return;
		}
		
		Collection<Email> userEmails = EmailParser.parseEmails(usernames);
		
		try {
		
			for ( Email userEmail : userEmails ) {
				// NOTE: this also sets the Uri within the Email object...
				RegisteredUsersTable.assertActiveUserByUsername(userEmail, cc);
			}
		} catch ( ODKDatastoreException e ) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		}
		
		resp.sendRedirect(cc.getWebApplicationURL(UserManagementServlet.ADDR));
	}
}