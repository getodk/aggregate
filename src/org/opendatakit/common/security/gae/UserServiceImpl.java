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

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;
import org.springframework.beans.factory.InitializingBean;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UserServiceImpl implements org.opendatakit.common.security.UserService, InitializingBean {

	Realm realm;
	User anonymous;
	User daemonAccount;
	private UserService userService;
	private OAuthService oauth;


	public UserServiceImpl() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if ( realm == null ) {
			throw new IllegalStateException("realm must be set");
		}
		userService = UserServiceFactory.getUserService();
        oauth = OAuthServiceFactory.getOAuthService();

        anonymous = new UserImpl(realm.getRealmString(), false);
		daemonAccount = new UserImpl(realm.getRealmString(), true);
	}

	public Realm getRealm() {
		return realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
	}

	@Override
	public String createLoginURL() {
		return userService.createLoginURL(ServletConsts.WEB_ROOT);
	}

	@Override
	public String createLogoutURL() {
		return userService.createLogoutURL(ServletConsts.WEB_ROOT);
	}

	@Override
	public Realm getCurrentRealm() {
		return realm;
	}

	@Override
	public User getCurrentUser() {
		com.google.appengine.api.users.User gaeUser = null;
		gaeUser = userService.getCurrentUser();
		if ( gaeUser != null ) {
			return new UserImpl(realm.getRealmString(), gaeUser);
		}
		return anonymous;
	}

	public User getCurrentOAuthUser() throws OAuthRequestException {
		com.google.appengine.api.users.User gaeUser = null;
		gaeUser = oauth.getCurrentUser();
		if ( gaeUser != null ) {
			return new UserImpl(realm.getRealmString(), gaeUser);
		}
		return anonymous;
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
