package org.opendatakit.aggregate.client.odktables;

import java.io.Serializable;
import java.util.List;

/**
 * This is the client-side version of 
 * org.opendatakit.aggregate.odktables.entity.TableProperties.java.
 * <br>
 * The idea is that this will do the same thing, but for the server. The common
 * caveats apply for all of these objects, in that it is not yet clear how 
 * they will work, and if there will need to be similar objects that are NOT
 * the original for-the-phone objects on the server.
 * @author sudar.sam@gmail.com
 *
 */
public class TablePropertiesClient implements Serializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = 197746211663068997L;

  private String propertiesEtag;
  private String tableKey;
  private List<OdkTablesKeyValueStoreEntryClient> kvsEntries;

  protected TablePropertiesClient() {
  }

  public TablePropertiesClient(String propertiesEtag, String tableKey, 
      List<OdkTablesKeyValueStoreEntryClient> kvsEntries) {
    this.propertiesEtag = propertiesEtag;
    this.tableKey = tableKey;
    this.kvsEntries = kvsEntries;
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

  public List<OdkTablesKeyValueStoreEntryClient> getKeyValueStoreEntries() {
    return this.kvsEntries;
  }
  
  public void setKeyValueStoreEntries(
      List<OdkTablesKeyValueStoreEntryClient> kvsEntries) {
    this.kvsEntries = kvsEntries;
  }

  @Override
  public String toString() {
    return "TableProperties [propertiesEtag=" + propertiesEtag 
        + ", tableName=" + tableKey
        + ", kvsEntries=" + kvsEntries.toString()
        + "]";
  }

}
