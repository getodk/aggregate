/**
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence.engine.gae;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.Query.FilterOperation;

import com.google.appengine.api.datastore.Query.FilterPredicate;

/**
 * Tracks the simple comparison filters to apply to the result set.
 *
 * @author mitchellsundt@gmail.com
 *
 */
final class SimpleFilterTracker extends Tracker {
  final FilterOperation op;
  final Object value;

  boolean isEqualityTest() {
    return op == FilterOperation.EQUAL;
  }

  SimpleFilterTracker(DataField attribute, FilterOperation op, Object value) {
    super(attribute);
    this.op = op;
    this.value = value;
  }

  @Override
  boolean passFilter(CommonFieldsBase record) {
    int result = compareField(record, value);
    switch (op) {
    case EQUAL:
      return result == 0;
    case NOT_EQUAL:
      return result != 0;
    case LESS_THAN:
      return result < 0;
    case LESS_THAN_OR_EQUAL:
      return result <= 0;
    case GREATER_THAN:
      return result > 0;
    case GREATER_THAN_OR_EQUAL:
      return result >= 0;
    default:
      throw new IllegalStateException("missing a filter operation!");
    }
  }

  @Override
  void setFilter(ArrayList<com.google.appengine.api.datastore.Query.Filter> filters) {
    if (attribute.getDataType() == DataType.DECIMAL) {
      Double d = null;
      if (value != null) {
        if ( value instanceof WrappedBigDecimal ) {
          WrappedBigDecimal wbd = (WrappedBigDecimal) value;
          if ( wbd.isSpecialValue() ) {
            d = wbd.d;
          } else {
            d = wbd.doubleValue();
          }
        } else if ( value instanceof BigDecimal ) {
          BigDecimal bd = (BigDecimal) value;
          d = bd.doubleValue();
        } else if ( value instanceof Double ) {
          d = (Double) value;
        } else {
          d = Double.valueOf(value.toString());
        }
      }
      filters.add(new FilterPredicate(attribute.getName(), QueryImpl.operationMap.get(op), d));
    } else {
      filters.add(new FilterPredicate(attribute.getName(), QueryImpl.operationMap.get(op), value));
    }
  }
}