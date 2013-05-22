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

package org.opendatakit.aggregate.odktables.entity;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Root;

/**
 * A TableEntry defines very only the tableId, the tableKey (for now), and
 * etags for the data and the properties. This stands in as a quick point of
 * reference for clients to access properties and data etags to see if changes
 * have been made.
 * @author dylan price?
 * @author sudar.sam@gmail.com
 *
 */
@Root
@Default(value = DefaultType.FIELD, required = false)
public class TableEntry {

  private String tableId;
  private String tableKey;
  private String dataEtag;
  private String propertiesEtag;

  protected TableEntry() {
  }

  public TableEntry(final String tableId, String tableKey,
      final String dataEtag, final String propertiesEtag) {
    this.tableId = tableId;
    this.tableKey = tableKey;
    this.dataEtag = dataEtag;
    this.propertiesEtag = propertiesEtag;
  }

  public String getTableId() {
    return this.tableId;
  }

  public String getTableKey() {
    return this.tableKey;
  }

  public String getDataEtag() {
    return this.dataEtag;
  }

  public void setTableId(final String tableId) {
    this.tableId = tableId;
  }

  public void setDataEtag(final String dataEtag) {
    this.dataEtag = dataEtag;
  }

  public String getPropertiesEtag() {
    return propertiesEtag;
  }

  public void setPropertiesEtag(String propertiesEtag) {
    this.propertiesEtag = propertiesEtag;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
    result = prime * result + ((tableKey == null) ? 0 : tableKey.hashCode());
    result = prime * result + ((dataEtag == null) ? 0 : dataEtag.hashCode());
    result = prime * result + ((propertiesEtag == null) ? 0 : propertiesEtag.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if ( obj == null ) {
      return false;
    }
    if ( obj == this ) {
      return true;
    }
    if (!(obj instanceof TableEntry)) {
      return false;
    }
    TableEntry other = (TableEntry) obj;
    return (tableId == null ? other.tableId == null : tableId.equals(other.tableId))
        && (tableKey == null ? other.tableKey == null : tableKey.equals(other.tableKey))
        && (dataEtag == null ? other.dataEtag == null : dataEtag.equals(other.dataEtag))
        && (propertiesEtag == null ? other.propertiesEtag == null : propertiesEtag.equals(other.propertiesEtag));
  }

  @Override
  public String toString() {
    return "TableEntry [tableId=" + tableId
        + ", tableKey=" + tableKey
        + ", dataEtag=" + dataEtag
        + ", propertiesEtag=" + propertiesEtag
        + "]";
  }
}