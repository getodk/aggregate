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
import org.springframework.security.core.GrantedAuthority;

// TODO: implement
public class UserImpl implements org.opendatakit.common.security.User {

	final String nickName;
	final String passwordTreatment;
	final String realmString;
	final String uriUser;
	final Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
	final Set<GrantedAuthority> groups = new HashSet<GrantedAuthority>();
	final Datastore datastore;
	Map<String, Set<GrantedAuthority> > formIdGrantedAuthorities = null;
	
	
	UserImpl(String uriUser, String realmString, String nickName,
			 String passwordTreatment, 
			 Collection<GrantedAuthority> groupsAndGrantedAuthorities,
			 Datastore datastore) {
		this.uriUser = uriUser;
		this.realmString = realmString;
		this.nickName = nickName;
		this.passwordTreatment = passwordTreatment;
		this.datastore = datastore;
		for ( GrantedAuthority g : groupsAndGrantedAuthorities ) {
			if ( g.getAuthority().startsWith("ROLE_") ) {
				grantedAuthorities.add(g);
			} else {
				groups.add(g);
			}
		}
		grantedAuthorities.addAll(GroupGrantedAuthority.getGrantedAuthorities(groups, datastore, this));
	}
	
	@Override
	public String getNickname() {
		return nickName;
	}

	public Set<GrantedAuthority> getGroups() {
		return Collections.unmodifiableSet(groups);
	}

	public Set<GrantedAuthority> getGrantedAuthorities() {
		return Collections.unmodifiableSet(grantedAuthorities);
	}

	@Override
	public String getPasswordTreatment() {
		return passwordTreatment;
	}

	@Override
	public String getRealmString() {
		return realmString;
	}

	@Override
	public String getUriUser() {
		return uriUser;
	}

	public Set<GrantedAuthority> getFormIdGrantedAuthorities(String formId) {
		if ( formIdGrantedAuthorities == null ) {
			formIdGrantedAuthorities = GroupFormIdGrantedAuthority.getAllGrantedAuthorities(groups, datastore, this);
		}
		
		Set<GrantedAuthority> s = formIdGrantedAuthorities.get(formId);
		if ( s == null ) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableSet(s);
		}
	}

	@Override
	public boolean isAnonymous() {
		return uriUser.equals(User.ANONYMOUS_USER);
	}
}
