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

	private final String salt;
	
	private final String mailtoDomain;
	
	public String getMailtoDomain() {
		return mailtoDomain;
	}
	
	public String getSalt() {
		return salt;
	}

	public AggregateUser(String username, String password, String salt,
			String mailToDomain,
			boolean enabled,
			boolean accountNonExpired, boolean credentialsNonExpired,
			boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired,
				accountNonLocked, authorities);
		this.salt = salt;
		this.mailtoDomain = mailToDomain;
	}

}
