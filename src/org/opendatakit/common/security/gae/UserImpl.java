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

import com.google.appengine.api.users.User;

public class UserImpl implements org.opendatakit.common.security.User {

	private final boolean privileged;
	private final User gaeUser;
	private final List<String> groups = new ArrayList<String>();

	public UserImpl(User user) {
		this.privileged = false;
		gaeUser = user;
	}

	public UserImpl(boolean privileged) {
		this.privileged = privileged;
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

	@Override
	public List<String> getGroups() {
		return groups;
	}

	@Override
	public String getPasswordTreatment() {
		return null;
	}

	@Override
	public String getRealmString() {
		return RealmImpl.GAE_REALM;
	}

	@Override
	public String getUriUser() {
		if (gaeUser == null) {
			if ( privileged ) {
				return ANONYMOUS_USER;
			} else {
				return DAEMON_USER_NICKNAME;
			}
		}
		return "mailto:" + gaeUser.getEmail();
	}
}
