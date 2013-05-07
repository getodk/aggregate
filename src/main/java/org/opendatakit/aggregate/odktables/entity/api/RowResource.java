package org.opendatakit.aggregate.odktables.entity.api;

import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.RowResourceClient;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Root;

@Root
@Default(DefaultType.FIELD)
public class RowResource extends Row {

  private String selfUri;
  private String tableUri;

  @SuppressWarnings("unused")
  private RowResource() {
  }

  public RowResource(Row row) {
    super();
    setRowId(row.getRowId());
    setRowEtag(row.getRowEtag());
    setDeleted(row.isDeleted());
    setCreateUser(row.getCreateUser());
    setLastUpdateUser(row.getLastUpdateUser());
    setFilterScope(row.getFilterScope());
    setValues(row.getValues());
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
    if (!(o instanceof RowResource))
      return false;
    final RowResource other = (RowResource) o;
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
    return other instanceof RowResource;
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
    return "RowResource(super=" + super.toString() + ", selfUri=" + this.getSelfUri()
        + ", tableUri=" + this.getTableUri() + ")";
  }

}