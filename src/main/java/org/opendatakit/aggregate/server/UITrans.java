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
import org.opendatakit.aggregate.constants.common.Visibility;
import org.opendatakit.common.persistence.Query;

/**
 * Translate enumerations between the UI layer and the rest of Aggregate
 *
 * @author wbrunette@gmail.com
 */
public final class UITrans {

  private static Map<FilterOperation, Query.FilterOperation> toPersistenceDisplay = new HashMap<FilterOperation, Query.FilterOperation>();
  private static Map<FilterOperation, Query.FilterOperation> toPersistenceHide = new HashMap<FilterOperation, Query.FilterOperation>();

  static {
    toPersistenceDisplay.put(FilterOperation.EQUAL, Query.FilterOperation.EQUAL);
    toPersistenceDisplay.put(FilterOperation.NOT_EQUAL, Query.FilterOperation.NOT_EQUAL);
    toPersistenceDisplay.put(FilterOperation.GREATER_THAN, Query.FilterOperation.GREATER_THAN);
    toPersistenceDisplay.put(FilterOperation.GREATER_THAN_OR_EQUAL,
        Query.FilterOperation.GREATER_THAN_OR_EQUAL);
    toPersistenceDisplay.put(FilterOperation.LESS_THAN, Query.FilterOperation.LESS_THAN);
    toPersistenceDisplay.put(FilterOperation.LESS_THAN_OR_EQUAL,
        Query.FilterOperation.LESS_THAN_OR_EQUAL);

    toPersistenceHide.put(FilterOperation.EQUAL, Query.FilterOperation.NOT_EQUAL);
    toPersistenceHide.put(FilterOperation.NOT_EQUAL, Query.FilterOperation.EQUAL);
    toPersistenceHide.put(FilterOperation.GREATER_THAN, Query.FilterOperation.LESS_THAN_OR_EQUAL);
    toPersistenceHide.put(FilterOperation.GREATER_THAN_OR_EQUAL, Query.FilterOperation.LESS_THAN);
    toPersistenceHide.put(FilterOperation.LESS_THAN, Query.FilterOperation.GREATER_THAN_OR_EQUAL);
    toPersistenceHide.put(FilterOperation.LESS_THAN_OR_EQUAL, Query.FilterOperation.GREATER_THAN);
  }

  public static final Query.FilterOperation convertFilterOperation(FilterOperation op,
                                                                   Visibility visibility) {
    return toPersistenceDisplay.get(op);
  }

}
