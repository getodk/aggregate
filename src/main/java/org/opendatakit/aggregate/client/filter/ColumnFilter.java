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
import java.util.ArrayList;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.constants.common.Visibility;

public final class ColumnFilter extends Filter implements Serializable {

  /**
   * Id for Serialization
   */
  private static final long serialVersionUID = -1045936241685471645L;

  private ArrayList<Column> columns;

  public ColumnFilter() {
    super();
  }

  public ColumnFilter(Visibility keepRemove, ArrayList<Column> columns, Long ordinal) {
    super(keepRemove, RowOrCol.COLUMN, ordinal);
    this.columns = columns;
  }

  /**
   * This constructor should only be used by the server
   *
   * @param uri
   */
  public ColumnFilter(String uri) {
    super(uri);
    this.columns = new ArrayList<Column>();
  }

  /**
   * Used to clear the URI in the elements so it can be Saved As properly in the
   * server, as the server creates a new entity when uri is set to URI_DEFAULT
   */
  public void resetUriToDefault() {
    uri = UIConsts.URI_DEFAULT;
    for (Column col : columns) {
      col.resetUriToDefault();
    }
  }

  public ArrayList<Column> getColumnFilterHeaders() {
    return columns;
  }

  public void setColumnFilterHeaders(ArrayList<Column> columns) {
    this.columns = columns;
  }

  public void addColumnFilterHeader(Column column) {
    this.columns.add(column);
  }


  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ColumnFilter)) {
      return false;
    }

    if (!super.equals(obj)) {
      return false;
    }

    ColumnFilter other = (ColumnFilter) obj;
    return (columns == null ? (other.columns == null) : (columns.equals(other.columns)));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 11;
    if (columns != null)
      hashCode += columns.hashCode();
    return hashCode;
  }
}
