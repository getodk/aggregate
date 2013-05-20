package org.opendatakit.aggregate.client.odktables;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This is the client-side version of 
 * org.opendatakit.aggregate.odktables.entity.TableEntry.java.
 * <br>
 * The idea is that this is the client-side implementation of the same
 * object. Usual caveat that it is unclear yet whether this all is 
 * necessary and if similar non-phone objects will be needed on the 
 * server side.
 * @author sudar.sam@gmail.com
 *
 */
public class TableEntryClient implements IsSerializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = -7094543853434685403L;
 
  
  private String tableId;
  private String tableKey;
  private String dataEtag;
  private String propertiesEtag;

  protected TableEntryClient() {
  }

  public TableEntryClient(final String tableId, String tableKey, 
      final String dataEtag, final String propertiesEtag) {
    this.tableId = tableId;
    this.tableKey = tableKey;
    this.dataEtag = dataEtag;
    this.propertiesEtag = propertiesEtag;
  }

  public String getTableId() {
    return this.tableId;
  }

  public String getTableKey() {
    return this.tableKey;
  }

  public String getDataEtag() {
    return this.dataEtag;
  }

  public void setTableId(final String tableId) {
    this.tableId = tableId;
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

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TableEntryClient))
      return false;
    TableEntryClient other = (TableEntryClient) obj;
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
    if (tableKey == null) {
      if (other.tableKey != null)
        return false;
    } else if (!tableKey.equals(other.tableKey))
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
    result = prime * result + ((tableKey == null) ? 0 : tableKey.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "TableEntry [tableId=" + tableId 
        + ", tableKey=" + tableKey 
        + ", dataEtag=" + dataEtag
        + ", propertiesEtag=" + propertiesEtag + "]";
  }
}