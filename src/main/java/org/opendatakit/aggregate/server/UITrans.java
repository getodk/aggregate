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

package org.opendatakit.aggregate.server;

import java.util.HashMap;
import java.util.Map;

import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.common.persistence.Query;

/**
 * Translate enumerations between the UI layer and the rest of Aggregate
 * 
 * @author wbrunette@gmail.com
 *
 */
public final class UITrans {

  private static Map<Query.FilterOperation, FilterOperation> toClient = new HashMap<Query.FilterOperation, FilterOperation>();
  private static Map<FilterOperation, Query.FilterOperation> toPersistence = new HashMap<FilterOperation, Query.FilterOperation>();

  static {
    toClient.put(Query.FilterOperation.EQUAL, FilterOperation.EQUAL);
    toClient.put(Query.FilterOperation.NOT_EQUAL, FilterOperation.NOT_EQUAL);
    toClient.put(Query.FilterOperation.GREATER_THAN, FilterOperation.GREATER_THAN);
    toClient.put(Query.FilterOperation.GREATER_THAN_OR_EQUAL, FilterOperation.GREATER_THAN_OR_EQUAL);
    toClient.put(Query.FilterOperation.LESS_THAN, FilterOperation.LESS_THAN);
    toClient.put(Query.FilterOperation.LESS_THAN_OR_EQUAL, FilterOperation.LESS_THAN_OR_EQUAL);
    
    toPersistence.put(FilterOperation.EQUAL, Query.FilterOperation.EQUAL);
    toPersistence.put(FilterOperation.NOT_EQUAL, Query.FilterOperation.NOT_EQUAL);
    toPersistence.put(FilterOperation.GREATER_THAN, Query.FilterOperation.GREATER_THAN);
    toPersistence.put(FilterOperation.GREATER_THAN_OR_EQUAL, Query.FilterOperation.GREATER_THAN_OR_EQUAL);
    toPersistence.put(FilterOperation.LESS_THAN, Query.FilterOperation.LESS_THAN);
    toPersistence.put(FilterOperation.LESS_THAN_OR_EQUAL, Query.FilterOperation.LESS_THAN_OR_EQUAL);
  }
  
  public static final Query.FilterOperation convertFilterOperation(FilterOperation op) {
    return toPersistence.get(op);
  }
  
  public static final FilterOperation convertFilterOperation(Query.FilterOperation op) {
    return toClient.get(op);
  }
}
