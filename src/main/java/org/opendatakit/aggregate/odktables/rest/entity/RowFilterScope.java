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
  public enum Type {
    DEFAULT, MODIFY, READ_ONLY, HIDDEN,
  }

  @JsonProperty(required = false)
  private Type type;

  @JsonProperty(required = false)
  private String value;


  public static final RowFilterScope EMPTY_ROW_FILTER;
  static {
	  EMPTY_ROW_FILTER = new RowFilterScope();
	  EMPTY_ROW_FILTER.initFields(RowFilterScope.Type.DEFAULT, null);
  }

  public static RowFilterScope asRowFilter(String filterType, String filterValue) {
    if (filterType != null && filterType.length() != 0) {
      RowFilterScope.Type type = RowFilterScope.Type.valueOf(filterType);
      return new RowFilterScope(type, (filterValue == null || filterValue.length() == 0) ? null
            : filterValue);
    } else {
      return RowFilterScope.EMPTY_ROW_FILTER;
    }
  }

  /**
   * Constructs a new RowFilter.
   *
   * @param type
   *          the type of the filter. Must not be null. The empty row filter may be
   *          accessed as {@link RowFilterScope#EMPTY_ROW_FILTER}.
   * @param value
   *          the "owner" userId if any. null if no designated owner.
   */
  public RowFilterScope(Type type, String value) {
    Validate.notNull(type);

    initFields(type, value);
  }

  private RowFilterScope() {
  }

  private void initFields(Type type, String value) {
    this.type = type;
    this.value = value;
  }

  /**
   * @return the type
   */
  public Type getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(Type type) {
    this.type = type;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
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
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    return (type == null ? other.type == null : type.equals(other.type))
        && (value == null ? other.value == null : value.equals(other.value));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("RowFilter [type=");
    builder.append(type);
    builder.append(", value=");
    builder.append(value);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int compareTo(RowFilterScope arg0) {
    if ( arg0 == null ) {
      return -1;
    }
    
    int outcome = type.name().compareTo(arg0.type.name());
    if ( outcome != 0 ) {
      return outcome;
    }
    outcome = (value == null) ? 
        ((arg0.value == null) ? 0 : -1) : value.compareTo(arg0.value);
    return outcome;
  }

}