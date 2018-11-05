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

package org.opendatakit.common.security.client;

import java.io.Serializable;

/**
 * Information about the security configuration of the server.
 *
 * @author mitchellsundt@gmail.com
 */
public class RealmSecurityInfo implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -430735053365397828L;

  String basicAuthHashEncoding;
  String realmString;
  String superUserEmail;
  String superUsername;
  boolean isSuperUsernamePasswordSet;
  String changeUserPasswordURL;

  public RealmSecurityInfo() {
  }

  public String getBasicAuthHashEncoding() {
    return basicAuthHashEncoding;
  }

  public void setBasicAuthHashEncoding(String basicAuthHashEncoding) {
    this.basicAuthHashEncoding = basicAuthHashEncoding;
  }

  public String getSuperUserEmail() {
    return superUserEmail;
  }

  public void setSuperUserEmail(String superUserEmail) {
    this.superUserEmail = superUserEmail;
  }

  public String getSuperUsername() {
    return superUsername;
  }

  public void setSuperUsername(String superUsername) {
    this.superUsername = superUsername;
  }

  public boolean isSuperUsernamePasswordSet() {
    return isSuperUsernamePasswordSet;
  }

  public void setSuperUsernamePasswordSet(boolean isSuperUsernamePasswordSet) {
    this.isSuperUsernamePasswordSet = isSuperUsernamePasswordSet;
  }

  public String getRealmString() {
    return realmString;
  }

  public void setRealmString(String realmString) {
    this.realmString = realmString;
  }

  public String getChangeUserPasswordURL() {
    return changeUserPasswordURL;
  }

  public void setChangeUserPasswordURL(String changeUserPasswordURL) {
    this.changeUserPasswordURL = changeUserPasswordURL;
  }
}
