/*
 * Copyright (C) 2016 University of Washington
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
 * <p>This is the client-side version of RowFilterScope within odktables entity.</p>
 * <p>
 * It might be possible that this isn't necessary. At this point I am just
 * copying exactly the entities that exist in that package, in the hopes of
 * translating almost directly the code implemented in the services there.</p>
 *
 * @author sudar.sam@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class RowFilterScopeClient implements Serializable {

  /**
	 *
	 */
  private static final long serialVersionUID = -76035214486037194L;

  public static final RowFilterScopeClient EMPTY_ROW_FILTER_SCOPE;

  static {
    EMPTY_ROW_FILTER_SCOPE = new RowFilterScopeClient();
    EMPTY_ROW_FILTER_SCOPE.initFields(Access.FULL, null, null, null, null);
  }

  public enum Access {
    FULL, MODIFY, READ_ONLY, HIDDEN,
  }

  private Access defaultAccess;

  private String rowOwner;
  
  private String groupReadOnly;
  
  private String groupModify;
  
  private String groupPrivileged;

  /**
   * Constructs a new Scope.
   *
   * @param access
   *          the type of the scope. Must not be null. The empty scope may be
   *          accessed as {@link Scope#EMPTY_SCOPE}.
   * @param rowOwner
   *          the userId if type is {@link Access#USER}, or the groupId of type is
   *          {@link Access#GROUP}. If type is {@link Access#FULL}, value is
   *          ignored (set to null).
   */
  public RowFilterScopeClient(Access access, String rowOwner, String groupReadOnly, String groupModify, String groupPrivileged) {
    initFields(access, rowOwner, groupReadOnly, groupModify, groupPrivileged);
  }

  private RowFilterScopeClient() {
  }

  private void initFields(Access access, String rowOwner, String groupReadOnly, String groupModify, String groupPrivileged) {
    this.defaultAccess = access;
    this.rowOwner = rowOwner;
    this.groupReadOnly = groupReadOnly;
    this.groupModify = groupModify;
    this.groupPrivileged = groupPrivileged;
  }

  /**
   * @return defaultAccess
   */
  public Access getAccess() {
    return defaultAccess;
  }

  /**
   * @param access
   *          the access to set
   */
  public void setAccess(Access access) {
    this.defaultAccess = access;
  }
  
  /**
   * @return the rowOwner
   */
  public String getRowOwner() {
    return rowOwner;
  }

  /**
   * @param rowOwner
   *          the rowOwner to set
   */
  public void setOwner(String rowOwner) {
    this.rowOwner = rowOwner;
  }
  
  /**
   * @return groupReadOnly
   */
  public String getGroupReadOnly() {
    return groupReadOnly;
  }
  
  /**
   * @param groupReadOnly
   *     groupReadOnly to set
   */
  public void setGroupReadOnly(String groupReadOnly) {
    this.groupReadOnly = groupReadOnly;
  }
  
  /**
   * 
   * @return groupModify
   */
  public String getGroupModify() {
    return groupModify;
  }
  
  /**
   * 
   * @param groupModify
   *     groupModify to set
   */
  public void setGroupModify(String groupModify) {
    this.groupModify = groupModify;
  }
  
  /**
   * 
   * @return groupPrivileged
   */
  public String getGroupPrivileged() {
    return groupPrivileged;
  }
  
  /**
   * 
   * @param groupPrivileged
   *        groupPrivileged to set
   */
  public void setGroupPrivileged(String groupPrivileged) {
    this.groupPrivileged = groupPrivileged;
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
    result = prime * result + ((defaultAccess == null) ? 0 : defaultAccess.hashCode());
    result = prime * result + ((rowOwner == null) ? 0 : rowOwner.hashCode());
    result = prime * result + ((groupReadOnly == null) ? 0 : groupReadOnly.hashCode());
    result = prime * result + ((groupModify == null) ? 0 : groupModify.hashCode());
    result = prime * result + ((groupPrivileged == null) ? 0 : groupPrivileged.hashCode());
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
    if (!(obj instanceof RowFilterScopeClient))
      return false;
    
    RowFilterScopeClient other = (RowFilterScopeClient) obj;
    
    return (defaultAccess == null ? other.defaultAccess == null : defaultAccess.equals(other.defaultAccess))
        && (rowOwner == null ? other.rowOwner == null : rowOwner.equals(other.rowOwner))
        && (groupReadOnly == null ? other.groupReadOnly == null : groupReadOnly.equals(other.groupReadOnly))
        && (groupModify == null ? other.groupModify == null : groupModify.equals(other.groupModify))
        && (groupPrivileged == null ? other.groupPrivileged == null : groupPrivileged.equals(other.groupPrivileged));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("RowFilterScope [defaultAccess=");
    builder.append(defaultAccess);
    builder.append(", rowOwner=");
    builder.append(rowOwner);
    builder.append(", groupReadOnly=");
    builder.append(groupReadOnly);
    builder.append(", groupModify");
    builder.append(groupModify);
    builder.append(", groupPrivileged");
    builder.append(groupPrivileged);
    builder.append("]");
    return builder.toString();
  }

}