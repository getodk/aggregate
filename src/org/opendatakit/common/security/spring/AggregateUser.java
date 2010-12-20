package org.opendatakit.common.security.spring;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Extension of default User to add mailToDomain for help in 
 * constructing the eMail address of the user (which is the 
 * uriUser).
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class AggregateUser extends User {

	private static final long serialVersionUID = 7141261836301276256L;

	private final String mailtoDomain;
	
	public String getMailtoDomain() {
		return mailtoDomain;
	}

	public AggregateUser(String username, String password, String mailToDomain,
			boolean enabled,
			boolean accountNonExpired, boolean credentialsNonExpired,
			boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired,
				accountNonLocked, authorities);
		this.mailtoDomain = mailToDomain;
	}

}
