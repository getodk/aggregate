package org.opendatakit.aggregate.client.odktables;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This is the client-side version of 
 * org.opendatakit.aggregate.odktables.entity.api.
 * <br>
 * The idea is that this will perform the same function on the client.
 * Usual caveat that it's not yet clear if this is necessary or if a 
 * new non-phone class has to be created on the server.
 * @author sudar.sam@gmail.com
 *
 */
public class TableResourceClient extends TableEntryClient 
    implements IsSerializable{
  private String selfUri;
  private String definitionUri;
  private String propertiesUri;
  private String dataUri;
  private String diffUri;
  private String aclUri;

  public TableResourceClient(TableEntryClient entry) {
    super(entry.getTableId(), entry.getTableKey(), entry.getDataEtag(), entry.getPropertiesEtag());
  }

  @SuppressWarnings("unused")
  private TableResourceClient() {
    // necessary for gwt serialization
  }

  public String getSelfUri() {
    return this.selfUri;
  }
  
  public String getDefinitionUri() {
    return this.definitionUri;
  }

  public String getPropertiesUri() {
    return this.propertiesUri;
  }

  public String getDataUri() {
    return this.dataUri;
  }

  public String getDiffUri() {
    return this.diffUri;
  }

  public String getAclUri() {
    return this.aclUri;
  }

  public void setSelfUri(final String selfUri) {
    this.selfUri = selfUri;
  }
  
  public void setDefinitionUri(final String definitionUri) {
    this.definitionUri = definitionUri;
  }

  public void setPropertiesUri(final String propertiesUri) {
    this.propertiesUri = propertiesUri;
  }

  public void setDataUri(final String dataUri) {
    this.dataUri = dataUri;
  }

  public void setDiffUri(final String diffUri) {
    this.diffUri = diffUri;
  }

  public void setAclUri(final String aclUri) {
    this.aclUri = aclUri;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (!(obj instanceof TableResourceClient))
      return false;
    TableResourceClient other = (TableResourceClient) obj;
    if (aclUri == null) {
      if (other.aclUri != null)
        return false;
    } else if (!aclUri.equals(other.aclUri))
      return false;
    if (dataUri == null) {
      if (other.dataUri != null)
        return false;
    } else if (!dataUri.equals(other.dataUri))
      return false;
    if (diffUri == null) {
      if (other.diffUri != null)
        return false;
    } else if (!diffUri.equals(other.diffUri))
      return false;
    if (propertiesUri == null) {
      if (other.propertiesUri != null)
        return false;
    } else if (!propertiesUri.equals(other.propertiesUri))
      return false;
    if (selfUri == null) {
      if (other.selfUri != null)
        return false;
    } else if (!selfUri.equals(other.selfUri))
      return false;
    return true;
  }

  public boolean canEqual(final Object other) {
    return other instanceof TableResourceClient;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((aclUri == null) ? 0 : aclUri.hashCode());
    result = prime * result + ((dataUri == null) ? 0 : dataUri.hashCode());
    result = prime * result + ((diffUri == null) ? 0 : diffUri.hashCode());
    result = prime * result + ((propertiesUri == null) ? 0 : propertiesUri.hashCode());
    result = prime * result + ((selfUri == null) ? 0 : selfUri.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("TableResource [selfUri=");
    builder.append(selfUri);
    builder.append(", propertiesUri=");
    builder.append(propertiesUri);
    builder.append(", dataUri=");
    builder.append(dataUri);
    builder.append(", diffUri=");
    builder.append(diffUri);
    builder.append(", aclUri=");
    builder.append(aclUri);
    builder.append("]");
    return builder.toString();
  }
}
