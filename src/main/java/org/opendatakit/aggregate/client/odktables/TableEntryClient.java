/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.odktables;

import java.io.Serializable;

/**
 * This is the client-side version of
 * org.opendatakit.aggregate.odktables.entity.TableEntry.java. <br>
 * The idea is that this is the client-side implementation of the same object.
 * Usual caveat that it is unclear yet whether this all is necessary and if
 * similar non-phone objects will be needed on the server side.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class TableEntryClient implements Serializable {

  /**
	 *
	 */
  private static final long serialVersionUID = -70945438534365403L;

  private String tableId;
  private String dataETag;
  private String schemaETag;

  protected TableEntryClient() {
  }

  public TableEntryClient(final String tableId, final String dataETag,
      final String schemaETag) {
    this.tableId = tableId;
    this.dataETag = dataETag;
    this.schemaETag = schemaETag;
  }

  public String getTableId() {
    return this.tableId;
  }

  public void setTableId(final String tableId) {
    this.tableId = tableId;
  }

  public String getDataETag() {
    return this.dataETag;
  }

  public void setDataETag(final String dataETag) {
    this.dataETag = dataETag;
  }

  public String getSchemaETag() {
    return schemaETag;
  }

  public void setSchemaETag(String schemaETag) {
    this.schemaETag = schemaETag;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TableEntryClient))
      return false;
    TableEntryClient other = (TableEntryClient) obj;

    if (dataETag == null) {
      if (other.dataETag != null) {
        return false;
      }
    } else if (!dataETag.equals(other.dataETag)) {
      return false;
    }

    if (schemaETag == null) {
      if (other.schemaETag != null) {
        return false;
      }
    } else if (!schemaETag.equals(other.schemaETag)) {
      return false;
    }

    if (tableId == null) {
      if (other.tableId != null) {
        return false;
      }
    } else if (!tableId.equals(other.tableId)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dataETag == null) ? 0 : dataETag.hashCode());
    result = prime * result + ((schemaETag == null) ? 0 : schemaETag.hashCode());
    result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "TableEntry [tableId=" + tableId + ", dataETag="
        + dataETag + ", schemaETag=" + schemaETag + "]";
  }
}