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
package org.opendatakit.common.security.gae;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.google.appengine.api.users.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UserImpl implements org.opendatakit.common.security.User {

	private final String realmString;
	private final boolean privileged;
	private final User gaeUser;
	private final List<GrantedAuthority> groups = new ArrayList<GrantedAuthority>();

	public UserImpl(String realmString, User user) {
		this.privileged = false;
		this.realmString = realmString;
		gaeUser = user;
	}

	public UserImpl(String realmString, boolean privileged) {
		this.privileged = privileged;
		this.realmString = realmString;
		gaeUser = null;
	}
	
	@Override
	public String getNickname() {
		if (gaeUser == null) {
			if ( privileged ) {
				return DAEMON_USER_NICKNAME;
			} else {
				return ANONYMOUS_USER_NICKNAME;
			}
		}
		return gaeUser.getNickname();
	}

	public List<GrantedAuthority> getGroups() {
		return groups;
	}

	@Override
	public String getPasswordTreatment() {
		return null;
	}

	@Override
	public String getRealmString() {
		return realmString;
	}

	@Override
	public String getUriUser() {
		if (gaeUser == null) {
			if ( privileged ) {
				return DAEMON_USER_NICKNAME;
			} else {
				return ANONYMOUS_USER;
			}
		}
		return "mailto:" + gaeUser.getEmail();
	}

	@Override
	public boolean isAnonymous() {
		return (gaeUser == null) && !privileged;
	}
}
