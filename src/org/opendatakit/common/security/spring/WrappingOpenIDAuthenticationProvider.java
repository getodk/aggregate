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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationProvider;
import org.springframework.security.openid.OpenIDAuthenticationToken;

/**
 * Ensures that the UserDetails returned from the openID authentication process
 * have a username that is the e-mail address of the user that was authenticated
 * and that the granted authorities include the AUTH_OPENID and MAILTO_... 
 * authorities.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class WrappingOpenIDAuthenticationProvider extends OpenIDAuthenticationProvider {

	UserDetailsService wrappingUserDetailsService;

	public UserDetailsService getWrappingUserDetailsService() {
		return wrappingUserDetailsService;
	}

	public void setWrappingUserDetailsService(
			UserDetailsService wrappingUserDetailsService) {
		this.wrappingUserDetailsService = wrappingUserDetailsService;
	}

	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();

		if ( wrappingUserDetailsService == null ) {
			throw new IllegalStateException("wrappingUserDetailsService must be defined");
		}
	}
	
	@Override
	protected Authentication createSuccessfulAuthentication(
			UserDetails rawUserDetails, OpenIDAuthenticationToken auth) {
		String eMail = null;
		List<OpenIDAttribute> oAttrList = auth.getAttributes();
		for ( OpenIDAttribute oAttr : oAttrList ) {
			if ( "email".equals(oAttr.getName()) ) {
				Object o = oAttr.getValues().get(0);
				if ( o != null ) {
					eMail= (String) o;
				}
			}
		}
		if ( eMail == null ) {
			throw new UsernameNotFoundException("email address not supplied in OpenID attributes");
		}
		eMail = SecurityUtils.normalizeUsername(eMail, null);
		String mailtoDomain = SecurityUtils.getMailtoDomain(eMail);
		
		AggregateUser userDetails = (AggregateUser) rawUserDetails;
		
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		
		authorities.addAll(userDetails.getAuthorities());
		// add the AUTH_OPENID granted authority and the MAILTO_... granted authority
		authorities.add(new GrantedAuthorityImpl("AUTH_OPENID"));
		String mailtoAuthority = GrantedAuthorityNames.getMailtoGrantedAuthorityName(eMail);
		if ( mailtoAuthority != null ) {
			authorities.add(new GrantedAuthorityImpl(mailtoAuthority));
		}

		AggregateUser partialDetails = null;
		try {
			partialDetails = (AggregateUser) wrappingUserDetailsService.loadUserByUsername(eMail);
			authorities.addAll(partialDetails.getAuthorities());
		} catch (Exception e) {
			partialDetails = userDetails;
		}

		AggregateUser trueUser = new AggregateUser(eMail, partialDetails.getPassword(),
													UUID.randomUUID().toString(), // junk...
													mailtoDomain,
													partialDetails.isEnabled(),
													partialDetails.isAccountNonExpired(),
													partialDetails.isCredentialsNonExpired(),
													partialDetails.isAccountNonLocked(),
													authorities);
		if ( !( trueUser.isEnabled() && trueUser.isAccountNonExpired() &&
				trueUser.isAccountNonLocked() ) ) {
			throw new UsernameNotFoundException("account is blocked");
		}
		
        return new OpenIDAuthenticationToken(trueUser, trueUser.getAuthorities(),
                 auth.getIdentityUrl(), auth.getAttributes());
    }

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		return super.authenticate(authentication);
	}
}
