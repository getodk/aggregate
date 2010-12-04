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
package org.opendatakit.common.security.tomcat;

import java.util.ArrayList;
import java.util.List;

// TODO: implement
public class UserImpl implements org.opendatakit.common.security.User {

	final String nickName;
	final String passwordTreatment;
	final String realmString;
	final String uriUser;
	final List<String> groups = new ArrayList<String>();
	
	
	UserImpl(String uriUser, String realmString, String nickName,
			 String passwordTreatment, List<String> groups) {
		this.uriUser = uriUser;
		this.realmString = realmString;
		this.nickName = nickName;
		this.passwordTreatment = passwordTreatment;
		if ( groups != null ) {
			this.groups.addAll(groups);
		}
	}
	
	@Override
	public String getNickname() {
		return nickName;
	}

	@Override
	public List<String> getGroups() {
		return groups;
	}

	@Override
	public String getPasswordTreatment() {
		return passwordTreatment;
	}

	@Override
	public String getRealmString() {
		return realmString;
	}

	@Override
	public String getUriUser() {
		return uriUser;
	}
}
