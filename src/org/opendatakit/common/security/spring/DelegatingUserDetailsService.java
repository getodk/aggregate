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

import org.opendatakit.common.security.SecurityUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Thin wrapper that supplies the default mailto domain during login (for basic auth)
 * and adds the configured authorities and the MAILTO_... authority to the set of granted
 * authorities.
 *  
 * @author mitchellsundt@gmail.com
 *
 */
public class DelegatingUserDetailsService  implements UserDetailsService, InitializingBean {
	
	private UserDetailsService userDetailsService;
	private String mailtoDomain;
	private boolean passDownUriUser;
	private Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

	DelegatingUserDetailsService() {
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}
	
	public void setMailtoDomain(String mailtoDomain) {
		this.mailtoDomain = mailtoDomain;
	}
	
	public void setPassDownUriUser(boolean passDownUriUser) {
		this.passDownUriUser = passDownUriUser;
	}
	
	public void setAuthorities(List<String> authorities) {
		this.authorities.clear();
		for ( String a : authorities ) {
			this.authorities.add(new GrantedAuthorityImpl(a));
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if ( userDetailsService == null ) {
			throw new IllegalStateException("userDetailsService must be defined");
		}
		if ( mailtoDomain == null ) {
			throw new IllegalStateException("mailtoDomain must be defined");
		}
		this.authorities.add(new GrantedAuthorityImpl(
				GrantedAuthorityNames.USER_IS_AUTHENTICATED.toString()));
	}
	
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		String mailtoUsername = SecurityUtils.normalizeUsername(username, mailtoDomain);
		String passedUsername = passDownUriUser ? mailtoUsername : username;
		
		UserDetails detail = userDetailsService.loadUserByUsername(passedUsername);
		Set<GrantedAuthority> full_authorities = new HashSet<GrantedAuthority>();
		full_authorities.addAll(authorities);
		String mailtoAuthority = GrantedAuthorityNames.getMailtoGrantedAuthorityName(mailtoUsername);
		if ( mailtoAuthority != null ) {
			full_authorities.add(new GrantedAuthorityImpl(mailtoAuthority));
		}
		full_authorities.addAll(detail.getAuthorities());
		String salt = UUID.randomUUID().toString();
		if ( detail instanceof AggregateUser ) {
			salt = ((AggregateUser) detail).getSalt();
		}
		return new AggregateUser(detail.getUsername(), detail.getPassword(), salt, 
								SecurityUtils.getMailtoDomain(mailtoUsername),
								detail.isEnabled(), detail.isAccountNonExpired(),
								detail.isCredentialsNonExpired(), detail.isAccountNonLocked(),
								full_authorities);
	}
}
