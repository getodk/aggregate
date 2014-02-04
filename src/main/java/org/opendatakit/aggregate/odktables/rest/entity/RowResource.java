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

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
@Default(DefaultType.FIELD)
public class RowResource extends Row {

  @Element(required = true)
  private String selfUri;

  @Element(required = true)
  private String tableUri;

  @SuppressWarnings("unused")
  private RowResource() {
  }

  public RowResource(Row row) {
    super();
    setRowId(row.getRowId());
    setRowETag(row.getRowETag());
    setDataETagAtModification(row.getDataETagAtModification());
    setDeleted(row.isDeleted());
    setCreateUser(row.getCreateUser());
    setLastUpdateUser(row.getLastUpdateUser());
    setFilterScope(row.getFilterScope());
    // sync'd metadata
    setFormId(row.getFormId());
    setLocale(row.getLocale());
    setSavepointTimestamp(row.getSavepointTimestamp());
    setSavepointCreator(row.getSavepointCreator());
    // data
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