package org.opendatakit.common.security.spring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationProvider;
import org.springframework.security.openid.OpenIDAuthenticationToken;

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
		eMail = "mailto:" + eMail;
		
		AggregateUser userDetails = (AggregateUser) rawUserDetails;
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		authorities.addAll(userDetails.getAuthorities());

		AggregateUser partialDetails = null;
		try {
			partialDetails = (AggregateUser) wrappingUserDetailsService.loadUserByUsername(eMail);
			authorities.addAll(partialDetails.getAuthorities());
		} catch (Exception e) {
			partialDetails = userDetails;
		}

		AggregateUser trueUser = new AggregateUser(eMail, partialDetails.getPassword(),
													partialDetails.getMailtoDomain(),
													partialDetails.isEnabled(),
													partialDetails.isAccountNonExpired(),
													partialDetails.isCredentialsNonExpired(),
													partialDetails.isAccountNonLocked(),
													authorities);
		if ( !( trueUser.isEnabled() && trueUser.isAccountNonExpired() &&
				trueUser.isCredentialsNonExpired() && trueUser.isAccountNonLocked() ) ) {
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
