package org.opendatakit.aggregate.odktables.entity;

import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

@Root
public class Row {

  @Element(name = "id", required = false)
  private String rowId;

  @Element(name = "etag", required = false)
  private String rowEtag;

  @Element(required = false)
  private boolean deleted;

  @Element(required = false)
  private String createUser;

  @Element(required = false)
  private String lastUpdateUser;

  @Element(required = false)
  private Scope filterScope;

  @ElementMap(entry = "entry", key = "column", attribute = true, inline = true)
  private Map<String, String> values;

  /**
   * Construct a row for insertion.
   * 
   * @param rowId
   * @param values
   */
  public static Row forInsert(String rowId, Map<String, String> values) {
    Row row = new Row();
    row.rowId = rowId;
    row.values = values;
    row.filterScope = Scope.EMPTY_SCOPE;
    return row;
  }

  /**
   * Construct a row for updating.
   * 
   * @param rowId
   * @param rowEtag
   * @param values
   */
  public static Row forUpdate(String rowId, String rowEtag, Map<String, String> values) {
    Row row = new Row();
    row.rowId = rowId;
    row.rowEtag = rowEtag;
    row.values = values;
    return row;
  }

  public Row() {
    this.rowId = null;
    this.rowEtag = null;
    this.deleted = false;
    this.createUser = null;
    this.lastUpdateUser = null;
    this.filterScope = null;
    this.values = new HashMap<String, String>();
  }

  public String getRowId() {
    return this.rowId;
  }

  public String getRowEtag() {
    return this.rowEtag;
  }

  public boolean isDeleted() {
    return this.deleted;
  }

  public String getCreateUser() {
    return createUser;
  }

  public String getLastUpdateUser() {
    return lastUpdateUser;
  }

  public Scope getFilterScope() {
    return filterScope;
  }

  public Map<String, String> getValues() {
    return this.values;
  }

  public void setRowId(final String rowId) {
    this.rowId = rowId;
  }

  public void setRowEtag(final String rowEtag) {
    this.rowEtag = rowEtag;
  }

  public void setDeleted(final boolean deleted) {
    this.deleted = deleted;
  }

  public void setCreateUser(String createUser) {
    this.createUser = createUser;
  }

  public void setLastUpdateUser(String lastUpdateUser) {
    this.lastUpdateUser = lastUpdateUser;
  }

  public void setFilterScope(Scope filterScope) {
    this.filterScope = filterScope;
  }

  public void setValues(final Map<String, String> values) {
    this.values = values;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((createUser == null) ? 0 : createUser.hashCode());
    result = prime * result + (deleted ? 1231 : 1237);
    result = prime * result + ((filterScope == null) ? 0 : filterScope.hashCode());
    result = prime * result + ((lastUpdateUser == null) ? 0 : lastUpdateUser.hashCode());
    result = prime * result + ((rowEtag == null) ? 0 : rowEtag.hashCode());
    result = prime * result + ((rowId == null) ? 0 : rowId.hashCode());
    result = prime * result + ((values == null) ? 0 : values.hashCode());
    return result;
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
    if (obj == null)
      return false;
    if (!(obj instanceof Row))
      return false;
    Row other = (Row) obj;
    if (createUser == null) {
      if (other.createUser != null)
        return false;
    } else if (!createUser.equals(other.createUser))
      return false;
    if (deleted != other.deleted)
      return false;
    if (filterScope == null) {
      if (other.filterScope != null)
        return false;
    } else if (!filterScope.equals(other.filterScope))
      return false;
    if (lastUpdateUser == null) {
      if (other.lastUpdateUser != null)
        return false;
    } else if (!lastUpdateUser.equals(other.lastUpdateUser))
      return false;
    if (rowEtag == null) {
      if (other.rowEtag != null)
        return false;
    } else if (!rowEtag.equals(other.rowEtag))
      return false;
    if (rowId == null) {
      if (other.rowId != null)
        return false;
    } else if (!rowId.equals(other.rowId))
      return false;
    if (values == null) {
      if (other.values != null)
        return false;
    } else if (!values.equals(other.values))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Row [rowId=");
    builder.append(rowId);
    builder.append(", rowEtag=");
    builder.append(rowEtag);
    builder.append(", deleted=");
    builder.append(deleted);
    builder.append(", createUser=");
    builder.append(createUser);
    builder.append(", lastUpdateUser=");
    builder.append(lastUpdateUser);
    builder.append(", filterScope=");
    builder.append(filterScope);
    builder.append(", values=");
    builder.append(values);
    builder.append("]");
    return builder.toString();
  }
}