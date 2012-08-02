package org.opendatakit.aggregate.client.odktables;

import java.io.Serializable;

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

  private String tableName;

  private String metadata;

  protected TablePropertiesClient() {
  }

  public TablePropertiesClient(String propertiesEtag, String tableName, String metadata) {
    this.propertiesEtag = propertiesEtag;
    this.tableName = tableName;
    this.metadata = metadata;
  }

  public String getPropertiesEtag() {
    return propertiesEtag;
  }

  public void setPropertiesEtag(String propertiesEtag) {
    this.propertiesEtag = propertiesEtag;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getMetadata() {
    return metadata;
  }

  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
    result = prime * result + ((propertiesEtag == null) ? 0 : propertiesEtag.hashCode());
    result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TablePropertiesClient))
      return false;
    TablePropertiesClient other = (TablePropertiesClient) obj;
    if (metadata == null) {
      if (other.metadata != null)
        return false;
    } else if (!metadata.equals(other.metadata))
      return false;
    if (propertiesEtag == null) {
      if (other.propertiesEtag != null)
        return false;
    } else if (!propertiesEtag.equals(other.propertiesEtag))
      return false;
    if (tableName == null) {
      if (other.tableName != null)
        return false;
    } else if (!tableName.equals(other.tableName))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "TableProperties [propertiesEtag=" + propertiesEtag + ", tableName=" + tableName
        + ", metadata=" + metadata + "]";
  }

}
