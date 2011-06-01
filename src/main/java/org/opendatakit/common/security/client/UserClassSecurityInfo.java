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

import java.util.TreeSet;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Transport object for returning the authorities assigned to a class of users.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class UserClassSecurityInfo implements Comparable<UserClassSecurityInfo>, IsSerializable {
	String userClassName;
	TreeSet<GrantedAuthorityInfo> assignedUserGroups = new TreeSet<GrantedAuthorityInfo>();
	TreeSet<GrantedAuthorityInfo> assignedGrantedAuthorities = new TreeSet<GrantedAuthorityInfo>();
	TreeSet<GrantedAuthorityInfo> grantedAuthorities = new TreeSet<GrantedAuthorityInfo>();
	
	public UserClassSecurityInfo() {	
	}
	
	public UserClassSecurityInfo(String userClassName) {
		this.userClassName = userClassName;
	}

	public String getUserClassName() {
		return userClassName;
	}

	public void setUserClassName(String userClassName) {
		this.userClassName = userClassName;
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

	public TreeSet<GrantedAuthorityInfo> getAssignedGrantedAuthorities() {
		return assignedGrantedAuthorities;
	}

	public void setAssignedGrantedAuthorities(TreeSet<GrantedAuthorityInfo> authorities) {
		assignedGrantedAuthorities.clear();
		if ( authorities != null ) {
			assignedGrantedAuthorities.addAll(authorities);
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
		UserClassSecurityInfo info = (UserClassSecurityInfo) obj;
		return userClassName.equals(info.userClassName);
	}

	@Override
	public int hashCode() {
		return userClassName.hashCode();
	}

	@Override
	public int compareTo(UserClassSecurityInfo o) {
		return userClassName.compareTo(o.userClassName);
	}

}
