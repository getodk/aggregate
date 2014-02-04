/*
 * Copyright (C) 2012-2013 University of Washington
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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class TableAcl {

  @Element
  private Scope scope;

  @Element
  private TableRole role;

  public TableAcl(TableRole role) {
    this();
    this.role = role;
  }

  public TableAcl() {
    this.scope = Scope.EMPTY_SCOPE;
  }

  /**
   * @return the scope
   */
  public Scope getScope() {
    return scope;
  }

  /**
   * @param scope
   *          the scope to set
   */
  public void setScope(Scope scope) {
    this.scope = scope;
  }

  /**
   * @return the role
   */
  public TableRole getRole() {
    return role;
  }

  /**
   * @param role
   *          the role to set
   */
  public void setRole(TableRole role) {
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
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof TableAcl)) {
      return false;
    }
    TableAcl other = (TableAcl) obj;
    return (role == null ? other.role == null : role.equals(other.role))
        && (scope == null ? other.scope == null : scope.equals(other.scope));
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