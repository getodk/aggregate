package org.opendatakit.aggregate.odktables.entity;

import java.util.List;

import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Per Dylan's thesis, a TableProperties object represents only the metadata.
 * The structural layout of the table is stored in the
 * {@link DbTableDefinitions} and {@link DbColumnDefinitions} tables. The
 * metadata stored in this {@link TableProperties} object consists of a list of
 * key value store entries.
 * @author dylan price?
 * @author sudar.sam@gmail.com
 *
 */
@Root(strict = false)
public class TableProperties {

  @Element(name = "etag", required = false)
  private String propertiesEtag;

  @Element(name = "tableKey", required = true)
  private String tableKey;

  @ElementList(inline = true, required = false)
  private List<OdkTablesKeyValueStoreEntry> kvsEntries;

  protected TableProperties() {
  }

  /**
   *
   * @param propertiesEtag
   * @param tableKey the tableKey field from {@link DbTableDefinition}
   * @param keyValueStoreEntries
   */
  public TableProperties(String propertiesEtag, String tableKey,
      List<OdkTablesKeyValueStoreEntry> keyValueStoreEntries) {
    this.propertiesEtag = propertiesEtag;
    this.tableKey = tableKey;
    this.kvsEntries = keyValueStoreEntries;
  }

  public String getPropertiesEtag() {
    return propertiesEtag;
  }

  public void setPropertiesEtag(String propertiesEtag) {
    this.propertiesEtag = propertiesEtag;
  }

  public String getTableKey() {
    return tableKey;
  }

  public void setTableName(String tableName) {
    this.tableKey = tableName;
  }

  public void setKeyValueStoreEntries(
      List<OdkTablesKeyValueStoreEntry> kvsEntries) {
    this.kvsEntries = kvsEntries;
  }

  public List<OdkTablesKeyValueStoreEntry> getKeyValueStoreEntries() {
    return this.kvsEntries;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((propertiesEtag == null) ? 0 : propertiesEtag.hashCode());
    result = prime * result + ((tableKey == null) ? 0 : tableKey.hashCode());
    result = prime * result + ((kvsEntries == null) ? 0 : kvsEntries.hashCode());
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
    if (!(obj instanceof TableProperties)) {
      return false;
    }
    TableProperties other = (TableProperties) obj;
    return (propertiesEtag == null ? other.propertiesEtag == null : propertiesEtag.equals(other.propertiesEtag))
        && (tableKey == null ? other.tableKey == null : tableKey.equals(other.tableKey))
        && (kvsEntries == null ? other.kvsEntries == null : kvsEntries.equals(other.kvsEntries));
  }

  @Override
  public String toString() {
    return "TableProperties [propertiesEtag=" + propertiesEtag
        + ", tableName=" + tableKey
        + ", kvsEntries=" + this.kvsEntries.toString()
        + "]";
  }

}
