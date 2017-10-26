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
 *
 */
public interface UserService {

  public String createLoginURL();

  public String createLogoutURL();

  public Realm getCurrentRealm();

  public User getCurrentUser();

  public User getDaemonAccountUser();

  /**
   * Determine if the access management system has been configured.
   *
   * @return true if access management has been configured or if database unavailable
   */
  public boolean isAccessManagementConfigured();

  public void reloadPermissions();

  public boolean isUserLoggedIn();

  /**
   * @return the configured super user email address.
   */
  public String getSuperUserEmail();

  /**
   * @return the configured ODK Aggregate super-user username.
   */
  public String getSuperUserUsername();

  /**
   * @return true if this user is a superUser
   * @throws ODKDatastoreException
   */
  public boolean isSuperUser(CallingContext cc) throws ODKDatastoreException;

  /**
   * If the superUsername is defined, returns true if the 
   * password for that account is something other than 'aggregate'
   * 
   * @return true if superUsername account password is not 'aggregate'
   */
  public boolean isSuperUsernamePasswordSet(CallingContext cc) throws ODKDatastoreException;

}
