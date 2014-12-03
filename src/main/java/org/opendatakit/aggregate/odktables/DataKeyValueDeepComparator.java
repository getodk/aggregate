/*
 * Copyright (C) 2014 University of Washington
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

package org.opendatakit.aggregate.odktables;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions.DbColumnDefinitionsEntity;
import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.ElementType;
import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;

public class DataKeyValueDeepComparator implements Comparator<DataKeyValue> {

  private final HashMap<String, ElementDataType> columnMap
    = new HashMap<String, ElementDataType>();
  
  DataKeyValueDeepComparator(List<DbColumnDefinitionsEntity> columns) {
    for ( DbColumnDefinitionsEntity column : columns ) {
      boolean hasChildren = !column.getArrayListChildElementKeys().isEmpty();
      String elementType = column.getElementType();
      ElementType e = ElementType.parseElementType(elementType, hasChildren);

      columnMap.put(column.getElementKey(), e.getDataType());
    }
  }
  
  @Override
  public int compare(DataKeyValue o1, DataKeyValue o2) {
    int cmp;

    // ensure we have DataKeyValue objects...
    if ( o1 == null ) {
      return (o2 == null) ? 0 : -1;
    }
    if ( o2 == null ) {
      return 1;
    }
    
    // ensure we have matching column names...
    
    if ( o1.column == null ) {
      // value is meaningless if no column name...
      return (o2.column == null) ? 0 : -1;
    }
    cmp = o1.column.compareTo(o2.column);
    if ( cmp != 0 ) {
      // column names do not match
      return cmp;
    }
    
    // ensure we have non-null values...
    if ( o1.value == null ) {
      return ( o2.value == null ) ? 0 : -1;
    }
    if ( o2.value == null ) {
      return 1;
    }
    
    cmp = o1.value.compareTo(o2.value);
    if ( cmp == 0 ) {
      // common case...
      return 0;
    }
    
    // Argh! we need to deal with possible numeric representation
    // problems...
    ElementDataType dt = columnMap.get(o1.column);

    if ( dt == ElementDataType.number ) {
      // special case of numeric data. 
      // !!Important!! Double.valueOf(str) handles NaN and +/-Infinity
      Double localNumber = Double.valueOf(o1.value);
      Double serverNumber = Double.valueOf(o2.value);
      
      if ( localNumber.equals(serverNumber) ) {
        // simple case -- trailing zeros or string representation mix-up
        // 
        return 0;
      } else if ( localNumber.isInfinite() && serverNumber.isInfinite() ) {
        // if they are both plus or both minus infinity, we have a match 
        if ( Math.signum(localNumber) == Math.signum(serverNumber) ) {
          return 0;
        } else {
          return (Math.signum(localNumber) < 0.0) ? -1 : 1;
        }
      } else if ( localNumber.isNaN() && serverNumber.isNaN() ) {
        // can't distinguish them...
        return 0;
      } else if ( localNumber.isNaN() ) {
        // NaN less than infinities...
        return -1;
      } else if ( serverNumber.isNaN() ) {
        return 1;
      } else if ( localNumber.isInfinite() ) {
        // infinity is high or low, based upon its sign
        return (Math.signum(localNumber) < 0.0) ? -1 : 1;
      } else if ( serverNumber.isInfinite() ) {
        return (Math.signum(serverNumber) < 0.0) ? 1 : -1;
      } else {
        double localDbl = localNumber;
        double serverDbl = serverNumber;
        if ( localDbl == serverDbl ) {
          // should handle two values like 9.80 and 9.8
          // but equals() earlier should also handle this.
          return 0;
        }
        // We have two values that may be like:
        //    9.8+epsilon and 9.8
        // consider them equal if they are within 2 steps of
        // each other. Deals with differing double-to-string
        // and string-to-double conversion libraries across
        // platforms and languages.
        double localNear = localDbl;
        int idist = 0;
        int idistMax = 2;
        for ( idist = 0 ; idist < idistMax ; ++idist ) {
          localNear = Math.nextAfter(localNear, serverDbl);
          if ( localNear == serverDbl ) {
            break;
          }
        }
        if ( idist < idistMax ) {
          return 0;
        }
        return localNumber.compareTo(serverNumber);
      }
    }
    // otherwise, return whatever the string compare gave...  
    return cmp;
  }

}
