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

import java.util.ArrayList;
import java.util.Date;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.WrappedBigDecimal;

/**
 * Track the attributes that we are querying and sorting on...
 *
 * @author mitchellsundt@gmail.com
 *
 */
abstract class Tracker {
  final DataField attribute;

  Tracker(DataField attribute) {
    this.attribute = attribute;
  }

  DataField getAttribute() {
    return attribute;
  }

  public int simpleValueCompare(CommonFieldsBase b1, CommonFieldsBase b2) {
    Object value;
    switch (attribute.getDataType()) {
    default:
      throw new IllegalStateException("missing dataType implementation");
    case BINARY:
    case LONG_STRING:
      throw new IllegalStateException("should never filter on large objects (text or blob)");
    case STRING:
    case URI:
      value = b2.getStringField(attribute);
      break;
    case INTEGER:
      value = b2.getLongField(attribute);
      break;
    case DECIMAL:
      value = b2.getNumericField(attribute);
      break;
    case BOOLEAN:
      value = b2.getBooleanField(attribute);
      break;
    case DATETIME:
      value = b2.getDateField(attribute);
      break;
    }
    return compareField(b1, value);
  }

  <T extends Comparable<T>> int compareObjects(T b1, T b2) {
    if (b1 == null) {
      if (b2 == null)
        return 0;
      return 1; // nulls (==b2) appear last in ordering
    }
    if (b2 == null)
      return -1; // nulls (==b1) appear last in ordering
    return b1.compareTo(b2);
  }

  int compareField(CommonFieldsBase record, Object value) {
    switch (attribute.getDataType()) {
    default:
      throw new IllegalStateException("missing dataType implementation");
    case BINARY:
    case LONG_STRING:
      throw new IllegalStateException("should never filter on large objects (text or blob)");
    case STRING:
    case URI:
      String eStr = record.getStringField(attribute);
      String vStr = (value == null) ? null : (String) value;
      return compareObjects(eStr, vStr);
    case INTEGER:
      Long eLong = record.getLongField(attribute);
      Long vLong;
      if (value == null) {
        vLong = null;
      } else if (value instanceof Long) {
        vLong = (Long) value;
      } else {
        vLong = Long.valueOf(value.toString());
      }
      return compareObjects(eLong, vLong);
    case DECIMAL:
      WrappedBigDecimal eDec = record.getNumericField(attribute);
      WrappedBigDecimal vDec;
      if (value == null) {
        vDec = null;
      } else if ( value instanceof WrappedBigDecimal ) {
        vDec = (WrappedBigDecimal) value;
      } else {
        vDec = new WrappedBigDecimal(value.toString());
      }
      return compareObjects(eDec, vDec);
    case BOOLEAN:
      Boolean eBool = record.getBooleanField(attribute);
      Boolean vBool = (value == null) ? null : (Boolean) value;
      return compareObjects(eBool, vBool);
    case DATETIME:
      Date eDate = record.getDateField(attribute);
      Date vDate = (value == null) ? null : (Date) value;
      return compareObjects(eDate, vDate);
    }
  }

  abstract boolean passFilter(CommonFieldsBase record);

  abstract void setFilter(ArrayList<com.google.appengine.api.datastore.Query.Filter> filters);
}