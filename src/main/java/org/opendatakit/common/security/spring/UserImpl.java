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
package org.opendatakit.common.security.spring;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserImpl implements org.opendatakit.common.security.User {

	final String nickName;
	final String email;
	final String uriUser;
	final Set<GrantedAuthority> groups = new HashSet<GrantedAuthority>();
	final Set<GrantedAuthority> directAuthorities = new HashSet<GrantedAuthority>();
	final Datastore datastore;
	Map<String, Set<GrantedAuthority> > formIdGrantedAuthorities = null;
	
	
	UserImpl(String uriUser, String email, String nickName,
			Collection<? extends GrantedAuthority> groupsAndGrantedAuthorities,
			Datastore datastore) {
		this.uriUser = uriUser;
		this.email = email;
		this.nickName = nickName;
		this.datastore = datastore;
		for ( GrantedAuthority g : groupsAndGrantedAuthorities ) {
			if ( GrantedAuthorityName.permissionsCanBeAssigned(g.getAuthority()) ) {
				groups.add(g);
			}
		}
		this.directAuthorities.addAll(groupsAndGrantedAuthorities);
	}
	
	@Override
	public String getNickname() {
		return nickName;
	}

	@Override
	public String getEmail() {
		return email;
	}
	
	public Set<GrantedAuthority> getGroups() {
		return Collections.unmodifiableSet(groups);
	}

	public Set<GrantedAuthority> getDirectAuthorities() {
		return Collections.unmodifiableSet(directAuthorities);
	}

	@Override
	public String getUriUser() {
		return uriUser;
	}

	@Override
	public boolean isAnonymous() {
		return uriUser.equals(User.ANONYMOUS_USER);
	}
	
	@Override
	public boolean isRegistered() {
		return groups.contains(new SimpleGrantedAuthority(GrantedAuthorityName.USER_IS_REGISTERED.name()));
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj == null || !(obj instanceof org.opendatakit.common.security.User)) {
			return false;
		}
		
		org.opendatakit.common.security.User u = (org.opendatakit.common.security.User) obj;
		return u.getUriUser().equals(getUriUser());
	}

	@Override
	public int hashCode() {
		return getUriUser().hashCode();
	}
}
