package org.opendatakit.aggregate.odktables.entity.api;

import java.util.List;

import org.opendatakit.aggregate.odktables.entity.Column;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class TableDefinition {

  @Element(name = "name", required = true)
  private String tableName;

  @ElementList(inline = true)
  private List<Column> columns;

  @Element(required = false)
  private String metadata;

  @SuppressWarnings("unused")
  private TableDefinition() {
  }

  public TableDefinition(final String tableName, final List<Column> columns, final String metadata) {
    this.tableName = tableName;
    this.columns = columns;
    this.metadata = metadata;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public List<Column> getColumns() {
    return this.columns;
  }

  public void setColumns(final List<Column> columns) {
    this.columns = columns;
  }

  public String getMetadata() {
    return metadata;
  }

  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TableDefinition))
      return false;
    TableDefinition other = (TableDefinition) obj;
    if (columns == null) {
      if (other.columns != null)
        return false;
    } else if (!columns.equals(other.columns))
      return false;
    if (metadata == null) {
      if (other.metadata != null)
        return false;
    } else if (!metadata.equals(other.metadata))
      return false;
    if (tableName == null) {
      if (other.tableName != null)
        return false;
    } else if (!tableName.equals(other.tableName))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((columns == null) ? 0 : columns.hashCode());
    result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
    result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "TableDefinition [tableName=" + tableName + ", columns=" + columns + ", metadata="
        + metadata + "]";
  }
}