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

import java.util.ArrayList;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Per Dylan's thesis, a TableProperties object represents only the metadata.
 * The structural layout of the table is stored in the (keep these fully
 * qualified!)
 * {@link org.opendatakit.aggregate.odktables.relation.DbTableDefinitions} and
 * {@link org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions}
 * tables. The metadata stored in this {@link TableProperties} object consists
 * of a list of key value store entries.
 *
 * @author dylan price?
 * @author sudar.sam@gmail.com
 *
 */
@Root(strict = false)
public class TableProperties {

  @Element(name = "schemaETag", required = false)
  private String schemaETag;

  @Element(name = "propertiesETag", required = false)
  private String propertiesETag;

  @Element(name = "tableId", required = true)
  private String tableId;

  @ElementList(inline = true, required = false)
  private ArrayList<OdkTablesKeyValueStoreEntry> kvsEntries;

  protected TableProperties() {
  }

  /**
   *
   * @param schemaETag
   * @param propertiesETag
   * @param tableKey
   *          the tableKey field from {@link DbTableDefinition}
   * @param keyValueStoreEntries
   */
  public TableProperties(String schemaETag, String propertiesETag, String tableId,
      ArrayList<OdkTablesKeyValueStoreEntry> keyValueStoreEntries) {
    this.schemaETag = schemaETag;
    this.propertiesETag = propertiesETag;
    this.tableId = tableId;
    this.kvsEntries = keyValueStoreEntries;
  }

  public String getSchemaETag() {
    return schemaETag;
  }

  public void setSchemaETag(String schemaETag) {
    this.schemaETag = schemaETag;
  }

  public String getPropertiesETag() {
    return propertiesETag;
  }

  public void setPropertiesETag(String propertiesETag) {
    this.propertiesETag = propertiesETag;
  }

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public void setKeyValueStoreEntries(ArrayList<OdkTablesKeyValueStoreEntry> kvsEntries) {
    this.kvsEntries = kvsEntries;
  }

  public ArrayList<OdkTablesKeyValueStoreEntry> getKeyValueStoreEntries() {
    return this.kvsEntries;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((schemaETag == null) ? 1 : schemaETag.hashCode());
    result = prime * result + ((propertiesETag == null) ? 1 : propertiesETag.hashCode());
    result = prime * result + ((tableId == null) ? 1 : tableId.hashCode());
    result = prime * result + ((kvsEntries == null) ? 1 : kvsEntries.hashCode());
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
    if (!(obj instanceof TableProperties)) {
      return false;
    }
    TableProperties other = (TableProperties) obj;
    return (schemaETag == null ? other.schemaETag == null : schemaETag
        .equals(other.schemaETag))
        && (propertiesETag == null ? other.propertiesETag == null : propertiesETag
        .equals(other.propertiesETag))
        && (tableId == null ? other.tableId == null : tableId.equals(other.tableId))
        && (kvsEntries == null ? other.kvsEntries == null : kvsEntries.equals(other.kvsEntries));
  }

  @Override
  public String toString() {
    return "TableProperties [schemaETag=" + schemaETag + ", propertiesETag=" + propertiesETag + ", tableId=" + tableId
        + ", kvsEntries=" + this.kvsEntries.toString() + "]";
  }

}
