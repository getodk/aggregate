package org.opendatakit.aggregate.odktables.entity.api;

import javax.xml.bind.annotation.XmlRootElement;

import org.opendatakit.aggregate.odktables.entity.TableEntry;

@XmlRootElement
public class TableResource extends TableEntry {
  private String selfUri;
  private String columnsUri;
  private String dataUri;
  private String diffUri;

  public TableResource(TableEntry entry) {
    super(entry.getTableId(), entry.getDataEtag());
  }

  @java.lang.SuppressWarnings("all")
  public TableResource() {
  }

  @java.lang.SuppressWarnings("all")
  public String getSelfUri() {
    return this.selfUri;
  }

  @java.lang.SuppressWarnings("all")
  public String getColumnsUri() {
    return this.columnsUri;
  }

  @java.lang.SuppressWarnings("all")
  public String getDataUri() {
    return this.dataUri;
  }

  public String getDiffUri() {
    return this.diffUri;
  }

  @java.lang.SuppressWarnings("all")
  public void setSelfUri(final String selfUri) {
    this.selfUri = selfUri;
  }

  @java.lang.SuppressWarnings("all")
  public void setColumnsUri(final String columnsUri) {
    this.columnsUri = columnsUri;
  }

  @java.lang.SuppressWarnings("all")
  public void setDataUri(final String dataUri) {
    this.dataUri = dataUri;
  }

  public void setDiffUri(String diffUri) {
    this.diffUri = diffUri;
  }

  @java.lang.Override
  @java.lang.SuppressWarnings("all")
  public java.lang.String toString() {
    return "TableResource(super=" + super.toString() + ", selfUri=" + this.getSelfUri()
        + ", columnsUri=" + this.getColumnsUri() + ", dataUri=" + this.getDataUri() + ", diffUri="
        + this.getDiffUri() + ")";
  }

  @java.lang.Override
  @java.lang.SuppressWarnings("all")
  public boolean equals(final java.lang.Object o) {
    if (o == this)
      return true;
    if (!(o instanceof TableResource))
      return false;
    final TableResource other = (TableResource) o;
    if (!other.canEqual((java.lang.Object) this))
      return false;
    if (!super.equals(o))
      return false;
    if (this.getSelfUri() == null ? other.getSelfUri() != null : !this.getSelfUri().equals(
        (java.lang.Object) other.getSelfUri()))
      return false;
    if (this.getColumnsUri() == null ? other.getColumnsUri() != null : !this.getColumnsUri()
        .equals((java.lang.Object) other.getColumnsUri()))
      return false;
    if (this.getDataUri() == null ? other.getDataUri() != null : !this.getDataUri().equals(
        (java.lang.Object) other.getDataUri()))
      return false;
    if (this.getDiffUri() == null ? other.getDiffUri() != null : !this.getDiffUri().equals(
        (java.lang.Object) other.getDiffUri()))
      return false;
    return true;
  }

  @java.lang.SuppressWarnings("all")
  public boolean canEqual(final java.lang.Object other) {
    return other instanceof TableResource;
  }

  @java.lang.Override
  @java.lang.SuppressWarnings("all")
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = result * PRIME + super.hashCode();
    result = result * PRIME + (this.getSelfUri() == null ? 0 : this.getSelfUri().hashCode());
    result = result * PRIME + (this.getColumnsUri() == null ? 0 : this.getColumnsUri().hashCode());
    result = result * PRIME + (this.getDataUri() == null ? 0 : this.getDataUri().hashCode());
    result = result * PRIME + (this.getDiffUri() == null ? 0 : this.getDiffUri().hashCode());
    return result;
  }
}