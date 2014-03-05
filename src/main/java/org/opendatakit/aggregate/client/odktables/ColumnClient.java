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
 * This is the client-side code of the Column class under odktables rest entity.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class ColumnClient implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 668434593121197884L;

  private String tableId;
  private String elementKey;
  private String elementName;
  private String elementType; // should be the string name() from Tables-side
  private String listChildElementKeys;
  private int isPersisted;
  private String joins;

  @SuppressWarnings("unused")
  private ColumnClient() {
    // necessary for gwt serialization
  }

  /**
   * Create a column. Spaces will be replaced by underscores. The backing dbName
   * of the column will be the displayName changed to lower case and prepended
   * with an underscore.
   *
   * @param displayName
   * @param type
   */
  public ColumnClient(final String tableId, final String elementKey, final String elementName,
      final String elementType, final String listChildElementKeys, final int isPersisted,
      final String joins) {
    // ss: not sure what this was.leaving it out for now.
    // String nameToBeEntered = displayName.toLowerCase().replace(" ", "_");
    this.tableId = tableId;
    this.elementKey = elementKey;
    this.elementName = elementName;
    this.elementType = elementType;
    this.listChildElementKeys = listChildElementKeys;
    this.isPersisted = isPersisted;
    this.joins = joins;
  }

  public String getTableId() {
    return this.tableId;
  }

  public String getElementKey() {
    return this.elementKey;
  }

  public String getElementName() {
    return this.elementName;
  }

  public String getElementType() {
    return this.elementType;
  }

  public String getListChildElementKeys() {
    return this.listChildElementKeys;
  }

  public int getIsPersisted() {
    return this.isPersisted;
  }

  public String getJoins() {
    return this.joins;
  }

  public String toString() {
    return "Column(tableId=" + this.getTableId() + ", elementKey=" + this.getElementKey()
        + ", elementName=" + this.getElementName() + ", elementType=" + this.getElementType()
        + ", listChildElementKeys=" + this.getListChildElementKeys() + ", isPersisted="
        + this.getIsPersisted() + ", joins=" + this.getJoins() + ")";
  }
}
