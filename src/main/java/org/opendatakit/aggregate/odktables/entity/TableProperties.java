package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.client.odktables.TablePropertiesClient;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class TableProperties {

  @Element(name = "etag", required = false)
  private String propertiesEtag;

  @Element(name = "name", required = true)
  private String tableName;

  @Element(required = false)
  private String metadata;

  protected TableProperties() {
  }

  public TableProperties(String propertiesEtag, String tableName, String metadata) {
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
  
  /**
   * Transform the object into the client-side object.
   */
  public TablePropertiesClient transform() {
	  TablePropertiesClient tpClient = new TablePropertiesClient(this.getPropertiesEtag(),
			  this.getTableName(), this.getMetadata());
	  return tpClient;
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
    if (!(obj instanceof TableProperties))
      return false;
    TableProperties other = (TableProperties) obj;
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
