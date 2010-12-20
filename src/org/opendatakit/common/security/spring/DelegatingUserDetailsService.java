package org.opendatakit.common.security.spring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class DelegatingUserDetailsService  implements UserDetailsService {
	
	private final UserDetailsService userDetailsService;
	private final String mailtoDomain;
	private final Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
	
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		UserDetails detail = userDetailsService.loadUserByUsername(username);
		Set<GrantedAuthority> full_authorities = new HashSet<GrantedAuthority>();
		full_authorities.addAll(authorities);
		full_authorities.addAll(detail.getAuthorities());
		return new AggregateUser(detail.getUsername(), detail.getPassword(), mailtoDomain,
								detail.isEnabled(), detail.isAccountNonExpired(),
								detail.isCredentialsNonExpired(), detail.isAccountNonLocked(),
								full_authorities);
	}

	DelegatingUserDetailsService(UserDetailsService userDetailsService, String mailtoDomain, List<String> authorities) {
		this.userDetailsService = userDetailsService;
		this.mailtoDomain = mailtoDomain;
		for ( String a : authorities ) {
			this.authorities.add(new GrantedAuthorityImpl(a));
		}
		String mungedMailtoDomain = "MAILTO_" + mailtoDomain.replaceAll("[^\\p{Digit}\\p{javaUpperCase}\\p{javaLowerCase}]", "_");
		this.authorities.add(new GrantedAuthorityImpl(mungedMailtoDomain.toUpperCase()));
		this.authorities.add(new GrantedAuthorityImpl("ROLE_AUTHENTICATED"));
	}
}
