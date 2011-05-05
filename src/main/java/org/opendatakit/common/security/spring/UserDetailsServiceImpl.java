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

import java.util.Set;
import java.util.UUID;

import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
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
	
	private Datastore datastore;
	private UserService userService;
	private PasswordType passwordType = PasswordType.Random; 
	
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

	@Override
	public UserDetails loadUserByUsername(String name)
			throws UsernameNotFoundException, DataAccessException {
		if ( !name.startsWith(SecurityUtils.MAILTO_COLON) || !name.contains(SecurityUtils.AT_SIGN)) {
			throw new IllegalStateException("Expecting name to be resolved to " + SecurityUtils.MAILTO_COLON + "user@domain");			
		}
		
		try {
			User user = userService.getDaemonAccountUser();
			RegisteredUsersTable relation;
			relation = RegisteredUsersTable.assertRelation(datastore, user);
			RegisteredUsersTable t = datastore.getEntity(relation, name, user);
			String uriUser = t.getUri();
			String mailtoDomain = SecurityUtils.getMailtoDomain(uriUser);
			if ( mailtoDomain == null ) mailtoDomain = "-undefined-";
			Set<GrantedAuthority> authorities = 
				UserGrantedAuthority.getGrantedAuthorities(uriUser, datastore, user);
			authorities.add(new GrantedAuthorityImpl(GrantedAuthorityNames.USER_IS_REGISTERED.name()));
			
			String password;
			String salt;
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
			case Random:
				password = UUID.randomUUID().toString();
				salt = UUID.randomUUID().toString();
				break;
			}
			if ( password == null ) {
				throw new AuthenticationCredentialsNotFoundException(
						"User " + name + " does not have a password configured. You must close and re-open your browser to clear this error.");
			}
			
			return new AggregateUser(uriUser, password, salt, mailtoDomain,
					t.getIsEnabled(),
					true, t.getIsCredentialNonExpired(),
					true,
					authorities);
		} catch (ODKEntityNotFoundException e) {
			throw new UsernameNotFoundException("User " + name + " is not registered", e);
		} catch (ODKDatastoreException e) {
			throw new TransientDataAccessResourceException("persistence layer problem", e);
		}
	}

}
