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

import java.util.List;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public interface User {
	public static final String ANONYMOUS_USER = "mailto:anonymous";
	public static final String ANONYMOUS_USER_NICKNAME = "anonymous user";

	public static final String DAEMON_USER = "aggregate.opendatakit.org:web-service";
	public static final String DAEMON_USER_NICKNAME = "daemon account";
	/**
	 * @return User-friendly name.
	 */
	public String getNickname();
	
	/**
	 * @return user id of the form mailto:user@domain.com  or mailto:anonymous
	 */
	public String getUriUser();
	
	/**
	 * @return treatment to be communicated to password handler for authentication
	 */
	public String getPasswordTreatment();

	/**
	 * @return realm string displayed to user during login
	 */
	public String getRealmString();
	
	/**
	 * @return list of groups to which the user belongs
	 */
	public List<String> getGroups();
}
