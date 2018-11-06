/*
  Copyright (C) 2011 University of Washington
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
package org.opendatakit.common.persistence.engine.gae;

import java.util.HashSet;
import java.util.Set;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;

/**
 * Implementation of a result container that uses a Set to 
 * accumulate the distinct values returned from a Query.
 *
 * @author mitchellsundt@gmail.com
 *
 */
final class DistinctResultContainer implements ResultContainer {
  private final DataField dataField;
  private final Set<Object> uniqueValueSet = new HashSet<Object>();

  public DistinctResultContainer(DataField dataField) {
    this.dataField = dataField;
  }

  @Override
  public void add(CommonFieldsBase odkEntity) {
    switch (dataField.getDataType()) {
      case BINARY:
      case LONG_STRING:
        throw new IllegalStateException("unsupported fetch of binary data");
      case BOOLEAN:
        uniqueValueSet.add(odkEntity.getBooleanField(dataField));
        break;
      case DATETIME:
        uniqueValueSet.add(odkEntity.getDateField(dataField));
        break;
      case DECIMAL:
        uniqueValueSet.add(odkEntity.getNumericField(dataField));
        break;
      case INTEGER:
        uniqueValueSet.add(odkEntity.getLongField(dataField));
        break;
      case STRING:
      case URI:
        uniqueValueSet.add(odkEntity.getStringField(dataField));
        break;
    }
  }

  @Override
  public int size() {
    return uniqueValueSet.size();
  }

  public Set<Object> getValueSet() {
    return uniqueValueSet;
  }
}