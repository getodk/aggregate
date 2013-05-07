package org.opendatakit.aggregate.odktables.entity.api;

import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.odktables.TableResourceClient;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Root;

@Root
@Default(value = DefaultType.FIELD, required = true)
public class TableResource extends TableEntry {
  private String selfUri;
  private String definitionUri;
  private String propertiesUri;
  private String dataUri;
  private String diffUri;
  private String aclUri;

  public TableResource(TableEntry entry) {
    super(entry.getTableId(), entry.getTableKey(), entry.getDataEtag(), entry.getPropertiesEtag());
  }

  @SuppressWarnings("unused")
  private TableResource() {
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
  
  /**
   * This method transforms the TableResource into a client-side 
   * TableResourceClient object.
   */
  public TableResourceClient transform() {
	  TableResourceClient trc = new TableResourceClient(new TableEntryClient(this.getTableId(),
			  this.getTableKey(), this.getDataEtag(), this.getPropertiesEtag()));
	  trc.setAclUri(this.getAclUri());
	  trc.setDataUri(this.getDataUri());
	  trc.setDiffUri(this.getDiffUri());
	  trc.setPropertiesUri(this.getPropertiesUri());
	  trc.setSelfUri(this.getSelfUri());
	  trc.setDefinitionUri(this.getDefinitionUri());
	  return trc;
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
    if (!(obj instanceof TableResource))
      return false;
    TableResource other = (TableResource) obj;
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
    if (definitionUri == null) {
      if (other.definitionUri != null) {
        return false;
      } else if (!definitionUri.equals(other.definitionUri)) {
        return false;
      }
    }
    return true;
  }

  public boolean canEqual(final Object other) {
    return other instanceof TableResource;
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
    result = prime * result + ((definitionUri == null) ? 0 : definitionUri.hashCode());
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