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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Implementation of a user details service that fetches data from the 
 * {@link RegisteredUsersTable} to report on registered users.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class UserDetailsServiceImpl implements UserDetailsService, InitializingBean {

	enum PasswordType {
		BasicAuth,
		DigestAuth,
		Random
	};
	
	enum CredentialType {
		Username,
		Email,
		Token // Out-of-band (Oauth) or Oauth2 token
	};
	
	private Datastore datastore;
	private UserService userService;
	private PasswordType passwordType = PasswordType.Random;
	private CredentialType credentialType = CredentialType.Username;
	private Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

	UserDetailsServiceImpl() {
	}

	public Datastore getDatastore() {
		return datastore;
	}

	public void setDatastore(Datastore datastore) {
		this.datastore = datastore;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	public void setPasswordType(String type) {
		if ( PasswordType.BasicAuth.name().equals(type) ) {
			passwordType = PasswordType.BasicAuth;
		} else if ( PasswordType.DigestAuth.name().equals(type)) {
			passwordType = PasswordType.DigestAuth;
		} else if ( PasswordType.Random.name().equals(type)) {
			passwordType = PasswordType.Random;
		} else {
			throw new IllegalArgumentException("Unrecognized PasswordType");
		}
	}
	
	public void setCredentialType(String type) {
		if ( CredentialType.Username.name().equals(type) ) {
			credentialType = CredentialType.Username;
		} else if ( CredentialType.Email.name().equals(type)) {
			credentialType = CredentialType.Email;
      } else if ( CredentialType.Token.name().equals(type)) {
        credentialType = CredentialType.Token;
		} else {
			throw new IllegalArgumentException("Unrecognized CredentialType");
		}
	}
	
	public void setAuthorities(List<SimpleGrantedAuthority> authorities) {
		this.authorities.clear();
		for ( SimpleGrantedAuthority a : authorities ) {
			this.authorities.add(a);
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if ( datastore == null ) {
			throw new IllegalStateException("datastore must be specified");
		}
		if ( userService == null ) {
			throw new IllegalStateException("userService must be specified");
		}
	}

	private Set<GrantedAuthority> getGrantedAuthorities(String uriUser ) throws ODKDatastoreException {
		User user = userService.getDaemonAccountUser();
		Set<GrantedAuthority> grantedAuthorities = 
			UserGrantedAuthority.getGrantedAuthorities(uriUser, datastore, user);
		grantedAuthorities.add(new SimpleGrantedAuthority(GrantedAuthorityName.USER_IS_REGISTERED.name()));
		grantedAuthorities.addAll(authorities);
		return grantedAuthorities;
	}
		
	@Override
	public UserDetails loadUserByUsername(String name)
			throws UsernameNotFoundException, DataAccessException {
		if ( name == null ) {
			throw new IllegalStateException("Username cannot be null");			
		}
		
		User user = userService.getDaemonAccountUser();
		
		final String uriUser;
		final String password;
		final String salt;
		final Set<GrantedAuthority> grantedAuthorities;
		final boolean isEnabled = true;
		final boolean isCredentialNonExpired = true;
		try {
			if ( credentialType == CredentialType.Username ) {
				RegisteredUsersTable t;
				// first call from digest, basic or forms-based auth
				if ( name.startsWith(RegisteredUsersTable.UID_PREFIX) ) {
					t = RegisteredUsersTable.getUserByUri(name, datastore, user);
					if ( t == null ) {
						throw new UsernameNotFoundException("UID " + name +	" is not recognized.");
					}
				} else {
					t = RegisteredUsersTable.getUniqueUserByUsername(name, datastore, user);
					if ( t == null ) {
						throw new UsernameNotFoundException("User " + name + 
									" is not registered or the registered users table is corrupt.");
					}
				}
				uriUser = t.getUri();
				switch ( passwordType ) {
				case BasicAuth:
					password = t.getBasicAuthPassword();
					salt = t.getBasicAuthSalt();
					break;
				case DigestAuth:
					password = t.getDigestAuthPassword();
					salt = UUID.randomUUID().toString();
					break;
				default:
					throw new AuthenticationCredentialsNotFoundException(
							"Password type " + passwordType.toString() + " cannot be interpretted");
				}
				
				grantedAuthorities = getGrantedAuthorities(t.getUri());
				if ( password == null ) {
					throw new AuthenticationCredentialsNotFoundException(
							"User " + name + " does not have a password configured. You must close and re-open your browser to clear this error.");
				}
			} else {
				// OAuth2 token...
				// there is no password for an OAuth2 credential
				if ( passwordType != PasswordType.Random ) {
					throw new AuthenticationCredentialsNotFoundException(
							"Password type " + passwordType.toString() + " cannot be interpretted");
				}
				// set password and salt to unguessable strings...
				password = UUID.randomUUID().toString();
				salt = UUID.randomUUID().toString();
				
				// try to find user in registered users table...
				RegisteredUsersTable eUser = RegisteredUsersTable.getUniqueUserByEmail(name, datastore, user);
				if ( eUser != null ) {
					uriUser = eUser.getUri();
					grantedAuthorities = getGrantedAuthorities(eUser.getUri());
				} else {
					throw new UsernameNotFoundException("User " + name + " is not registered");
				}
			}
		} catch (ODKDatastoreException e) {
			throw new TransientDataAccessResourceException("persistence layer problem", e);
		}
			
		return new AggregateUser(uriUser, password, salt, "-undefined-",
				isEnabled, true, isCredentialNonExpired, true, grantedAuthorities );
	}
}
