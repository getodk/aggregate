package org.opendatakit.aggregate.odktables.entity;

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
    return row;
  }

  /**
   * Construct a row for updating.
   * 
   * @param rowId
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

  public void setValues(final Map<String, String> values) {
    this.values = values;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (deleted ? 1231 : 1237);
    result = prime * result + ((rowEtag == null) ? 0 : rowEtag.hashCode());
    result = prime * result + ((rowId == null) ? 0 : rowId.hashCode());
    result = prime * result + ((values == null) ? 0 : values.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof Row))
      return false;
    Row other = (Row) obj;
    if (deleted != other.deleted)
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

  @Override
  public String toString() {
    return "Row [rowId=" + rowId + ", rowEtag=" + rowEtag + ", deleted=" + deleted + ", values="
        + values + "]";
  }
}