/**
 * Copyright (C) 2011 University of Washington
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence.engine.gae;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;

/**
 * Tracks the value-set ( 'IN' ) clause filters to apply to a result set.
 *
 * @author mitchellsundt@gmail.com
 *
 */
final class ValueSetFilterTracker extends Tracker {
  final Collection<?> valueSet;

  ValueSetFilterTracker(DataField attribute, Collection<?> valueSet) {
    super(attribute);
    this.valueSet = valueSet;
  }

  @Override
  boolean passFilter(CommonFieldsBase record) {
    for (Object o : valueSet) {
      int result = compareField(record, o);
      if (result == 0)
        return true;
    }
    return false;
  }

  @Override
  void setFilter(ArrayList<com.google.appengine.api.datastore.Query.Filter> filters) {
    if (attribute.getDataType() == DataType.DECIMAL) {
      Set<Double> dvSet = new HashSet<Double>();
      for (Object value : valueSet) {
        Double d = null;
        if (value != null) {
          BigDecimal bd = (BigDecimal) value;
          d = bd.doubleValue();
        }
        dvSet.add(d);
      }
      filters.add(new FilterPredicate(attribute.getName(), FilterOperator.IN, dvSet));
    } else {
      filters.add(new FilterPredicate(attribute.getName(), FilterOperator.IN, valueSet));
    }
  }
}