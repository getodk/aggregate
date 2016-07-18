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
  private static final long serialVersionUID = -760352144860371942L;

  public static final RowFilterScopeClient EMPTY_ROW_FILTER_SCOPE;

  static {
    EMPTY_ROW_FILTER_SCOPE = new RowFilterScopeClient();
    EMPTY_ROW_FILTER_SCOPE.initFields(Type.DEFAULT, null);
  }

  public enum Type {
    DEFAULT, MODIFY, READ_ONLY, HIDDEN,
  }

  private Type type;

  private String value;

  /**
   * Constructs a new Scope.
   *
   * @param type
   *          the type of the scope. Must not be null. The empty scope may be
   *          accessed as {@link Scope#EMPTY_SCOPE}.
   * @param value
   *          the userId if type is {@link Type#USER}, or the groupId of type is
   *          {@link Type#GROUP}. If type is {@link Type#DEFAULT}, value is
   *          ignored (set to null).
   */
  public RowFilterScopeClient(Type type, String value) {
    initFields(type, value);
  }

  private RowFilterScopeClient() {
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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof RowFilterScopeClient))
      return false;
    RowFilterScopeClient other = (RowFilterScopeClient) obj;
    if (type != other.type)
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
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
    builder.append("RowFilterScope [type=");
    builder.append(type);
    builder.append(", value=");
    builder.append(value);
    builder.append("]");
    return builder.toString();
  }

}