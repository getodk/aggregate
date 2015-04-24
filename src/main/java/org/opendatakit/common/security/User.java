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
package org.opendatakit.common.security;

import java.util.Set;

import org.springframework.security.core.GrantedAuthority;


/**
 * Minimal features of a user.  Note that the security permissions granted a 
 * user are not defined here.  That is a security aspect that doesn't get 
 * exposed to the application layer.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public interface User {
	public static final String ANONYMOUS_USER = "anonymousUser";
	public static final String ANONYMOUS_USER_NICKNAME = "Anonymous Access";

	public static final String DAEMON_USER = "aggregate.opendatakit.org:web-service";
	public static final String DAEMON_USER_NICKNAME = "Background Task Account";
	/**
	 * @return User-friendly name.
	 */
	public String getNickname();
	
	/**
	 * 
	 * @return user@domain.com if an OAuth2 authenticated user.  Otherwise null.
	 */
	public String getEmail();
	
	/**
	 * @return user id of the form "uid:username|timestamp" 
	 * 		or "mailto:user@domain.com" (authenticated but not registered)
	 *      or ANONYMOUS_USER or DAEMON_USER
	 */
	public String getUriUser();

	/**
	 * @return set of groups the user belongs to.
	 */
	public Set<GrantedAuthority> getGroups();
	
	/**
	 * @return set of directly granted authorities.
	 */
	public Set<GrantedAuthority> getDirectAuthorities();

	/**
	 * @return true if this user is the anonymous user.
	 */
	public boolean isAnonymous();
	
	/**
	 * @return true if this user is a registered user.
	 * @return
	 */
	public boolean isRegistered();
}
