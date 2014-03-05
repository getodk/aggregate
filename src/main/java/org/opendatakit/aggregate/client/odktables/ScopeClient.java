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

import org.simpleframework.xml.Element;

/**
 * <p>This is the client-side version of Scope within odktables entity.</p>
 * <p>
 * It might be possible that this isn't necessary. At this point I am just
 * copying exactly the entities that exist in that package, in the hopes of
 * translating almost directly the code implemented in the services there.</p>
 *
 * @author sudar.sam@gmail.com
 *
 */
public class ScopeClient implements Serializable {

  /**
	 *
	 */
  private static final long serialVersionUID = -7603521544860371942L;

  public static final ScopeClient EMPTY_SCOPE;

  static {
    EMPTY_SCOPE = new ScopeClient();
    EMPTY_SCOPE.initFields(null, null);
  }

  public enum Type {
    DEFAULT, USER, GROUP,
  }

  @Element(required = false)
  private Type type;

  @Element(required = false)
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
  public ScopeClient(Type type, String value) {
    // TODO make sure these validate things are compensated for somehow
    // Validate.notNull(type);
    if (type.equals(Type.GROUP)) {
      // Validate.notEmpty(value);
    } else if (type.equals(Type.DEFAULT)) {
      value = null;
    }

    initFields(type, value);
  }

  private ScopeClient() {
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
    if (!(obj instanceof ScopeClient))
      return false;
    ScopeClient other = (ScopeClient) obj;
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
    builder.append("Scope [type=");
    builder.append(type);
    builder.append(", value=");
    builder.append(value);
    builder.append("]");
    return builder.toString();
  }

}