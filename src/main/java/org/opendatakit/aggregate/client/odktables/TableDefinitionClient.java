package org.opendatakit.aggregate.client.odktables;

import java.util.List;

/**
 * This is the client-side version of 
 * org.opendatakit.aggregate.odktables.entity.api.
 * <br>
 * The idea is that it will do the same thing, but on the client side.
 * Usual caveat that it is not yet clear if this is needed or if another
 * thing needs be created for the server to do non-phone things.
 * @author sudar.sam@gmail.com
 *
 */
public class TableDefinitionClient {

  private String tableName;

  private List<ColumnClient> columns;

  private String metadata;

  private TableDefinitionClient() {
  }

  public TableDefinitionClient(final String tableName, final List<ColumnClient> columns, final String metadata) {
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

  public List<ColumnClient> getColumns() {
    return this.columns;
  }

  public void setColumns(final List<ColumnClient> columns) {
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
    if (!(obj instanceof TableDefinitionClient))
      return false;
    TableDefinitionClient other = (TableDefinitionClient) obj;
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
