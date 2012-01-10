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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.SecurityBeanDefs;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.web.CallingContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserServiceImpl implements org.opendatakit.common.security.UserService, InitializingBean {

	private static final Log logger = LogFactory.getLog(UserServiceImpl.class);
	
	// configured by bean definition...
	Datastore datastore;
	Realm realm;
	String superUserEmail;
	String superUserUsername;
	String userServiceKey;
	
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
		if ( superUserEmail == null ) {
			throw new IllegalStateException("superUserEmail must be configured");
		}
		if ( !superUserEmail.startsWith(SecurityUtils.MAILTO_COLON) || !superUserEmail.contains(SecurityUtils.AT_SIGN)) {
			throw new IllegalStateException("superUserEmail is malformed. " +
					"Must be of the form 'mailto:user@gmail.com' or other supported OpenID provider.");
		}
		if ( userServiceKey == null ) {
			throw new IllegalStateException("userServiceKey must be configured");
		}
		Log log = LogFactory.getLog(UserServiceImpl.class);
		log.info("superUserEmail: " + superUserEmail);
		log.info("superUserUsername: " + superUserUsername);

		reloadPermissions();
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

	public String getSuperUserEmail() {
		return superUserEmail;
	}
	
	public void setSuperUserEmail(String superUserEmail) {
		this.superUserEmail = superUserEmail;
	}

	public String getSuperUserUsername() {
		return superUserUsername;
	}

	public void setSuperUserUsername(String superUserUsername) {
		this.superUserUsername = superUserUsername;
	}
	
	@Override
	public synchronized boolean isSuperUser(CallingContext cc) throws ODKDatastoreException {
		MessageDigestPasswordEncoder mde = null;
		try {
			Object obj =  cc.getBean(SecurityBeanDefs.BASIC_AUTH_PASSWORD_ENCODER);
			if ( obj != null ) {
				mde = (MessageDigestPasswordEncoder) obj;
			}
		} catch ( Exception e ) {
			mde = null;
		}
		List<RegisteredUsersTable> tList = RegisteredUsersTable.assertSuperUsers(this, mde, datastore);
		
		String uriUser = cc.getCurrentUser().getUriUser();
		for ( RegisteredUsersTable t : tList ) {
			if ( t.getUri().equals(uriUser) ) return true;
		}
		return false;
	}
	
	@Override
	public String getUserServiceKey() {
		return userServiceKey;
	}
	
	public void setUserServiceKey(String userServiceKey) {
		this.userServiceKey = userServiceKey;
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
	logger.info("Executing: reloadPermissions");
	activeUsers.clear();
  }
  
  @Override
  public boolean isAccessManagementConfigured() {
	  try {
		  /**
		   * Any configuration in the GrantedAuthorityHierarchy table indicates that 
		   * we have configured access management with at least a default configuration.
		   */
	      GrantedAuthorityHierarchyTable relation = GrantedAuthorityHierarchyTable.assertRelation(datastore, getDaemonAccountUser());
	      Query query = datastore.createQuery(relation, "UserServiceImpl.isAccessManagementConfigured", getDaemonAccountUser());
	      List<?> values = query.executeQuery();
	      return !values.isEmpty();
	  } catch ( ODKDatastoreException e ) {
		  e.printStackTrace();
		  // The persistence layer is having problems.  
		  // Allow the 'normal control path' to deal with it.
		  return true;
	  }
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

  private synchronized User internalGetUser(String uriUser, Collection<? extends GrantedAuthority> authorities) {
	User match = activeUsers.get(uriUser);
	if ( match != null ) {
		return match; 
	} else if ( User.ANONYMOUS_USER.equals(uriUser) ) {
		// ignored passed-in authorities
		Set<GrantedAuthority> anonGroups = new HashSet<GrantedAuthority>();
		anonGroups.add(new SimpleGrantedAuthority(GrantedAuthorityName.USER_IS_ANONYMOUS.name()));
		match = new UserImpl(User.ANONYMOUS_USER, 
				User.ANONYMOUS_USER_NICKNAME, anonGroups, datastore );
		activeUsers.put(uriUser, match);
		return match;
	} else if ( User.DAEMON_USER.equals(uriUser) ) {
		// ignored passed-in authorities
		Set<GrantedAuthority> daemonGroups = new HashSet<GrantedAuthority>();
		daemonGroups = new HashSet<GrantedAuthority>();
		daemonGroups.add(new SimpleGrantedAuthority(GrantedAuthorityName.USER_IS_DAEMON.name()));
		match = new UserImpl(User.DAEMON_USER, 
				User.DAEMON_USER_NICKNAME, daemonGroups, datastore );
		activeUsers.put(uriUser, match);
		return match;
	} else {
		try {
			RegisteredUsersTable t = RegisteredUsersTable.getUserByUri(uriUser, datastore, getDaemonAccountUser());
			match = new UserImpl( uriUser, t.getDisplayName(), authorities, datastore);
		} catch (ODKEntityNotFoundException e) {
			String name = UserServiceImpl.getNickname(uriUser);
			match = new UserImpl(uriUser, name, authorities, datastore);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			// best guess...
			String name = "--datastore error--";
			match = new UserImpl(uriUser, name, authorities, datastore);
		}
		activeUsers.put(uriUser, match);
		return match;
	}
  }
  
  @Override
  public User getCurrentUser() {
	Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	return internalGetUser(auth.getName(), auth.getAuthorities());
  }

  @Override
  public boolean isUserLoggedIn() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return !isAnonymousUser(auth);
  }

  @Override
  public User getDaemonAccountUser() {
	return internalGetUser(User.DAEMON_USER, null);
  }

public static final String getNickname( String uriUser ) {
	String name = uriUser;
	if ( name.startsWith(SecurityUtils.MAILTO_COLON) ) {
		name = name.substring(SecurityUtils.MAILTO_COLON.length());
	}
	return name;
}

}
