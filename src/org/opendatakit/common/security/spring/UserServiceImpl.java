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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;

public class UserServiceImpl implements org.opendatakit.common.security.UserService, InitializingBean {

	// configured by bean definition...
	Datastore datastore;
	Realm realm;

	User anonymous;
	User daemonAccount;
	final Map<String, User> activeUsers = new HashMap<String, User>();
	static final String AnonymousGroup = "-AnonymousGroup-";
	static final String DaemonGroup = "-DaemonGroup-";
	
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
		anonGroups.add(new GrantedAuthorityImpl(AnonymousGroup));
		anonGroups.add(new GrantedAuthorityImpl("ROLE_ANONYMOUS"));
		anonymous = new UserImpl(User.ANONYMOUS_USER, realm.getRealmString(), 
				User.ANONYMOUS_USER_NICKNAME, null, anonGroups, datastore );
		Set<GrantedAuthority> daemonGroups = new HashSet<GrantedAuthority>();
		daemonGroups = new HashSet<GrantedAuthority>();
		daemonGroups.add(new GrantedAuthorityImpl(DaemonGroup));
		daemonAccount = new UserImpl(User.DAEMON_USER, realm.getRealmString(), 
				User.DAEMON_USER_NICKNAME, null, daemonGroups, datastore );
		
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
			// construct the e-mail...
			String eMail = null;
			Object p = auth.getPrincipal();
			if ( p == null ) {
				throw new IllegalStateException("Principal in Authentication is null -- is Spring AnonymousAuthenticationFilter configured?");
			}
			
			// expect all user principals to be AggregateUser types.
			AggregateUser principal = null;
			if (p instanceof AggregateUser) {
					principal = (AggregateUser) p;
			} else {
				throw new IllegalStateException("All UserDetails services should be wrapped by DelegatingUserDetailsService");
			}
			
			if ( auth instanceof OpenIDAuthenticationToken ) {
				List<OpenIDAttribute> oAttrList = ((OpenIDAuthenticationToken) auth).getAttributes();
				for ( OpenIDAttribute oAttr : oAttrList ) {
					if ( "email".equals(oAttr.getName()) ) {
						Object o = oAttr.getValues().get(0);
						if ( o != null ) {
							eMail= (String) o;
						}
					}
				}
				if ( eMail == null ) {
					throw new IllegalStateException("Email from OpenID Authentication is null -- is name 'email' configured to retrieve this OpenIDAttribute?");
				}
				
			} else if ( principal != null ) {
				eMail = principal.getUsername();
			}

			if ( !eMail.startsWith("mailto:") ) {
				if ( eMail.contains("@") ) {
					eMail = "mailto:" + eMail;
				} else {
					eMail = "mailto:" + eMail + "@" + principal.getMailtoDomain();
				}
			}
			return eMail;
		}
  }
  
  @Override
  public synchronized User getCurrentUser() {
	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	String eMail = getAuthenticationEmail(auth);

	String name = eMail;
	if ( name != null && name.contains("@") ) {
		name = name.substring(0,name.indexOf("@"));
		if ( name.startsWith("mailto:") ) {
			name = name.substring(7);
		}
	}

	Logger.getLogger(UserServiceImpl.class.getCanonicalName()).info("Logged in user: " + eMail);
	User match = activeUsers.get(eMail);
	if ( match != null ) {
		return match; 
	} else {
		Set<GrantedAuthority> userAuthorities = UserGrantedAuthority.getGrantedAuthorities(eMail, datastore, daemonAccount);
		userAuthorities.addAll(auth.getAuthorities());
		match = new UserImpl(eMail, name, name, "", userAuthorities, datastore);
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
