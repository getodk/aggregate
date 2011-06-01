/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.common.security.client.security.admin;

import java.util.ArrayList;

import org.opendatakit.common.security.client.CredentialsInfo;
import org.opendatakit.common.security.client.GrantedAuthorityInfo;
import org.opendatakit.common.security.client.UserClassSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SecurityAdminServiceAsync {

	void getAllUsers(boolean withAuthorities, AsyncCallback<ArrayList<UserSecurityInfo>> callback);

	void getUsers(GrantedAuthorityInfo auth,
			AsyncCallback<ArrayList<UserSecurityInfo>> callback);

	void setUsersAndGrantedAuthorities(String xsrfString,
			ArrayList<UserSecurityInfo> users,
			ArrayList<GrantedAuthorityInfo> anonGrants,
			ArrayList<GrantedAuthorityInfo> allGroups,
			AsyncCallback<Void> callback);

	void isSimpleConfig(AsyncCallback<Boolean> callback);

	void setUserPasswords(String xsrfString,
			ArrayList<CredentialsInfo> credentials, AsyncCallback<Void> callback);

	void getUserClassPrivileges(String userClassName,
			AsyncCallback<UserClassSecurityInfo> callback);

}
