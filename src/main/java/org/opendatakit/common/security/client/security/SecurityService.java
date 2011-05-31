/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.common.security.client.security;

import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.CredentialsInfo;
import org.opendatakit.common.security.client.RealmSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * These are the APIs available to users with the ROLE_USER privilege.
 * Because this interface includes the change-password API, it requires
 * an SSL connection on servers that support SSL.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
@RemoteServiceRelativePath("securityservice")
public interface SecurityService extends RemoteService {
	
	/**
	 * @return information about the logged-in user, including the full set of groups and grants it has.
	 * @throws AccessDeniedException
	 * @throws DatastoreFailureException
	 */
	UserSecurityInfo getUserInfo() throws AccessDeniedException, DatastoreFailureException;
	
	/**
	 * @return the display name of the logged-in user.
	 * @throws AccessDeniedException
	 */
	String getUserDisplayName() throws AccessDeniedException;
	
	/**
	 * @return whether the user is a registered user
	 *  (vs. anonymous or authenticated through OpenId but not registered).
	 * @throws AccessDeniedException
	 */
	boolean isRegisteredUser() throws AccessDeniedException;
	
	/**
	 * @return whether the user is the anonymous user
	 * @throws AccessDeniedException
	 */
	boolean isAnonymousUser() throws AccessDeniedException;

	/**
	 * @param xsrfString
	 * @return information needed for building CredentialsInfo records.
	 * @throws AccessDeniedException
	 */
	RealmSecurityInfo getRealmInfo(String xsrfString) throws AccessDeniedException;

	/**
	 * Change the user's password.
	 * @param xsrfString
	 * @param credentials
	 * @throws AccessDeniedException
	 * @throws DatastoreFailureException
	 */
	void setUserPassword(String xsrfString, CredentialsInfo credentials) throws AccessDeniedException, DatastoreFailureException;
}
