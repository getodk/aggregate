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
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.constants.common.Visibility;

public final class RowFilter extends Filter implements Serializable {

  private static final long serialVersionUID = -482917672621588696L;
  private Column column;
  private FilterOperation operation;
  private String input;

  public RowFilter() {
    super();
  }

  public RowFilter(Visibility keepRemove, Column column, FilterOperation compare, String inputParam, Long ordinal) {
    super(keepRemove, RowOrCol.ROW, ordinal);
    this.operation = compare;
    this.input = inputParam;
    this.column = column;
  }

  public RowFilter(String uri) {
    super(uri);
  }

  public void resetUriToDefault() {
    uri = UIConsts.URI_DEFAULT;
  }

  public FilterOperation getOperation() {
    return operation;
  }

  public void setOperation(FilterOperation operation) {
    this.operation = operation;
  }

  public String getInput() {
    return input;
  }

  public void setInput(String input) {
    this.input = input;
  }

  public Column getColumn() {
    return this.column;
  }

  public void setColumn(Column column) {
    this.column = column;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RowFilter)) {
      return false;
    }

    if (!super.equals(obj)) {
      return false;
    }

    RowFilter other = (RowFilter) obj;
    return (column == null ? (other.column == null) : (column.equals(other.column)))
        && (input == null ? (other.input == null) : (input.equals(other.input)))
        && (operation == null ? (other.operation == null) : (operation.equals(other.operation)));
  }

  @Override
  public int hashCode() {
    int hashCode = 11;
    if (column != null)
      hashCode += column.hashCode();
    if (input != null)
      hashCode += input.hashCode();
    if (operation != null)
      hashCode += operation.hashCode();
    return hashCode;
  }

}
