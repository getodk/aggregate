package org.opendatakit.common.security.spring;

import java.util.Set;
import java.util.UUID;

import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService, InitializingBean {

	private static final String AT_SIGN = "@";
	private static final String MAILTO_COLON = "mailto:";

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
		if ( !name.startsWith(MAILTO_COLON) && !name.contains(AT_SIGN)) {
			throw new IllegalStateException("Expecting name to be resolved to " + MAILTO_COLON + "user@domain");			
		}
		
		try {
			User user = userService.getDaemonAccountUser();
			RegisteredUsersTable relation;
			relation = RegisteredUsersTable.assertRelation(datastore, user);
			RegisteredUsersTable t = datastore.getEntity(relation, name, user);
			String uriUser = t.getUri();
			String mailtoDomain = uriUser.substring(uriUser.lastIndexOf(AT_SIGN)+1);
			Set<GrantedAuthority> authorities = 
				UserGrantedAuthority.getGrantedAuthorities(uriUser, datastore, user);
			authorities.add(new GrantedAuthorityImpl(GrantedAuthorityNames.USER_IS_REGISTERED.name()));
			
			String password;
			switch ( passwordType ) {
			case BasicAuth:
				password = t.getBasicAuthPassword();
				break;
			case DigestAuth:
				password = t.getDigestAuthPassword();
				break;
			default:
			case Random:
				password = UUID.randomUUID().toString();
				break;
			}
			return new AggregateUser(uriUser, password, mailtoDomain,
					t.getIsEnabled(),
					t.getIsAccountNonExpired(), t.getIsCredentialNonExpired(),
					t.getIsAccountNonLocked(),
					authorities);
		} catch (ODKEntityNotFoundException e) {
			throw new UsernameNotFoundException("User " + name + " is not registered", e);
		} catch (ODKDatastoreException e) {
			throw new TransientDataAccessResourceException("persistence layer problem", e);
		}
	}

}
