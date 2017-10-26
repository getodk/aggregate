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

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RowFilterScope implements Comparable<RowFilterScope> {

  /**
   * Type of Filter.
   *
   * Limited to 10 characters
   */
  public enum Access {
    FULL, MODIFY, READ_ONLY, HIDDEN,
  }

  @JsonProperty(required = false)
  private Access defaultAccess;

  @JsonProperty(required = false)
  private String rowOwner;

  @JsonProperty(required = false)
  private String groupReadOnly;

  @JsonProperty(required = false)
  private String groupModify;

  @JsonProperty(required = false)
  private String groupPrivileged;

  public static final RowFilterScope EMPTY_ROW_FILTER;
  static {
    EMPTY_ROW_FILTER = new RowFilterScope();
    EMPTY_ROW_FILTER.initFields(RowFilterScope.Access.FULL, null, null,
        null, null);
  }

  public static RowFilterScope asRowFilter(String dAccess, String rowOwner, String groupReadOnly,
      String groupModify, String groupPrivileged) {
    RowFilterScope.Access access = RowFilterScope.Access.FULL;
    
    if (dAccess != null) {
      access = RowFilterScope.Access.valueOf(dAccess);
    }    

    return new RowFilterScope(access, rowOwner, groupReadOnly, groupModify, groupPrivileged);

  }

  /**
   * Constructs a new RowFilter.
   *
   * @param access
   *          the type of the filter. Must not be null. The empty row filter may
   *          be accessed as {@link RowFilterScope#EMPTY_ROW_FILTER}.
   * @param rowOwner
   *          the "rowOwner" userId if any. null if no designated rowOwner.
   */
  public RowFilterScope(Access access, String rowOwner, String groupReadOnly, String groupModify,
      String groupPrivileged) {
    Validate.notNull(access);

    initFields(access, rowOwner, groupReadOnly, groupModify, groupPrivileged);
  }

  private RowFilterScope() {
  }

  private void initFields(Access access, String rowOwner, String groupReadOnly, String groupModify,
      String groupPrivileged) {
    this.defaultAccess = access;
    this.rowOwner = rowOwner;
    this.groupReadOnly = groupReadOnly;
    this.groupModify = groupModify;
    this.groupPrivileged = groupPrivileged;
  }

  /**
   * @return the defaultAccess
   */
  public Access getDefaultAccess() {
    return defaultAccess;
  }

  /**
   * @param access
   *          the access level to set
   */
  public void setDefaultAccess(Access access) {
    this.defaultAccess = access;
  }

  /**
   * @return the rowOwner of the row
   */
  public String getRowOwner() {
    return rowOwner;
  }

  /**
   * @param rowOwner
   *          the rowOwner to set
   */
  public void setRowOwner(String rowOwner) {
    
    this.rowOwner = rowOwner;
  }

  /**
   * getGroupReadOnly
   * 
   * @return groupReadOnly
   */
  public String getGroupReadOnly() {
    return groupReadOnly;
  }

  /**
   * setGroupReadOnly
   * 
   * @param groupReadOnly
   */
  public void setGroupReadOnly(String groupReadOnly) {
    this.groupReadOnly = groupReadOnly;
  }

  /**
   * getGroupModify
   * 
   * @return groupModify
   */
  public String getGroupModify() {
    return this.groupModify;
  }

  /**
   * setGroupModify
   * 
   * @param groupModify
   */
  public void setGroupModify(String groupModify) {
    this.groupModify = groupModify;
  }

  /**
   * getGroupPrivileged
   * 
   * @return groupPrivileged
   */
  public String getGroupPrivileged() {
    return this.groupPrivileged;
  }

  /**
   * setGroupPrivileged
   * 
   * @param groupPrivileged
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
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof RowFilterScope)) {
      return false;
    }
    RowFilterScope other = (RowFilterScope) obj;
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
    builder.append("RowFilter [defaultAccess=");
    builder.append(defaultAccess);
    builder.append(", rowOwner=");
    builder.append(rowOwner);
    builder.append(", groupReadOnly=");
    builder.append(groupReadOnly);
    builder.append(", groupModify=");
    builder.append(groupModify);
    builder.append(", groupPrivileged=");
    builder.append(groupPrivileged);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int compareTo(RowFilterScope arg0) {
    if (arg0 == null) { return -1; }

    int outcome = defaultAccess.name().compareTo(arg0.defaultAccess.name());
    if (outcome != 0) { return outcome; }

    outcome = (rowOwner == null) ? ((arg0.rowOwner == null) ? 0 : -1) : rowOwner.compareTo(arg0.rowOwner);
    if (outcome != 0) { return outcome; }
    
    outcome = (groupReadOnly == null) ? ((arg0.groupReadOnly == null) ? 0 : -1) : groupReadOnly.compareTo(arg0.groupReadOnly);
    if (outcome != 0) { return outcome; }
    
    outcome = (groupModify == null) ? ((arg0.groupModify == null) ? 0 : -1) : groupModify.compareTo(arg0.groupModify);
    if (outcome != 0) { return outcome; }
    
    outcome = (groupPrivileged == null) ? ((arg0 == null) ? 0 : -1) : groupPrivileged.compareTo(arg0.groupPrivileged);
    return outcome;

  }

}