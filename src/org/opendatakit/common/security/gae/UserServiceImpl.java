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

import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UserServiceImpl implements org.opendatakit.common.security.UserService {

	final Realm realm;
	final User anonymous;
	final User daemonAccount;
	private UserService userService;

	public UserServiceImpl() {
		userService = UserServiceFactory.getUserService();
		List<String> domains = new ArrayList<String>();
		domains.add("aggregate.test.org");
		domains.add("test.net");
		realm = new RealmImpl(RealmImpl.GAE_REALM, "gmail.com", "test.org",
				domains);
		anonymous = new UserImpl(false);
		daemonAccount = new UserImpl(true);
	}

	@Override
	public String createLoginURL(String destinationURL) {
		return userService.createLoginURL(destinationURL);
	}

	@Override
	public String createLogoutURL(String destinationURL) {
		return userService.createLogoutURL(destinationURL);
	}

	@Override
	public Realm getCurrentRealm() {
		return realm;
	}

	@Override
	public User getCurrentUser() {
		com.google.appengine.api.users.User gaeUser = userService.getCurrentUser();
		if ( gaeUser == null ) {
			return anonymous;
		} else {
			return new UserImpl(gaeUser);
		}
	}

	@Override
	public boolean isUserLoggedIn() {
		return userService.isUserLoggedIn();
	}

	@Override
	public User getDaemonAccountUser() {
		return daemonAccount;
	}

}
