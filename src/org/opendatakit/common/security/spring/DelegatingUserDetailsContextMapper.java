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

//package org.opendatakit.common.security.spring;
//
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.springframework.ldap.core.DirContextAdapter;
//import org.springframework.ldap.core.DirContextOperations;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.GrantedAuthorityImpl;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
//
//public class DelegatingUserDetailsContextMapper implements UserDetailsContextMapper {
//
//	private final UserDetailsContextMapper userDetailsContextMapper;
//	
//	public UserDetails mapUserFromContext(DirContextOperations ctxt,
//			String username, Collection<GrantedAuthority> authority) {
//		UserDetails detail = userDetailsContextMapper.mapUserFromContext(ctxt, username, authority);
//		Set<GrantedAuthority> full_authorities = new HashSet<GrantedAuthority>();
//		full_authorities.addAll(authorities);
//		full_authorities.addAll(detail.getAuthorities());
//		return new AggregateUser(detail.getUsername(), detail.getPassword(), mailtoDomain,
//								detail.isEnabled(), detail.isAccountNonExpired(),
//								detail.isCredentialsNonExpired(), detail.isAccountNonLocked(),
//								full_authorities);
//	}
//
//	public void mapUserToContext(UserDetails user, DirContextAdapter ctxt) {
//		userDetailsContextMapper.mapUserToContext(user, ctxt);
//	}
//
//	private final String mailtoDomain;
//	private final Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
//
//	DelegatingUserDetailsContextMapper(UserDetailsContextMapper userDetailsContextMapper,
//			String mailtoDomain, List<String> authorities) {
//		this.userDetailsContextMapper = userDetailsContextMapper;
//		this.mailtoDomain = mailtoDomain;
//		for ( String a : authorities ) {
//			this.authorities.add(new GrantedAuthorityImpl(a));
//		}
//		String mungedMailtoDomain = "MAILTO_" + mailtoDomain.replaceAll("[^\\p{Digit}\\p{javaUpperCase}\\p{javaLowerCase}]", "_");
//		this.authorities.add(new GrantedAuthorityImpl(mungedMailtoDomain.toUpperCase()));
//		this.authorities.add(new GrantedAuthorityImpl("USER_IS_AUTHENTICATED"));
//	}
//
//}
