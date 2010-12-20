package org.opendatakit.common.security.spring;

import java.util.Collection;

import org.opendatakit.common.security.UserService;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;

public class SpringAccessDecisionManager implements AccessDecisionManager {

	final UserService userService;
	
	SpringAccessDecisionManager(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public void decide(Authentication authentication, Object secureObject,
			Collection<ConfigAttribute> config) throws AccessDeniedException,
			InsufficientAuthenticationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean supports(ConfigAttribute attribute) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

}
