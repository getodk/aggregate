/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.odktables;

import java.io.Serializable;

/**
 * This is the client-side version of
 * org.opendatakit.aggregate.odktables.entity. <br>
 * The idea is that this will be the client-side object that will perform the
 * same function. Now standard caveat applies that at this point it is not yet
 * clear if this will be necessary, or if a new non-phone object performing the
 * same function needs to be created for the server.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class TableAclClient implements Serializable {

  /**
	 *
	 */
  private static final long serialVersionUID = 3563986876354300986L;

  private ScopeClient scope;

  private TableRoleClient role;

  public TableAclClient(TableRoleClient role) {
    this();
    this.role = role;
  }

  public TableAclClient() {
    this.scope = ScopeClient.EMPTY_SCOPE;
  }

  /**
   * @return the scope
   */
  public ScopeClient getScope() {
    return scope;
  }

  /**
   * @param scope
   *          the scope to set
   */
  public void setScope(ScopeClient scope) {
    this.scope = scope;
  }

  /**
   * @return the role
   */
  public TableRoleClient getRole() {
    return role;
  }

  /**
   * @param role
   *          the role to set
   */
  public void setRole(TableRoleClient role) {
    this.role = role;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((role == null) ? 0 : role.hashCode());
    result = prime * result + ((scope == null) ? 0 : scope.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TableAclClient))
      return false;
    TableAclClient other = (TableAclClient) obj;
    if (role != other.role)
      return false;
    if (scope == null) {
      if (other.scope != null)
        return false;
    } else if (!scope.equals(other.scope))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("TableAcl [scope=");
    builder.append(scope);
    builder.append(", role=");
    builder.append(role);
    builder.append("]");
    return builder.toString();
  }

}