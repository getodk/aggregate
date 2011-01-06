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
package org.opendatakit.common.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helpful utilities used by presentation layer and security services layers.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public final class SecurityUtils {
	public static final String MAILTO_COLON = "mailto:";
	public static final String AT_SIGN = "@";

	private SecurityUtils() {};
	
	public static final String normalizeUsername(String username, String mailtoDomain) {
		String mailtoUsername = username;
		if ( !username.startsWith(MAILTO_COLON) ) {
			if ( username.contains(AT_SIGN) ) {
				mailtoUsername = MAILTO_COLON + username;
			} else {
				if ( mailtoDomain == null ) {
					throw new IllegalStateException("e-mail address is incomplete - it does not specify a domain!");
				}
				mailtoUsername = MAILTO_COLON + username + AT_SIGN + mailtoDomain;
			}
		}
		return mailtoUsername;
	}
	
	public static final String getEmailAddress( String uriUser ) {
		String name = uriUser;
		if ( name != null && name.startsWith(MAILTO_COLON) ) {
			name = name.substring(MAILTO_COLON.length());
		}
		return name;
	}
	
	public static final String getMailtoDomain( String uriUser ) {
		if ( uriUser == null ||
			 !uriUser.startsWith(MAILTO_COLON) ||
			 !uriUser.contains(AT_SIGN) )
			return null;
		return uriUser.substring(uriUser.indexOf(AT_SIGN)+1);
	}
	
	public static final String getNickname( String uriUser ) {
		String name = uriUser;
		if ( name != null && name.contains(AT_SIGN) ) {
			name = name.substring(0,name.indexOf(AT_SIGN));
			if ( name.startsWith(MAILTO_COLON) ) {
				name = name.substring(MAILTO_COLON.length());
			}
		}
		return name;
	}
	
	public static final String getDigestAuthenticationPasswordHash(String uriUser, String password, Realm realm) {
		String fullDigestAuth = uriUser.substring(MAILTO_COLON.length()) + ":" + 
								realm.getRealmString() + ":" + password;
	    try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] asBytes = fullDigestAuth.getBytes();
			md.update(asBytes);
			
	        byte[] messageDigest = md.digest();
	
	        BigInteger number = new BigInteger(1, messageDigest);
	        String md5 = number.toString(16);
	        while (md5.length() < 32)
	            md5 = "0" + md5;
	        return md5;
	    } catch (NoSuchAlgorithmException e) {
	    	throw new IllegalStateException("Unexpected problem computing md5 hash", e);
		}
	}
}
