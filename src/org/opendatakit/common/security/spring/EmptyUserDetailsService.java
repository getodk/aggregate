package org.opendatakit.common.security.spring;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * This is a trivial stub service that returns a User object with the 
 * supplied login.  When wrapped by the DelegatingUserDetailsService,
 * it is used by the OpenIDAuthenticationProvider for OpenID identities.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class EmptyUserDetailsService implements UserDetailsService {

	public EmptyUserDetailsService() {
	}

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		UserDetails user = new User(username, UUID.randomUUID().toString(), 
							true, true, true, true, new ArrayList<GrantedAuthority>() );
		return user;
	}

}
