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
public class UITrans {

  public static Map<Query.FilterOperation, FilterOperation> toClient = new HashMap<Query.FilterOperation, FilterOperation>();
  public static Map<FilterOperation, Query.FilterOperation> toPersistence = new HashMap<FilterOperation, Query.FilterOperation>();

  static {
    toClient.put(Query.FilterOperation.EQUAL, FilterOperation.EQUAL);
    toClient.put(Query.FilterOperation.GREATER_THAN, FilterOperation.GREATER_THAN);
    toClient.put(Query.FilterOperation.GREATER_THAN_OR_EQUAL, FilterOperation.GREATER_THAN_OR_EQUAL);
    toClient.put(Query.FilterOperation.LESS_THAN, FilterOperation.LESS_THAN);
    toClient.put(Query.FilterOperation.LESS_THAN_OR_EQUAL, FilterOperation.LESS_THAN_OR_EQUAL);
    
    toPersistence.put(FilterOperation.EQUAL, Query.FilterOperation.EQUAL);
    toPersistence.put(FilterOperation.GREATER_THAN, Query.FilterOperation.GREATER_THAN);
    toPersistence.put(FilterOperation.GREATER_THAN_OR_EQUAL, Query.FilterOperation.GREATER_THAN_OR_EQUAL);
    toPersistence.put(FilterOperation.LESS_THAN, Query.FilterOperation.LESS_THAN);
    toPersistence.put(FilterOperation.LESS_THAN_OR_EQUAL, Query.FilterOperation.LESS_THAN_OR_EQUAL);
  }
  
  public static Query.FilterOperation convertFilterOperation(FilterOperation op) {
    return toPersistence.get(op);
  }
  
  public static FilterOperation convertFilterOperation(Query.FilterOperation op) {
    return toClient.get(op);
  }
}
