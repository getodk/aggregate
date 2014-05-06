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

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * A TableEntry defines very only the tableId, the tableKey (for now), and ETags
 * for the data and the properties. This stands in as a quick point of reference
 * for clients to access properties and data ETags to see if changes have been
 * made.
 *
 * @author dylan price?
 * @author sudar.sam@gmail.com
 *
 */
@Root
@Default(value = DefaultType.FIELD, required = false)
public class TableEntry {

  @Element(required = true)
  private String tableId;

  @Element(required = false)
  private String dataETag;

  @Element(required = false)
  private String schemaETag;

  protected TableEntry() {
  }

  public TableEntry(final String tableId, final String dataETag, final String schemaETag) {
    this.tableId = tableId;
    this.dataETag = dataETag;
    this.schemaETag = schemaETag;
  }

  public String getTableId() {
    return this.tableId;
  }

  public void setTableId(final String tableId) {
    this.tableId = tableId;
  }

  public String getDataETag() {
    return this.dataETag;
  }

  public void setDataETag(final String dataETag) {
    this.dataETag = dataETag;
  }

  public String getSchemaETag() {
    return schemaETag;
  }

  public void setSchemaETag(String schemaETag) {
    this.schemaETag = schemaETag;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tableId == null) ? 1 : tableId.hashCode());
    result = prime * result + ((dataETag == null) ? 1 : dataETag.hashCode());
    result = prime * result + ((schemaETag == null) ? 1 : schemaETag.hashCode());
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
    if (!(obj instanceof TableEntry)) {
      return false;
    }
    TableEntry other = (TableEntry) obj;
    return (tableId == null ? other.tableId == null : tableId.equals(other.tableId))
        && (dataETag == null ? other.dataETag == null : dataETag.equals(other.dataETag))
        && (schemaETag == null ? other.schemaETag == null : schemaETag.equals(other.schemaETag));
  }

  @Override
  public String toString() {
    return "TableEntry [tableId=" + tableId + ", dataETag=" + dataETag + ", schemaETag=" + schemaETag + "]";
  }
}