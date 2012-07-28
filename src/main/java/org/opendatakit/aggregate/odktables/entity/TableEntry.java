package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Root;

@Root
@Default(value = DefaultType.FIELD, required = false)
public class TableEntry {

  private String tableId;
  private String tableName;
  private String dataEtag;
  private String propertiesEtag;

  protected TableEntry() {
  }

  public TableEntry(final String tableId, String tableName, final String dataEtag,
      final String propertiesEtag) {
    this.tableId = tableId;
    this.tableName = tableName;
    this.dataEtag = dataEtag;
    this.propertiesEtag = propertiesEtag;
  }

  public String getTableId() {
    return this.tableId;
  }

  public String getTableName() {
    return this.tableName;
  }

  public String getDataEtag() {
    return this.dataEtag;
  }

  public void setTableId(final String tableId) {
    this.tableId = tableId;
  }

  public void setTablename(String tableName) {
    this.tableName = tableName;
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
  
  /**
   * Transforms the object into client-side TableEntryClient object.
   */
  public TableEntryClient transform() {
	  TableEntryClient tec = new TableEntryClient(this.getTableId(), this.getTableName(),
			  this.getDataEtag(), this.getPropertiesEtag());
	  return tec;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TableEntry))
      return false;
    TableEntry other = (TableEntry) obj;
    if (dataEtag == null) {
      if (other.dataEtag != null)
        return false;
    } else if (!dataEtag.equals(other.dataEtag))
      return false;
    if (propertiesEtag == null) {
      if (other.propertiesEtag != null)
        return false;
    } else if (!propertiesEtag.equals(other.propertiesEtag))
      return false;
    if (tableId == null) {
      if (other.tableId != null)
        return false;
    } else if (!tableId.equals(other.tableId))
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
    result = prime * result + ((dataEtag == null) ? 0 : dataEtag.hashCode());
    result = prime * result + ((propertiesEtag == null) ? 0 : propertiesEtag.hashCode());
    result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
    result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "TableEntry [tableId=" + tableId + ", tableName=" + tableName + ", dataEtag=" + dataEtag
        + ", propertiesEtag=" + propertiesEtag + "]";
  }
}