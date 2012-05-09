package org.opendatakit.aggregate.odktables.entity.api;

import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Root;

@Root
@Default(value = DefaultType.FIELD, required = true)
public class TableResource extends TableEntry {
  private String selfUri;
  private String propertiesUri;
  private String dataUri;
  private String diffUri;

  public TableResource(TableEntry entry) {
    super(entry.getTableId(), entry.getTableName(), entry.getDataEtag(), entry.getPropertiesEtag());
  }

  @SuppressWarnings("unused")
  private TableResource() {
  }

  public String getSelfUri() {
    return this.selfUri;
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

  public void setSelfUri(final String selfUri) {
    this.selfUri = selfUri;
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

  @Override
  public boolean equals(final Object o) {
    if (o == this)
      return true;
    if (!(o instanceof TableResource))
      return false;
    final TableResource other = (TableResource) o;
    if (!other.canEqual((Object) this))
      return false;
    if (!super.equals(o))
      return false;
    if (this.getSelfUri() == null ? other.getSelfUri() != null : !this.getSelfUri().equals(
        (Object) other.getSelfUri()))
      return false;
    if (this.getPropertiesUri() == null ? other.getPropertiesUri() != null : !this
        .getPropertiesUri().equals((Object) other.getPropertiesUri()))
      return false;
    if (this.getDataUri() == null ? other.getDataUri() != null : !this.getDataUri().equals(
        (Object) other.getDataUri()))
      return false;
    if (this.getDiffUri() == null ? other.getDiffUri() != null : !this.getDiffUri().equals(
        (Object) other.getDiffUri()))
      return false;
    return true;
  }

  public boolean canEqual(final Object other) {
    return other instanceof TableResource;
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = result * PRIME + super.hashCode();
    result = result * PRIME + (this.getSelfUri() == null ? 0 : this.getSelfUri().hashCode());
    result = result * PRIME
        + (this.getPropertiesUri() == null ? 0 : this.getPropertiesUri().hashCode());
    result = result * PRIME + (this.getDataUri() == null ? 0 : this.getDataUri().hashCode());
    result = result * PRIME + (this.getDiffUri() == null ? 0 : this.getDiffUri().hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "TableResource(super=" + super.toString() + ", selfUri=" + this.getSelfUri()
        + ", columnsUri=" + this.getPropertiesUri() + ", dataUri=" + this.getDataUri()
        + ", diffUri=" + this.getDiffUri() + ")";
  }
}