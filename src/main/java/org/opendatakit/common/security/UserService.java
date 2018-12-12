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

import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;


/**
 * Minimal service for accessing information about the current
 * user and the calling context.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public interface UserService {

  String createLogoutURL();

  Realm getCurrentRealm();

  User getCurrentUser();

  User getDaemonAccountUser();

  boolean isAccessManagementConfigured();

  void reloadPermissions();

  boolean isUserLoggedIn();

  String getSuperUserEmail();

  String getSuperUserUsername();

  boolean isSuperUser(CallingContext cc) throws ODKDatastoreException;

  boolean isSuperUsernamePasswordSet(CallingContext cc) throws ODKDatastoreException;

}
