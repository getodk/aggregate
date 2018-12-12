/*
  Copyright (C) 2010 University of Washington
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */
package org.opendatakit.common.persistence;

import java.util.List;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * The Query interface defines how persistence implementations should create query functionality.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public interface Query {

  void addSort(DataField attributeName, Direction direction);

  void addFilter(DataField attributeName, FilterOperation op, Object value);

  List<? extends CommonFieldsBase> executeQuery() throws ODKDatastoreException;

  QueryResult executeQuery(QueryResumePoint startCursor, int fetchLimit) throws ODKDatastoreException;

  List<?> executeDistinctValueForDataField(DataField dataField) throws ODKDatastoreException;

  enum Direction {
    ASCENDING,
    DESCENDING
  }

  enum FilterOperation {
    EQUAL,
    NOT_EQUAL,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL;
  }
}
