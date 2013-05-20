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
  public String toString() {
    return "TableProperties [propertiesEtag=" + propertiesEtag
        + ", tableName=" + tableKey
        + ", kvsEntries=" + this.kvsEntries.toString()
        + "]";
  }

}
