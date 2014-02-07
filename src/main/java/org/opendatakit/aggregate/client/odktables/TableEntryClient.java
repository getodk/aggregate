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
 * org.opendatakit.aggregate.odktables.entity.TableEntry.java.
 * <br>
 * The idea is that this is the client-side implementation of the same
 * object. Usual caveat that it is unclear yet whether this all is
 * necessary and if similar non-phone objects will be needed on the
 * server side.
 * @author sudar.sam@gmail.com
 *
 */
public class TableEntryClient implements Serializable {

  /**
	 *
	 */
	private static final long serialVersionUID = -7094543853434685403L;


  private String tableId;
  private String displayName;
  private String dataEtag;
  private String propertiesEtag;
  private String schemaEtag;

  protected TableEntryClient() {
  }

  public TableEntryClient(final String tableId, final String displayName,
      final String dataEtag, final String propertiesEtag, final String schemaEtag) {
    this.tableId = tableId;
    this.displayName = displayName;
    this.dataEtag = dataEtag;
    this.propertiesEtag = propertiesEtag;
    this.schemaEtag = schemaEtag;
  }

  public String getTableId() {
    return this.tableId;
  }

  public void setTableId(final String tableId) {
    this.tableId = tableId;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public void setDisplayName(final String displayName) {
    this.displayName = displayName;
  }

  public String getDataEtag() {
    return this.dataEtag;
  }

  public void setDataEtag(final String dataEtag) {
    this.dataEtag = dataEtag;
  }

  public String getPropertiesEtag() {
    return propertiesEtag;
  }

  public void setPropertiesEtag(String propertiesEtag) {
    this.propertiesEtag = propertiesEtag;
  }

  public String getSchemaEtag() {
    return schemaEtag;
  }

  public void setSchemaEtag(String schemaEtag) {
    this.schemaEtag = schemaEtag;
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

    if (dataEtag == null) {
      if (other.dataEtag != null) {
        return false;
      }
    } else if (!dataEtag.equals(other.dataEtag)) {
      return false;
    }

    if (propertiesEtag == null) {
      if (other.propertiesEtag != null) {
        return false;
      }
    } else if (!propertiesEtag.equals(other.propertiesEtag)) {
      return false;
    }

    if (schemaEtag == null) {
      if (other.schemaEtag != null) {
        return false;
      }
    } else if (!schemaEtag.equals(other.schemaEtag)) {
      return false;
    }

    if (tableId == null) {
      if (other.tableId != null) {
        return false;
      }
    } else if (!tableId.equals(other.tableId)) {
      return false;
    }

    if (displayName == null) {
      if (other.displayName != null) {
        return false;
      }
    } else if (!displayName.equals(other.displayName)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dataEtag == null) ? 0 : dataEtag.hashCode());
    result = prime * result + ((propertiesEtag == null) ? 0 : propertiesEtag.hashCode());
    result = prime * result + ((schemaEtag == null) ? 0 : schemaEtag.hashCode());
    result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
    result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "TableEntry [tableId=" + tableId
        + ", displayName=" + displayName
        + ", dataEtag=" + dataEtag
        + ", propertiesEtag=" + propertiesEtag
        + ", schemaEtag=" + schemaEtag + "]";
  }
}