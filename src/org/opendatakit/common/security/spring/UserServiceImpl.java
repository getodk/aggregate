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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserServiceImpl implements org.opendatakit.common.security.UserService, InitializingBean {

	// configured by bean definition...
	Datastore datastore;
	Realm realm;

	User anonymous;
	User daemonAccount;
	final Map<String, User> activeUsers = new HashMap<String, User>();
	
	public UserServiceImpl() {
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if ( realm == null ) {
			throw new IllegalStateException("realm must be configured");
		}
		if ( datastore == null ) {
			throw new IllegalStateException("datastore must be configured");
		}
		Set<GrantedAuthority> anonGroups = new HashSet<GrantedAuthority>();
		anonGroups.add(new GrantedAuthorityImpl(GrantedAuthorityNames.USER_IS_ANONYMOUS.name()));
		anonymous = new UserImpl(User.ANONYMOUS_USER, 
				User.ANONYMOUS_USER_NICKNAME, anonGroups, datastore );
		Set<GrantedAuthority> daemonGroups = new HashSet<GrantedAuthority>();
		daemonGroups = new HashSet<GrantedAuthority>();
		daemonGroups.add(new GrantedAuthorityImpl(GrantedAuthorityNames.USER_IS_DAEMON.name()));
		daemonAccount = new UserImpl(User.DAEMON_USER, 
				User.DAEMON_USER_NICKNAME, daemonGroups, datastore );
		
		activeUsers.put(anonymous.getUriUser(), anonymous);
		activeUsers.put(daemonAccount.getUriUser(), daemonAccount);
	}

	public Datastore getDatastore() {
		return datastore;
	}

	public void setDatastore(Datastore datastore) {
		this.datastore = datastore;
	}

	public Realm getRealm() {
		return realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
	}

	@Override
  public String createLoginURL() {
    return "login.html";
  }

  @Override
  public String createLogoutURL() {
    return "j_spring_security_logout";
  }
  
  @Override
  public Realm getCurrentRealm() {
    return realm;
  }

  @Override
  public synchronized void reloadPermissions() {
	activeUsers.clear();
	activeUsers.put(anonymous.getUriUser(), anonymous);
	activeUsers.put(daemonAccount.getUriUser(), daemonAccount);
  }

  private boolean isAnonymousUser(Authentication auth) {
		if ( auth == null ) {
			throw new NullPointerException("Unexpected null pointer from authentication retrieval");
		} else if ( !auth.isAuthenticated()) {
			throw new IllegalStateException("Unexpected unauthenticated user from authentication retrieval (expect anonymous authentication)");
		} else if ((auth.getPrincipal() instanceof String) && ((String) auth.getPrincipal()).equals("anonymousUser")) {
			return true;
		} else {
			return false;
		}
  }
  
  public String getAuthenticationEmail(Authentication auth) {
		if ( isAnonymousUser(auth) ) {
			Logger.getLogger(UserServiceImpl.class.getCanonicalName()).info("Logged in user: " + anonymous.getUriUser());
			return anonymous.getUriUser();
		} else {
			return auth.getName();
		}
  }
  
  @Override
  public synchronized User getCurrentUser() {
	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	String eMail = getAuthenticationEmail(auth);
	String name = SecurityUtils.getNickname(eMail);

	Logger.getLogger(UserServiceImpl.class.getCanonicalName()).info("Logged in user: " + eMail);
	User match = activeUsers.get(eMail);
	if ( match != null ) {
		return match; 
	} else {
		match = new UserImpl(eMail, name, auth.getAuthorities(), datastore);
		activeUsers.put(eMail, match);
		return match;
	}
  }

  @Override
  public boolean isUserLoggedIn() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return !isAnonymousUser(auth);
  }

  @Override
  public User getDaemonAccountUser() {
	return daemonAccount;
  }

}
