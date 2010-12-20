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

import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;
import org.springframework.beans.factory.InitializingBean;

//TODO: implement
public class UserServiceImpl implements org.opendatakit.common.security.UserService, InitializingBean {
	
	Realm realm;
	User anonymous;
	User daemonAccount;
	
	UserServiceImpl() {
		List<String> domains = new ArrayList<String>();
		domains.add("aggregate.test.org");
		domains.add("test.net");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if ( realm == null ) {
			throw new IllegalStateException("realm must be set");
		}
		anonymous = new UserImpl(User.ANONYMOUS_USER, realm.getRealmString(), 
				User.ANONYMOUS_USER_NICKNAME, null, null );
		daemonAccount = new UserImpl(User.DAEMON_USER, realm.getRealmString(), 
				User.DAEMON_USER_NICKNAME, null, null );
	}

	public Realm getRealm() {
		return realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
	}

	@Override
  public String createLoginURL(String destinationURL) {
    return destinationURL;
  }

  @Override
  public String createLogoutURL(String destinationURL) {
    return destinationURL;
  }

  @Override
  public Realm getCurrentRealm() {
    return realm;
  }

  @Override
  public User getCurrentUser() {
    return anonymous;
  }

  @Override
  public boolean isUserLoggedIn() {
    return true;
  }

@Override
public User getDaemonAccountUser() {
	return daemonAccount;
}

}
