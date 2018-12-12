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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Query.Direction;

/**
 * Defines the sort order for an attribute that should be applied to the result set.
 *
 * @author mitchellsundt@gmail.com
 */
final class SortTracker extends Tracker implements Comparator<CommonFieldsBase> {
  final Direction direction;

  SortTracker(DataField attribute, Direction direction) {
    super(attribute);
    this.direction = direction;
  }

  @Override
  boolean passFilter(CommonFieldsBase record) {
    throw new IllegalStateException("not implemented");
  }

  @Override
  void setFilter(ArrayList<com.google.appengine.api.datastore.Query.Filter> filters) {
    throw new IllegalStateException("not implemented");
  }

  @Override
  public int compare(CommonFieldsBase o1, CommonFieldsBase o2) {
    if (direction == Direction.ASCENDING) {
      return simpleValueCompare(o1, o2);
    } else {
      return -simpleValueCompare(o1, o2);
    }
  }

  public Comparator<Object> getComparator() {
    return new Comparator<Object>() {
      @Override
      public int compare(Object o1, Object o2) {
        int sense = (direction == Direction.ASCENDING) ? 1 : -1;
        switch (attribute.getDataType()) {
          default:
            throw new IllegalStateException("missing dataType implementation");
          case BINARY:
          case LONG_STRING:
            throw new IllegalStateException("should never filter on large objects (text or blob)");
          case STRING:
          case URI:
            String s1 = (o1 == null) ? null : (String) o1;
            String s2 = (o2 == null) ? null : (String) o2;
            return sense * compareObjects(s1, s2);
          case INTEGER:
            Long l1 = (o1 == null) ? null : (Long) o1;
            Long l2 = (o2 == null) ? null : (Long) o2;
            return sense * compareObjects(l1, l2);
          case DECIMAL:
            BigDecimal bd1 = (o1 == null) ? null : (BigDecimal) o1;
            BigDecimal bd2 = (o2 == null) ? null : (BigDecimal) o2;
            return sense * compareObjects(bd1, bd2);
          case BOOLEAN:
            Boolean b1 = (o1 == null) ? null : (Boolean) o1;
            Boolean b2 = (o2 == null) ? null : (Boolean) o2;
            return sense * compareObjects(b1, b2);
          case DATETIME:
            Date d1 = (o1 == null) ? null : (Date) o1;
            Date d2 = (o2 == null) ? null : (Date) o2;
            return sense * compareObjects(d1, d2);
        }
      }
    };
  }
}