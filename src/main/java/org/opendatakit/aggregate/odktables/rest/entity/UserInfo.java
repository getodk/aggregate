/*
 * Copyright (C) 2017 University of Washington
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

package org.opendatakit.aggregate.odktables.rest.entity;

import java.util.ArrayList;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Describes a user who can manipulate odkTables appName content.
 * This user may not have synchronize permissions on the server
 * but may be able to verify identity and operate on the device.
 *
 * @author mitchellsundt@gmail.com
 *
 */
@JacksonXmlRootElement(localName="userInfo")
public class UserInfo {

  /**
   * user id (unique)
   */
  @JsonProperty(required = true)
  private String user_id;

  /**
   * display name of user (may not be unique)
   */
  @JsonProperty(required = true)
  private String full_name;

  /**
   * The privileges this user has.
   * Sorted.
   */
  @JsonProperty(required = true)
  @JacksonXmlElementWrapper(useWrapping=false)
  @JacksonXmlProperty(localName="roles")
  private ArrayList<String> roles;

  /**
   * Constructor used by Jackson
   */
  public UserInfo() {
    this.roles = new ArrayList<String>();
    this.user_id = null;
    this.full_name = null;
  }

  /**
   * Constructor used by our Java code
   *
   * @param entries
   */
  public UserInfo(String user_id, String full_name, ArrayList<String> roles) {
    this.user_id = user_id;
    this.full_name = full_name;
    if (roles == null) {
      this.roles = new ArrayList<String>();
    } else {
      this.roles = roles;
      Collections.sort(this.roles);
    }
  }
  
  public ArrayList<String> getRoles() {
    return roles;
  }

  public void setRoles(ArrayList<String> roles) {
    this.roles = roles;
    Collections.sort(this.roles);
  }

  public String getUser_id() {
    return user_id;
  }

  public void setUser_id(String user_id) {
    this.user_id = user_id;
  }

  public String getFull_name() {
    return full_name;
  }

  public void setFull_name(String full_name) {
    this.full_name = full_name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((roles == null) ? 0 : roles.hashCode());
    result = prime * result + ((user_id == null) ? 0 : user_id.hashCode());
    result = prime * result + ((full_name == null) ? 0 : full_name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof UserInfo)) {
      return false;
    }
    UserInfo other = (UserInfo) obj;
    boolean simpleResult = ((roles == null) ? (other.roles == null) :
              ((other.roles != null) && (roles.size() == other.roles.size()))) &&
      (user_id == null ? other.user_id == null : (user_id.equals(other.user_id))) &&
      (full_name == null ? other.full_name == null : (full_name.equals(other.full_name)));
    
    if ( !simpleResult ) {
      return false;
    }
    
    if ( roles == null ) {
      return true;
    }
    
    // roles is a sorted list. Compare linearly...
    for ( int i = 0 ; i < roles.size() ; ++i ) {
      if ( !roles.get(i).equals(other.roles.get(i)) ) {
        return false;
      }
    }
    return true;
  }
}
