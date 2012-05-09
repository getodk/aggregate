package org.opendatakit.aggregate.odktables.entity.api;

import org.opendatakit.aggregate.odktables.entity.TableProperties;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Root;

@Root
@Default(DefaultType.FIELD)
public class PropertiesResource extends TableProperties {

  private String selfUri;
  private String tableUri;

  @SuppressWarnings("unused")
  private PropertiesResource() {
  }

  public PropertiesResource(TableProperties tableProperties) {
    super();
    setPropertiesEtag(tableProperties.getPropertiesEtag());
    setTableName(tableProperties.getTableName());
    setMetadata(tableProperties.getMetadata());
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
    if (!(o instanceof PropertiesResource))
      return false;
    final PropertiesResource other = (PropertiesResource) o;
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
    return other instanceof PropertiesResource;
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
