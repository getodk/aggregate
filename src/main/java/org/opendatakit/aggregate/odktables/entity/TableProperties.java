package org.opendatakit.aggregate.odktables.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opendatakit.aggregate.client.exception.ImportFromCSVExceptionClient;
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
  
  /**
   * Return the metadata string as a map. The metada exists as a JSON object
   * in the datastore, so this gives a better way to interact with it.
   * See the warnings at {@link setMetadata} about setting the metadata from
   * the server.
   * @return
   * @throws ImportFromCSVExceptionClient
   */
  public Map<String, Object> getMetadataAsMap() 
      throws ImportFromCSVExceptionClient {
    ObjectMapper mapper = new ObjectMapper();
    try {
      HashMap<String, Object> metadataMap =
          mapper.readValue(metadata, HashMap.class);
      return metadataMap;
    } catch (JsonMappingException e) {
      throw new ImportFromCSVExceptionClient("json mapping exception", e);
    } catch (JsonParseException e) {
      throw new ImportFromCSVExceptionClient("json parse exception", e);
    } catch (IOException e) {
      throw new ImportFromCSVExceptionClient("IOException", e);
    }
  }

  /**
   * Set the metadata for the properties. This should be used very sparingly,
   * if at all. This is configured by the phone, and changes made here might
   * end up breaking the tables on the phone unless you are very, very careful
   * about what you are doing.
   * @param metadata
   */
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
