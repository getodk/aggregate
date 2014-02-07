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
   * Schema version ETag for the tableId's database schema.
   */
  @Element(name = "schemaETag", required = false)
  private String schemaETag;

  /**
   * Unique tableId
   */
  @Element(name = "tableId", required = true)
  private String tableId;

  /**
   * The columns in the table.
   */
  @ElementList(inline = true, required = false)
  private ArrayList<Column> columns;

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
   * @param schemaETag
   *          schemaETag of the table
   * @param columns
   *          list of {@link Column} objects
   * @param displayName
   *          the displayName of the table (JSON.parse() to get viewable name)
   * @param type
   *          the string type of the table (must be one of (keep this fully
   *          qualified!)
   *          {@link org.opendatakit.aggregate.client.odktables.TableTypeClient#getRepresentation()}
   *          )
   * @param tableIdAccessControls
   *          id of the table holding access controls
   */
  public TableDefinition(final String tableId, final String schemaETag, final ArrayList<Column> columns) {
    this.tableId = tableId;
    this.schemaETag = schemaETag;
    if ( columns == null ) {
      this.columns = new ArrayList<Column>();
    } else {
      this.columns = columns;
    }
  }

  public String getSchemaETag() {
    return this.schemaETag;
  }

  public void setSchemaETag(String schemaETag) {
    this.schemaETag = schemaETag;
  }

  public String getTableId() {
    return this.tableId;
  }

  public ArrayList<Column> getColumns() {
    return this.columns;
  }

  public void setColumns(final ArrayList<Column> columns) {
    if ( columns == null ) {
      this.columns = new ArrayList<Column>();
    } else {
      this.columns = columns;
    }
  }

  @Override
  public String toString() {
    return "TableDefinition [schemaETag=" + schemaETag
        + ", tableId=" + tableId
        + ", columns=" + columns
        + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((schemaETag == null) ? 1 : schemaETag.hashCode());
    result = prime * result + ((tableId == null) ? 1 : tableId.hashCode());
    result = prime * result + ((columns == null) ? 1 : columns.hashCode());
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
    return (schemaETag == null ? other.schemaETag == null : schemaETag.equals(other.schemaETag))
        && (tableId == null ? other.tableId == null : tableId.equals(other.tableId))
        && (columns == null ? other.columns == null : columns.equals(other.columns));
  }

}