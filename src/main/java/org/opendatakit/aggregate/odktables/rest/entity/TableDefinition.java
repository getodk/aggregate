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

import java.util.List;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Represents the XML format of a table definition. This is essentially all the
 * necessary information for a table to be defined on the server in a way taht
 * will be ODK Tables -friendly.
 *
 * @author dylan price?
 * @author sudar.sam@gmail.com
 *
 */
@Root
@Default(value = DefaultType.FIELD, required = false)
public class TableDefinition {

  /**
   * This is based roughly on the ODK Tables Schema Google Doc. The required
   * elements are those that are not allowed to be null in (keep this fully
   * qualified!)
   * {@link org.opendatakit.aggregate.odktables.relation.DbTableDefinitions}.
   */

  @Element(name = "table_id", required = true)
  private String tableId;

  @Element(name = "table_key", required = false)
  private String tableKey;

  @Element(name = "db_table_name", required = true)
  private String dbTableName;

  /*
   * While not defined in DbTableDefinitions, this was originally how column
   * information was uploaded to the server, and will remain this way for now.
   */
  @ElementList(inline = true)
  private List<Column> columns;

  // ss: trying to subsume this information into the kvs.
  // @Element(required = false)
  // private String metadata;

  protected TableDefinition() {
  }

  /**
   * Construct the table definition
   *
   * @param tableId
   *          id of the table
   * @param columns
   *          list of {@link Column} objects
   * @param tableKey
   *          key of the table
   * @param dbTableName
   *          the db name of the table
   * @param type
   *          the string type of the table (must be one of (keep this fully
   *          qualified!)
   *          {@link org.opendatakit.aggregate.client.odktables.TableTypeClient#getRepresentation()}
   *          )
   * @param tableIdAccessControls
   *          id of the table holding access controls
   */
  public TableDefinition(final String tableId, final List<Column> columns, final String tableKey,
      final String dbTableName) {
    this.tableId = tableId;
    this.columns = columns;
    this.tableKey = tableKey;
    this.dbTableName = dbTableName;
  }

  public String getTableId() {
    return this.tableId;
  }

  public String getTableKey() {
    return this.tableKey;
  }

  public List<Column> getColumns() {
    return this.columns;
  }

  public String getDbTableName() {
    return this.dbTableName;
  }

  public void setColumns(final List<Column> columns) {
    this.columns = columns;
  }

  @Override
  public String toString() {
    return "TableDefinition [tableId=" + tableId + ", columns=" + columns + ", tableKey="
        + tableKey + ", dbTableName=" + dbTableName + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
    result = prime * result + ((tableKey == null) ? 0 : tableKey.hashCode());
    result = prime * result + ((dbTableName == null) ? 0 : dbTableName.hashCode());
    result = prime * result + ((columns == null) ? 0 : columns.hashCode());
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
    if (!(obj instanceof TableDefinition)) {
      return false;
    }
    TableDefinition other = (TableDefinition) obj;
    return (tableId == null ? other.tableId == null : tableId.equals(other.tableId))
        && (tableKey == null ? other.tableKey == null : tableKey.equals(other.tableKey))
        && (dbTableName == null ? other.dbTableName == null : dbTableName.equals(other.dbTableName))
        && (columns == null ? other.columns == null : columns.equals(other.columns));
  }

}