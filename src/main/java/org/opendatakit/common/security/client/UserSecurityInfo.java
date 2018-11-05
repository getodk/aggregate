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
import java.util.TreeSet;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.security.common.GrantedAuthorityName;


/**
 * Heavy object comparable to User that is transportable across the GWT interfaces.
 * If requested, it provides the full set of authorizations and group memberships that
 * a user possesses (so that the client can show or hide tabs as appropriate).  The
 * username is a unique key for this object.
 *
 * @author mitchellsundt@gmail.com
 */
public class UserSecurityInfo implements Comparable<UserSecurityInfo>, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 758102181896882604L;
  private static final String UID_PREFIX = "uid:";

  ;
  String username; // null if email is non-null
  String fullname; // tie-back to whatever the site admin wants to know.
  String email; // null if username is non-null
  UserType type;
  TreeSet<GrantedAuthorityName> assignedUserGroups = new TreeSet<GrantedAuthorityName>();
  TreeSet<GrantedAuthorityName> grantedAuthorities = new TreeSet<GrantedAuthorityName>();

  public UserSecurityInfo() {
  }

  public UserSecurityInfo(String username, String fullname, String email, UserType type) {
    this.username = username;
    this.fullname = fullname;
    this.email = email;
    this.type = type;
    if ((email != null && username != null) || (email == null && username == null)) {
      throw new IllegalArgumentException("must have either just username or just email non-null");
    }
  }

  /**
   * Simple converter of user's primary key to a display name.
   * The actual user record at the time of the storage is identified by the
   * uriUser.  The user's display name is encoded in that as well as a
   * creation timestamp.
   *
   * @param uriUser
   * @return
   */
  public static final String getDisplayName(String uriUser) {
    String displayName;
    if (uriUser.startsWith(EmailParser.K_MAILTO)) {
      displayName = uriUser.substring(EmailParser.K_MAILTO.length());
    } else if (uriUser.startsWith(UID_PREFIX)) {
      displayName = uriUser.substring(UID_PREFIX.length(), uriUser.indexOf("|"));
    } else {
      displayName = uriUser;
    }
    return displayName;
  }

  public UserType getType() {
    return type;
  }

  public void setType(UserType type) {
    this.type = type;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFullName() {
    return fullname;
  }

  public void setFullName(String fullname) {
    this.fullname = fullname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getCanonicalName() {
    if (username != null) {
      return username;
    } else {
      return email.substring(EmailParser.K_MAILTO.length());
    }
  }

  public TreeSet<GrantedAuthorityName> getAssignedUserGroups() {
    return assignedUserGroups;
  }

  public void setAssignedUserGroups(TreeSet<GrantedAuthorityName> authorities) {
    assignedUserGroups.clear();
    if (authorities != null) {
      assignedUserGroups.addAll(authorities);
    }
  }

  public TreeSet<GrantedAuthorityName> getGrantedAuthorities() {
    return grantedAuthorities;
  }

  public void setGrantedAuthorities(TreeSet<GrantedAuthorityName> authorities) {
    grantedAuthorities.clear();
    if (authorities != null) {
      grantedAuthorities.addAll(authorities);
    }
  }

  @Override
  public boolean equals(Object obj) {
    UserSecurityInfo info = (UserSecurityInfo) obj;
    if (username != null && info.username != null) {
      return username.equals(info.username);
    }
    if (email != null && info.email != null) {
      return email.equals(info.email);
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (username != null) {
      return 1010101 + 3 * username.hashCode();
    } else if (email != null) {
      return email.hashCode();
    } else {
      return 3;
    }
  }

  @Override
  public int compareTo(UserSecurityInfo o) {
    if (username != null && o.username != null) {
      return username.compareTo(o.username);
    }
    if (email != null && o.email != null) {
      return email.compareTo(o.email);
    }
    if (username != null) return -1;
    return 1;
  }

  public enum UserType implements Serializable {
    ANONYMOUS,     // not authenticated (anonymous)
    REGISTERED,    // authenticated and registered
    AUTHENTICATED;  // external service authentication but not registered

    private UserType() {
      // GWT
    }
  }
}
