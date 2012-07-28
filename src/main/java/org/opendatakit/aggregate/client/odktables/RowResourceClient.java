package org.opendatakit.aggregate.client.odktables;

/**
 * This is the client-side version of RowResource. It is based heavily on 
 * org.opendatakit.aggregate.odktables.entity.api.RowResource.java.
 * <br>
 * The idea is that this will serve the same function as RowResource,
 * but for the client. 
 * @author sudars
 *
 */
public class RowResourceClient extends RowClient {

  private String selfUri;
  private String tableUri;

  @SuppressWarnings("unused")
  private RowResourceClient() {
  }

  public RowResourceClient(RowClient row) {
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
    if (!(o instanceof RowResourceClient))
      return false;
    final RowResourceClient other = (RowResourceClient) o;
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
    return other instanceof RowResourceClient;
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