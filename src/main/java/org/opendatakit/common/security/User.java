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

/**
 * Minimal features of a user.  Note that the security permissions granted a
 * user are not defined here.  That is a security aspect that doesn't get
 * exposed to the application layer.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public interface User {
  String ANONYMOUS_USER = "anonymousUser";
  String ANONYMOUS_USER_NICKNAME = "Anonymous Access";

  String DAEMON_USER = "aggregate.opendatakit.org:web-service";
  String DAEMON_USER_NICKNAME = "Background Task Account";

  String getUriUser();

  boolean isAnonymous();

  boolean isRegistered();
}
