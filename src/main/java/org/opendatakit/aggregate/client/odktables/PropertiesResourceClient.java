package org.opendatakit.aggregate.client.odktables;

/**
 * This is the client-side version of 
 * org.opendatakit.aggregate.odktables.entity.api.PropertiesResource.java.
 * <br>
 * The idea is that this will serve the same function as the server-side object,
 * but for the client. It is possible that a similar object might have to be 
 * created on the server-side to handle the non-phone requests, but this 
 * will hopefully become apparent.
 * @author sudar.sam@gmail.com
 *
 */
public class PropertiesResourceClient extends TablePropertiesClient {

  private String selfUri;
  private String tableUri;

  private PropertiesResourceClient() {
  }

  public PropertiesResourceClient(TablePropertiesClient tableProperties) {
    super();
    setPropertiesEtag(tableProperties.getPropertiesEtag());
    setTableName(tableProperties.getTableKey());
    setKeyValueStoreEntries(tableProperties.getKeyValueStoreEntries());
  }

  public String getSelfUri() {
    return this.selfUri;
  }

  public String getTableUri() {
    return this.tableUri;
  }

  public void setSelfUri(final String selfUri) {
    this.selfUri = selfUri;
  }

  public void setTableUri(final String tableUri) {
    this.tableUri = tableUri;
  }
  
  @Override
  public boolean equals(final Object o) {
    if (o == this)
      return true;
    if (!(o instanceof PropertiesResourceClient))
      return false;
    final PropertiesResourceClient other = (PropertiesResourceClient) o;
    if (!other.canEqual((java.lang.Object) this))
      return false;
    if (!super.equals(o))
      return false;
    if (this.getSelfUri() == null ? other.getSelfUri() != null : !this.getSelfUri().equals(
        (java.lang.Object) other.getSelfUri()))
      return false;
    if (this.getTableUri() == null ? other.getTableUri() != null : !this.getTableUri().equals(
        (java.lang.Object) other.getTableUri()))
      return false;
    return true;
  }

  public boolean canEqual(final Object other) {
    return other instanceof PropertiesResourceClient;
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = result * PRIME + super.hashCode();
    result = result * PRIME + (this.getSelfUri() == null ? 0 : this.getSelfUri().hashCode());
    result = result * PRIME + (this.getTableUri() == null ? 0 : this.getTableUri().hashCode());
    return result;
  }

  public String toString() {
    return "PropertiesResource(super=" + super.toString() + ", selfUri=" + this.getSelfUri()
        + ", tableUri=" + this.getTableUri() + ")";
  }

}

