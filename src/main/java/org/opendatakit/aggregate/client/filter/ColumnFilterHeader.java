/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;

import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.constants.common.UIConsts;

public class ColumnFilterHeader implements Serializable {

  private static final long serialVersionUID = -6420599052382340424L;

  private String uri; // unique identifier
  private Column column;

  public ColumnFilterHeader() {

  }

  public ColumnFilterHeader(Column column) {
    this.uri = UIConsts.URI_DEFAULT;
    this.column = column;
  }
  
  public ColumnFilterHeader(String displayHeader, String columnName, Long geopointCode) {
    this(new Column(displayHeader, columnName, geopointCode));
  }
  
  /**
   * This constructor should only be used by the server
   * 
   * @param uri
   * @param column
   */
  public ColumnFilterHeader(String uri, Column column) {
    this.uri = uri;
    this.column = column;
  }
  
  public String getUri() {
    return uri;
  }
  
  public Column getColumn() {
    return column;
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ColumnFilterHeader)) {
      return false;
    }
    
    
    ColumnFilterHeader other = (ColumnFilterHeader) obj;
    return (uri == null ? (other.uri == null) : (uri.equals(other.uri)))
        && (column == null ? (other.column == null) : (column.equals(other.column)));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 11;
    if (uri != null)
      hashCode += uri.hashCode();
    if(column != null)
      hashCode += column.hashCode();
    return hashCode;
  }
}
