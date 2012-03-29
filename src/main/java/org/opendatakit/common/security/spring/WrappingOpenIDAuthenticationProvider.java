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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationProvider;
import org.springframework.security.openid.OpenIDAuthenticationToken;

/**
 * Ensures that the UserDetails returned from the openID authentication process
 * have a username that is the e-mail address of the user that was authenticated
 * and that the granted authorities include the AUTH_OPENID authority.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class WrappingOpenIDAuthenticationProvider extends OpenIDAuthenticationProvider {

  private static final Log logger = LogFactory.getLog(WrappingOpenIDAuthenticationProvider.class);

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
		   logger.warn("OpenId attributes did not include an e-mail address! ");
			throw new UsernameNotFoundException("email address not supplied in OpenID attributes");
		}
		eMail = WrappingOpenIDAuthenticationProvider.normalizeMailtoAddress(eMail);
		String mailtoDomain = WrappingOpenIDAuthenticationProvider.getMailtoDomain(eMail);
		
		UserDetails userDetails = rawUserDetails;
		
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		
		authorities.addAll(userDetails.getAuthorities());
		// add the AUTH_OPENID granted authority,
		authorities.add(new SimpleGrantedAuthority(GrantedAuthorityName.AUTH_OPENID.toString()));

		// attempt to look user up in registered users table...
		String username = null;
		UserDetails partialDetails = null;
		boolean noRights = false;
		try {
			partialDetails = wrappingUserDetailsService.loadUserByUsername(eMail);
			// found the user in the table -- fold in authorizations and get uriUser.
			authorities.addAll(partialDetails.getAuthorities());
			// users are blacklisted by registering them and giving them no rights.
			noRights = partialDetails.getAuthorities().isEmpty();
			username = partialDetails.getUsername();
		} catch (Exception e) {
		  e.printStackTrace();
		  logger.warn("OpenId attribute e-mail: " + eMail + " did not match any known e-mail addresses! " + e.getMessage());
        throw new UsernameNotFoundException("account not recognized");
		}

		AggregateUser trueUser = new AggregateUser(username, 
													partialDetails.getPassword(),
													UUID.randomUUID().toString(), // junk...
													mailtoDomain,
													partialDetails.isEnabled(),
													partialDetails.isAccountNonExpired(),
													partialDetails.isCredentialsNonExpired(),
													partialDetails.isAccountNonLocked(),
													authorities);
		if ( noRights || !( trueUser.isEnabled() && trueUser.isAccountNonExpired() &&
				trueUser.isAccountNonLocked() ) ) {
         logger.warn("OpenId attribute e-mail: " + eMail + " account is blocked! ");
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

	public static final String getMailtoDomain( String uriUser ) {
		if ( uriUser == null ||
			 !uriUser.startsWith(SecurityUtils.MAILTO_COLON) ||
			 !uriUser.contains(SecurityUtils.AT_SIGN) )
			return null;
		return uriUser.substring(uriUser.indexOf(SecurityUtils.AT_SIGN)+1);
	}

	public static final String normalizeMailtoAddress(String emailAddress) {
		String mailtoUsername = emailAddress;
		if ( !emailAddress.startsWith(SecurityUtils.MAILTO_COLON) ) {
			if ( emailAddress.contains(SecurityUtils.AT_SIGN) ) {
				mailtoUsername = SecurityUtils.MAILTO_COLON + emailAddress;
			} else {
			   logger.warn("OpenId attribute e-mail: " + emailAddress + " does not specify a domain! ");
	         throw new IllegalStateException("e-mail address is incomplete - it does not specify a domain!");
			}
		}
		return mailtoUsername;
	}
}
