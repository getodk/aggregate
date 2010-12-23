package org.opendatakit.common.security.spring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class DelegatingUserDetailsService  implements UserDetailsService, InitializingBean {
	
	private static final String AT_SIGN = "@";
	private static final String MAILTO_COLON = "mailto:";
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
		
		String mungedMailtoDomain = "MAILTO_" + mailtoDomain.replaceAll("[^\\p{Digit}\\p{javaUpperCase}\\p{javaLowerCase}]", "_");
		this.authorities.add(new GrantedAuthorityImpl(mungedMailtoDomain.toUpperCase()));
		this.authorities.add(new GrantedAuthorityImpl("USER_IS_AUTHENTICATED"));
	}
	
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		String passedUsername = username;
		if ( passDownUriUser ) {
			if ( !username.startsWith(MAILTO_COLON) ) {
				if ( username.contains(AT_SIGN) ) {
					passedUsername = MAILTO_COLON + username;
				} else {
					passedUsername = MAILTO_COLON + username + AT_SIGN + mailtoDomain;
				}
			}
		}
		
		UserDetails detail = userDetailsService.loadUserByUsername(passedUsername);
		Set<GrantedAuthority> full_authorities = new HashSet<GrantedAuthority>();
		full_authorities.addAll(authorities);
		full_authorities.addAll(detail.getAuthorities());
		return new AggregateUser(detail.getUsername(), detail.getPassword(), mailtoDomain,
								detail.isEnabled(), detail.isAccountNonExpired(),
								detail.isCredentialsNonExpired(), detail.isAccountNonLocked(),
								full_authorities);
	}
}
