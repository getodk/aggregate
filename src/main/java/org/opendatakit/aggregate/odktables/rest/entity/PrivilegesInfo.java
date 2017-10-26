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
 * This contains the default group of the user and the list of
 * groups and roles to which the currently authenticated user is
 * assigned.
 *
 * @author mitchellsundt@gmail.com
 *
 */
@JacksonXmlRootElement(localName="privilegesInfo")
public class PrivilegesInfo {

  /**
   * User id -- this may be more fully-qualified than the user identity information
   * that the client used for login (the server may have provided auto-completion 
   * of a qualifying domain, etc.). The client should update their user
   * identity property to this value.
   */
  @JsonProperty(required = true)
  private String user_id;

  /**
   * Friendly full name for this user. Could be used for display.
   */
  @JsonProperty(required = false)
  private String full_name;

  /**
   * Default group
   */
  @JsonProperty(required = false)
  private String defaultGroup;


  /**
   * The roles and groups this user belongs to.
   * This is sorted alphabetically.
   */
  @JsonProperty(required = false)
  @JacksonXmlElementWrapper(useWrapping=false)
  @JacksonXmlProperty(localName="roles")
  private ArrayList<String> roles;

  /**
   * Constructor used by Jackson
   */
  public PrivilegesInfo() {
    this.roles = new ArrayList<String>();
    this.user_id = null;
    this.full_name = null;
    this.defaultGroup = null;
  }

  /**
   * Constructor used by our Java code
   *
   * @param entries
   */
  public PrivilegesInfo(String user_id,
      String full_name,
      ArrayList<String> roles,
      String defaultGroup) {
    this.user_id = user_id;
    this.full_name = full_name;
    if (roles == null) {
      this.roles = new ArrayList<String>();
    } else {
      this.roles = roles;
      Collections.sort(this.roles);
    }
    this.defaultGroup = defaultGroup;
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

  public String getDefaultGroup() {
    return defaultGroup;
  }

  public void setDefaultGroup(String defaultGroup) {
    this.defaultGroup = defaultGroup;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((roles == null) ? 0 : roles.hashCode());
    result = prime * result + ((user_id == null) ? 0 : user_id.hashCode());
    result = prime * result + ((full_name == null) ? 0 : full_name.hashCode());
    result = prime * result + ((defaultGroup == null) ? 0 : defaultGroup.hashCode());
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
    if (!(obj instanceof PrivilegesInfo)) {
      return false;
    }
    PrivilegesInfo other = (PrivilegesInfo) obj;
    boolean simpleResult = ((roles == null) ? (other.roles == null) :
              ((other.roles != null) && (roles.size() == other.roles.size()))) &&
      (user_id == null ? other.user_id == null : (user_id.equals(other.user_id))) &&
      (full_name == null ? other.full_name == null : (full_name.equals(other.full_name))) &&
      (defaultGroup == null ? other.defaultGroup == null : (defaultGroup.equals(other.defaultGroup)));
    
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
