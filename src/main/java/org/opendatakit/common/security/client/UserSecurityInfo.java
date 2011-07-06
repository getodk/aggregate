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

package org.opendatakit.common.security.client;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * Heavy object comparable to User that is transportable across the GWT interfaces.
 * If requested, it provides the full set of authorizations and group memberships that
 * a user possesses (so that the client can show or hide tabs as appropriate).  The
 * username is a unique key for this object.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class UserSecurityInfo implements Comparable<UserSecurityInfo>, Serializable {

	/**
   * 
   */
  private static final long serialVersionUID = 7581021818962882604L;

  public enum UserType implements Serializable {
		ANONYMOUS,     // not authenticated (anonymous)
		DAEMON,        // daemon (background) account
		REGISTERED,    // authenticated and registered
		AUTHENTICATED  // authenticated, but not registered
	};
	String username;
	String nickname;
	String email;
	UserType type;
	TreeSet<GrantedAuthorityInfo> assignedUserGroups = new TreeSet<GrantedAuthorityInfo>();
	TreeSet<GrantedAuthorityInfo> grantedAuthorities = new TreeSet<GrantedAuthorityInfo>();
	
	public UserSecurityInfo() {	
	}
	
	public UserSecurityInfo(String username, String nickname, String email, UserType type) {
		this.username = username;
		this.nickname = nickname;
		this.email = email;
		this.type = type;
	}
	
	public UserType getType() {
		return type;
	}

	public void setType(UserType type) {
		this.type = type;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public TreeSet<GrantedAuthorityInfo> getAssignedUserGroups() {
		return assignedUserGroups;
	}

	public void setAssignedUserGroups(TreeSet<GrantedAuthorityInfo> authorities) {
		assignedUserGroups.clear();
		if ( authorities != null ) {
			assignedUserGroups.addAll(authorities);
		}
	}

	public TreeSet<GrantedAuthorityInfo> getGrantedAuthorities() {
		return grantedAuthorities;
	}

	public void setGrantedAuthorities(TreeSet<GrantedAuthorityInfo> authorities) {
		grantedAuthorities.clear();
		if ( authorities != null ) {
			grantedAuthorities.addAll(authorities);
		}
	}

	@Override
	public boolean equals(Object obj) {
		UserSecurityInfo info = (UserSecurityInfo) obj;
		return username.equals(info.username);
	}

	@Override
	public int hashCode() {
		return username.hashCode();
	}

	@Override
	public int compareTo(UserSecurityInfo o) {
		return username.compareTo(o.username);
	}
	
}
