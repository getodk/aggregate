/*
 * Copyright (C) 2012-2013 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.odktables.rest.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Row {

  @Element(name = "id", required = false)
  private String rowId;

  @Element(name = "ETag", required = false)
  private String rowETag;

  @Element(name = "dataETagAtModification", required = false)
  private String dataETagAtModification;

  @Element(required = false)
  private boolean deleted;

  @Element(required = false)
  private String createUser;

  @Element(required = false)
  private String lastUpdateUser;

  @Element(required = false)
  private Scope filterScope;

  /**
   * OdkTables metadata column.
   */
  @Element(required = false)
  private String uriAccessControl;

  /**
   * OdkTables metadata column.
   */
  @Element(required = false)
  private String formId;

  /**
   * OdkTables metadata column.
   */
  @Element(required = false)
  private String locale;

  /**
   * OdkTables metadata column.
   */
  @Element(required = false)
  private Long savepointTimestamp;

  @ElementMap(entry = "entry", key = "column", attribute = true, inline = true)
  private Map<String, String> values;

  /**
   * Construct a row for insertion. This is used by the remote client (ODK
   * Tables) to construct a REST request to insert the row.
   *
   * @param rowId
   * @param values
   */
  public static Row forInsert(String rowId, String uriAccessControl, String formId, String locale,
      Long savepointTimestamp, Map<String, String> values) {
    Row row = new Row();
    row.rowId = rowId;
    row.uriAccessControl = uriAccessControl;
    row.formId = formId;
    row.locale = locale;
    row.savepointTimestamp = savepointTimestamp;
    row.values = values;
    return row;
  }

  /**
   * Construct a row for updating. This is used by the remote client (ODK
   * Tables) to construct a REST request to modify the row.
   *
   * @param rowId
   * @param rowETag
   * @param values
   */
  public static Row forUpdate(String rowId, String rowETag, String uriAccessControl, String formId,
      String locale, Long savepointTimestamp, Map<String, String> values) {
    Row row = new Row();
    row.rowId = rowId;
    row.rowETag = rowETag;
    row.uriAccessControl = uriAccessControl;
    row.formId = formId;
    row.locale = locale;
    row.savepointTimestamp = savepointTimestamp;
    row.values = values;
    return row;
  }

  public Row() {
    this.rowId = null;
    this.rowETag = null;
    this.dataETagAtModification = null;
    this.deleted = false;
    this.createUser = null;
    this.lastUpdateUser = null;
    this.filterScope = null;
    // data coming up from client
    this.uriAccessControl = null;
    this.formId = null;
    this.locale = null;
    this.savepointTimestamp = null;
    this.values = new HashMap<String, String>();
  }

  public String getRowId() {
    return this.rowId;
  }

  public String getRowETag() {
    return this.rowETag;
  }

  public String getDataETagAtModification() {
    return this.dataETagAtModification;
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

  public String getUriAccessControl() {
    return this.uriAccessControl;
  }

  public String getFormId() {
    return this.formId;
  }

  public String getLocale() {
    return this.locale;
  }

  public Long getSavepointTimestamp() {
    return this.savepointTimestamp;
  }

  public Map<String, String> getValues() {
    return this.values;
  }

  public void setRowId(final String rowId) {
    this.rowId = rowId;
  }

  public void setRowETag(final String rowETag) {
    this.rowETag = rowETag;
  }

  public void setDataETagAtModification(final String dataETagAtModification) {
    this.dataETagAtModification = dataETagAtModification;
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

  public void setUriAccessControl(String uriAccessControl) {
    this.uriAccessControl = uriAccessControl;
  }

  public void setFormId(String formId) {
    this.formId = formId;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public void setValues(final Map<String, String> values) {
    this.values = values;
  }

  /**
   * Expects a string as generated by
   * {@link org.opendatakit.common.utils.WebUtils#iso8601Date(Date)}.
   *
   * @param timestamp
   */
  public void setSavepointTimestamp(Long savepointTimestamp) {
    this.savepointTimestamp = savepointTimestamp;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((rowId == null) ? 0 : rowId.hashCode());
    result = prime * result + ((rowETag == null) ? 0 : rowETag.hashCode());
    result = prime * result
        + ((dataETagAtModification == null) ? 0 : dataETagAtModification.hashCode());
    result = prime * result + ((deleted) ? 0 : 1);
    result = prime * result + ((createUser == null) ? 0 : createUser.hashCode());
    result = prime * result + ((lastUpdateUser == null) ? 0 : lastUpdateUser.hashCode());
    result = prime * result + ((filterScope == null) ? 0 : filterScope.hashCode());
    result = prime * result + ((uriAccessControl == null) ? 0 : uriAccessControl.hashCode());
    result = prime * result + ((formId == null) ? 0 : formId.hashCode());
    result = prime * result + ((locale == null) ? 0 : locale.hashCode());
    result = prime * result + ((savepointTimestamp == null) ? 0 : savepointTimestamp.hashCode());
    result = prime * result + ((values == null) ? 0 : values.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Row)) {
      return false;
    }
    Row other = (Row) obj;
    return (rowId == null ? other.rowId == null : rowId.equals(other.rowId))
        && (rowETag == null ? other.rowETag == null : rowETag.equals(other.rowETag))
        && (dataETagAtModification == null ? other.dataETagAtModification == null
            : dataETagAtModification.equals(dataETagAtModification))
        && (deleted == other.deleted)
        && (createUser == null ? other.createUser == null : createUser.equals(other.createUser))
        && (lastUpdateUser == null ? other.lastUpdateUser == null : lastUpdateUser
            .equals(other.lastUpdateUser))
        && (filterScope == null ? other.filterScope == null : filterScope.equals(other.filterScope))
        && (uriAccessControl == null ? other.uriAccessControl == null : uriAccessControl
            .equals(other.uriAccessControl))
        && (formId == null ? other.formId == null : formId.equals(other.formId))
        && (locale == null ? other.locale == null : locale.equals(other.locale))
        && (savepointTimestamp == null ? other.savepointTimestamp == null : savepointTimestamp
            .equals(other.savepointTimestamp))
        && (values == null ? other.values == null : values.equals(other.values));
  }

  /**
   * A reduced-function equals predicate to detect if the values significant to
   * the end user are matched across the two data records.
   * <p>
   * Ignore the following fields when making this comparison:
   * <ul><li>rowETag</li>
   * <li>dataETagAtModification</li>
   * <li>createUser</li>
   * <li>lastUpdateUser</li>
   * </ul>
   *
   * @param other
   * @return
   */
  public boolean hasMatchingSignificantFieldValues(Row other) {
    if ( other == null ) return false;
    return (rowId == null ? other.rowId == null : rowId.equals(other.rowId))
        && (deleted == other.deleted)
        && (filterScope == null ? other.filterScope == null : filterScope.equals(other.filterScope))
        && (uriAccessControl == null ? other.uriAccessControl == null : uriAccessControl
            .equals(other.uriAccessControl))
        && (formId == null ? other.formId == null : formId.equals(other.formId))
        && (locale == null ? other.locale == null : locale.equals(other.locale))
        && (savepointTimestamp == null ? other.savepointTimestamp == null : savepointTimestamp
            .equals(other.savepointTimestamp))
        && (values == null ? other.values == null : values.equals(other.values));
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
    builder.append(", rowETag=");
    builder.append(rowETag);
    builder.append(", dataETagAtModification=");
    builder.append(dataETagAtModification);
    builder.append(", deleted=");
    builder.append(deleted);
    builder.append(", createUser=");
    builder.append(createUser);
    builder.append(", lastUpdateUser=");
    builder.append(lastUpdateUser);
    builder.append(", filterScope=");
    builder.append(filterScope);
    builder.append(", uriAccessControl=");
    builder.append(uriAccessControl);
    builder.append(", formId=");
    builder.append(formId);
    builder.append(", locale=");
    builder.append(locale);
    builder.append(", savepointTimestamp=");
    builder.append(savepointTimestamp);
    builder.append(", values=");
    builder.append(values);
    builder.append("]");
    return builder.toString();
  }
}