/**
 * Copyright (C) 2010 University of Washington
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.QueryResult;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.engine.EngineUtils;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.WebUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.quota.QuotaService;
import com.google.appengine.api.quota.QuotaServiceFactory;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryImpl implements org.opendatakit.common.persistence.Query {

  private static final Map<FilterOperation, FilterOperator> operationMap = new HashMap<FilterOperation, FilterOperator>();

  static {
    operationMap.put(FilterOperation.EQUAL, FilterOperator.EQUAL);
    operationMap.put(FilterOperation.GREATER_THAN, FilterOperator.GREATER_THAN);
    operationMap.put(FilterOperation.GREATER_THAN_OR_EQUAL, FilterOperator.GREATER_THAN_OR_EQUAL);
    operationMap.put(FilterOperation.LESS_THAN, FilterOperator.LESS_THAN);
    operationMap.put(FilterOperation.LESS_THAN_OR_EQUAL, FilterOperator.LESS_THAN_OR_EQUAL);
  }

  private final String loggingContextTag;
  private final CommonFieldsBase relation;
  private final DatastoreImpl datastore;
  private final User user;
  private final Log logger;
  private final ExecutionTimeLogger gaeCostLogger;
  
  private static final long ACTIVE_COST_LOGGING_CHECK_INTERVAL = 10*1000; // 10 seconds
  private static long costLoggingMinimumMegacyclesThreshold = 10*1200; // 10 seconds
  private static long milliLastCheck = 0L;
  
  private final synchronized void updateCostLoggingThreshold() {
    Log logger = LogFactory.getLog(QueryImpl.class);
    
    long currentTime = System.currentTimeMillis();
    if ( milliLastCheck + ACTIVE_COST_LOGGING_CHECK_INTERVAL < currentTime ) {
      
      milliLastCheck = currentTime;// update early in case an exception is thrown...
      try {
        com.google.appengine.api.datastore.Query query = new Query("_COST_LOGGING_");
        PreparedQuery pq = datastore.getDatastoreService().prepare(query);
        List<com.google.appengine.api.datastore.Entity> eList = pq.asList(FetchOptions.Builder.withDefaults());
        if ( eList.isEmpty() ) {
          costLoggingMinimumMegacyclesThreshold = 10*1200; // 10 seconds...
          logger.warn("writing 10-second cost logging threshold record");
          com.google.appengine.api.datastore.Entity e = new com.google.appengine.api.datastore.Entity( "_COST_LOGGING_", "T" + WebUtils.iso8601Date(new Date()) );
          e.setProperty("COST_LOGGING_MEGACYCLE_THRESHOLD", 10*1200 ); // 10 seconds...
          e.setProperty("LAST_UPDATE_DATE", new Date());
          datastore.getDatastoreService().put(e);
        } else {
          Long newValue = null;
          for ( com.google.appengine.api.datastore.Entity e : eList ) {
            Object o = e.getProperty("COST_LOGGING_MEGACYCLE_THRESHOLD");
            if ( o != null ) {
              if ( o instanceof Long ) {
                Long l = (Long) o;
                if ( newValue == null || newValue.compareTo(l) > 0 ) {
                  newValue = l;
                } else {
                  logger.warn("deleting superceded logging threshold record");
                  datastore.getDatastoreService().delete(e.getKey());
                }
              } else if ( o instanceof Integer ) {
                Integer i = (Integer) o;
                Long l = Long.valueOf(i);
                if ( newValue == null || newValue.compareTo(l) > 0 ) {
                  newValue = l;
                } else {
                  logger.warn("deleting superceded logging threshold record");
                  datastore.getDatastoreService().delete(e.getKey());
                }
              } else if ( o instanceof String ) {
                String s = (String) o;
                try { 
                  Long l = Long.parseLong(s);
                  if ( newValue == null || newValue.compareTo(l) > 0 ) {
                    newValue = l;
                  } else {
                    logger.warn("deleting superceded logging threshold record");
                    datastore.getDatastoreService().delete(e.getKey());
                  }
                } catch (NumberFormatException ex) {
                  logger.warn("deleting superceded logging threshold record");
                  datastore.getDatastoreService().delete(e.getKey());
                }
              } else {
                logger.warn("deleting superceded logging threshold record");
                datastore.getDatastoreService().delete(e.getKey());
              }
            }
          }
          if ( newValue == null ) {
            logger.warn("resetting cost logging to 10 second (12000 megacycle) threshold");
            costLoggingMinimumMegacyclesThreshold = 10*1200; // 10 seconds...
          } else if ( costLoggingMinimumMegacyclesThreshold != newValue.longValue() ) { 
            logger.warn("changing cost logging to " + newValue + " megacycle threshold");
            costLoggingMinimumMegacyclesThreshold = newValue;
          }
        }
      } catch ( Exception e ) {
        e.printStackTrace();
        logger.error("exception while updating cost logging threshold: " + e.getMessage());
      }
    }
  }
  
  private final class ExecutionTimeLogger {
    
    private final long startApiTime;
    private long istartApiTime;
    private String queryString = null;
    
    ExecutionTimeLogger() {
      updateCostLoggingThreshold();
      
      QuotaService svc = QuotaServiceFactory.getQuotaService();
      istartApiTime = startApiTime = svc.getApiTimeInMegaCycles();
    }
    
    void declareQuery(com.google.appengine.api.datastore.Query hack) {
      if ( queryString != null ) {
        intermediateLogging();
      }
      queryString = hack.toString();
      // report intermediate results from when query is declared (i.e., execution steps only).
      QuotaService svc = QuotaServiceFactory.getQuotaService();
      istartApiTime = svc.getApiTimeInMegaCycles();
    }
    
    private void intermediateLogging() {
      QuotaService svc = QuotaServiceFactory.getQuotaService();
      long endApiTime = svc.getApiTimeInMegaCycles();
      long elapsed = endApiTime - istartApiTime;
      if ( elapsed >= costLoggingMinimumMegacyclesThreshold ) {
        Log logger = LogFactory.getLog(QueryImpl.ExecutionTimeLogger.class);
        logger.warn(String.format("%1$06d **intermediate** %2$s[%3$s] %4$s", elapsed, loggingContextTag, relation.getTableName(), queryString ));
      }
    }
    
    void wrapUp() {
      QuotaService svc = QuotaServiceFactory.getQuotaService();
      long endApiTime = svc.getApiTimeInMegaCycles();
      long elapsed = endApiTime - startApiTime;
      if ( queryString != null ) {
        intermediateLogging();
      }
      if ( elapsed >= costLoggingMinimumMegacyclesThreshold ) {
        Log logger = LogFactory.getLog(QueryImpl.ExecutionTimeLogger.class);
        logger.warn(String.format("%1$06d **final** %2$s[%3$s]", elapsed, loggingContextTag, relation.getTableName() ));
      }
    }
  }

  /**
   * Track the attributes that we are querying and sorting on...
   * 
   * @author mitchellsundt@gmail.com
   * 
   */
  private abstract class Tracker {
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
          vLong = Long.parseLong(value.toString());
        }
        return compareObjects(eLong, vLong);
      case DECIMAL:
        BigDecimal eDec = record.getNumericField(attribute);
        BigDecimal vDec;
        if (value == null) {
          vDec = null;
        } else {
          vDec = (BigDecimal) value;
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

    abstract void setFilter(com.google.appengine.api.datastore.Query q);
  }

  private final class SimpleFilterTracker extends Tracker {
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
    void setFilter(com.google.appengine.api.datastore.Query q) {
      if (attribute.getDataType() == DataType.DECIMAL) {
        Double d = null;
        if (value != null) {
          BigDecimal bd = (BigDecimal) value;
          d = bd.doubleValue();
        }
        q.addFilter(attribute.getName(), operationMap.get(op), d);
      } else {
        q.addFilter(attribute.getName(), operationMap.get(op), value);
      }
    }
  }

  private final class ValueSetFilterTracker extends Tracker {
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
    void setFilter(com.google.appengine.api.datastore.Query q) {
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
        q.addFilter(attribute.getName(), FilterOperator.IN, dvSet);
      } else {
        q.addFilter(attribute.getName(), FilterOperator.IN, valueSet);
      }
    }
  }

  private final List<Tracker> filterList = new ArrayList<Tracker>();

  private final class SortTracker extends Tracker implements Comparator<CommonFieldsBase> {
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
    void setFilter(com.google.appengine.api.datastore.Query q) {
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

  private final List<SortTracker> sortList = new ArrayList<SortTracker>();

  public QueryImpl(CommonFieldsBase relation, String loggingContextTag, DatastoreImpl datastore,
      User user) {
    this.relation = relation;
    this.loggingContextTag = loggingContextTag;
    this.datastore = datastore;
    this.user = user;
    this.logger = LogFactory.getLog(QueryImpl.class);
    this.gaeCostLogger = new ExecutionTimeLogger();
  }

  @Override
  public void addFilter(DataField attribute, FilterOperation op, Object value) {
    // do everything locally except the first one (later)...
    if (attribute.getDataType() == DataType.DECIMAL) {
      if (value != null) {
        // ensure the value is always a BigDecimal and always rounded to scale
        BigDecimal bd;
        if (value instanceof BigDecimal) {
          bd = (BigDecimal) value;
        } else {
          bd = new BigDecimal(value.toString());
        }
        bd = bd.setScale(attribute.getNumericScale(), BigDecimal.ROUND_HALF_UP);
        filterList.add(new SimpleFilterTracker(attribute, op, bd));
      } else {
        filterList.add(new SimpleFilterTracker(attribute, op, null));
      }
    } else {
      filterList.add(new SimpleFilterTracker(attribute, op, value));
    }
  }

  @Override
  public void addValueSetFilter(DataField attribute, Collection<?> valueSet) {
    // do everything locally except the first one (later)...
    if (attribute.getDataType() == DataType.DECIMAL) {
      List<BigDecimal> bdList = new ArrayList<BigDecimal>();
      for (Object value : valueSet) {
        if (value != null) {
          // ensure the value is always a BigDecimal and always rounded to scale
          BigDecimal bd;
          if (value instanceof BigDecimal) {
            bd = (BigDecimal) value;
          } else {
            bd = new BigDecimal(value.toString());
          }
          bd = bd.setScale(attribute.getNumericScale(), BigDecimal.ROUND_HALF_UP);
          bdList.add(bd);
        } else {
          bdList.add(null);
        }
      }
      filterList.add(new ValueSetFilterTracker(attribute, bdList));
    } else {
      filterList.add(new ValueSetFilterTracker(attribute, valueSet));
    }
  }

  @Override
  public void addSort(DataField attribute, Direction direction) {
    // do the sort locally -- later...
    sortList.add(new SortTracker(attribute, direction));
  }

  private final class CoreResult {
    final List<CommonFieldsBase> results;
    final boolean hasMoreResults;

    CoreResult(List<CommonFieldsBase> results, boolean hasMoreResults) {
      this.results = results;
      this.hasMoreResults = hasMoreResults;
    }
  }

  private interface ResultContainer {
    public void add(CommonFieldsBase record);

    public int size();
  }

  /**
   * Inner action function that can fill odkEntities with enough entries to
   * match all results. It does this by making repeated query requests to GAE.
   * 
   * @param odkEntities
   *          -- list of entities being assembled.
   * @param startCursorFilter
   *          -- filter appropriate for this cursor.
   * @param fetchLimit
   *          -- number of records the requester wants.
   * @return
   * @throws ODKDatastoreException
   */
  private void chunkFetch(ResultContainer odkEntities, SimpleFilterTracker startCursorFilter,
      int fetchLimit) throws ODKDatastoreException {

    // Step 1: create a prepared query that we may repeatedly
    // fetch values from using a chunk size, fetch limit and
    // fetch offset until we have fetched enough records into
    // the odkEntities list to ensure that we can return a
    // total of (fetchLimit+1) values (or exhaust the return set).
    DatastoreService ds = datastore.getDatastoreService();

    // this is the dominant sort:
    SortTracker dominantSort = sortList.get(0);
    DataField dominantSortAttr = dominantSort.getAttribute();

    // Fetch chunks bigger that the default...
    int chunkSize = 2048;

    // We always start the first fetch with an offset of zero
    // even if this is a resumed query (startCursorFilter != null).
    // This is because we will be applying subordinate sorts.
    // We therefore don't know what the true value order is during
    // the GAE fetch phase and therefore cannot apply the last-URI
    // filtering or the appropriate fetch offset until after all
    // candidate values are returned.
    //
    // We do care about the fetchOffset when we issue subsequent
    // requests against the prepared query in order to flesh out
    // the result list.
    //
    int fetchOffset = 0;
    // track the current first entity of the result set.
    // this affects whether we re-issue the prepared query
    // or construct a new query with an updated startCursorFilter.
    CommonFieldsBase odkFirstEntityOfCurrentDominantValue = null;
    boolean dominantSortAttrValueHasChanged = false;
    // convert to odk entities
    EntityRowMapper m = new EntityRowMapper(relation, user);
    int idx = 0; // for logging and debugging only...
    // since the subordinate sorts rearrange the data sharing the same
    // dominantSort attribute value, we must gather all matching start
    // values then all values up to the fetchLimit, then all matching
    // end values and then one more record to determine if there are
    // additional records.
    boolean possiblyBeforeStartCursor = (startCursorFilter != null);
    int sizeQuestionableFirstMatches = 0;

    boolean mustReadEverything = (fetchLimit == 0);
    WorkingValues w = new WorkingValues();
    
    boolean hasQueryResults = true;
    for (; hasQueryResults;) {

      logger.debug("hqrLoop: " + idx + " fetchOffset: " + fetchOffset + " startCursor " + 
          ((startCursorFilter == null) ? "<<none>>" : 
              ((startCursorFilter.value == null) ? "null" : startCursorFilter.value.toString())) );
      PreparedQuery preparedHack;
      try {
        Query hack = new com.google.appengine.api.datastore.Query(relation.getSchemaName() + "."
            + relation.getTableName());

        // determine whether the query has equality tests on fields other than the dominant sort attribute
        boolean hasEqualityTests = false;
        for (Tracker t : filterList) {
          if (t instanceof SimpleFilterTracker) {
            SimpleFilterTracker st = (SimpleFilterTracker) t;
            if (st.isEqualityTest() && !st.getAttribute().equals(dominantSortAttr)) {
              hasEqualityTests = true;
              break;
            }
          }
        }
        
        if ( hasEqualityTests && (fetchLimit == 0) ) {
          
          // assume that the equality tests will filter the result set more strongly than the dominant sort attr.
          // GAE doesn't support ordering and equality tests, so just use the equality tests
          // and don't apply any ordering.
          
          for (Tracker t : filterList) {
            if (t instanceof SimpleFilterTracker) {
              SimpleFilterTracker st = (SimpleFilterTracker) t;
              if (st.isEqualityTest()) {
                st.setFilter(hack);
              }
            }
          }

          // we aren't sorting, so we must read the entire dataset and apply the sort locally
          mustReadEverything = true;
        } else {
          // we have only range queries or we have a non-zero fetchLimit.
          // add all the dominant filter conditions...
          boolean hasDominantFilter = false;
  
          // apply the startCursor filter on dominant attr.
          if (startCursorFilter != null) {
            startCursorFilter.setFilter(hack);
            hasDominantFilter = true;
          }
  
          // add any other filter conditions on the dominant sort attribute.
          // e.g., for "between x and y" types of queries.
          for (Tracker t : filterList) {
            if (dominantSortAttr.equals(t.getAttribute())) {
              t.setFilter(hack);
              hasDominantFilter = true;
            }
          }
  
          SortDirection sd = dominantSort.direction.equals(Direction.ASCENDING) ? SortDirection.ASCENDING
              : SortDirection.DESCENDING;
  
          if (!hasDominantFilter) {
            // GAE production throws an exception when a query with equality filters 
            // and a sort on a column not constrained by any filter is issued.
            // 
            // To work around this, issue a query to return the current min/max value 
            // of the dominant sort attribute and apply a GE/LE constraint using that
            // value.  This is effectively a no-op, but keeps GAE happy.
            // 
            Query orderingHack = new com.google.appengine.api.datastore.Query(relation.getSchemaName() + "."
                + relation.getTableName());
            orderingHack.addSort(dominantSort.getAttribute().getName(), sd);
            
            PreparedQuery orderingPrep = ds.prepare(orderingHack);
            logger.debug("hqrLoop: finding min/max of dominantSortAttr: " + dominantSortAttr.getName());
            
            List<com.google.appengine.api.datastore.Entity> values = orderingPrep.asList(FetchOptions.Builder.withDefaults().limit(3));
            if ( values == null || values.isEmpty() ) {
              // the table is empty -- no need to go further...
              return;
            }
  
            // determine the GE/LE sense of the bogus filter constraint...
            FilterOperator fo = dominantSort.direction.equals(Direction.ASCENDING) ? FilterOperator.GREATER_THAN_OR_EQUAL
                : FilterOperator.LESS_THAN_OR_EQUAL;
            // and apply the filter...
            CommonFieldsBase odkEntity = (CommonFieldsBase) m.mapRow(datastore, values.get(0), 0);
            hack.addFilter( dominantSort.getAttribute().getName(), fo, 
                  EngineUtils.getDominantSortAttributeValue(odkEntity, dominantSortAttr));
          }
          
//          // GAE: this doesn't work in production, though the ZigZag queries are supposed to support this.
//          
//          // and add all equality filter conditions.
//          // track whether the dominant sort column has an equality filter.
//          for (Tracker t : filterList) {
//            if ( !dominantSortAttr.equals(t.getAttribute())) {
//              if (t instanceof SimpleFilterTracker) {
//                SimpleFilterTracker st = (SimpleFilterTracker) t;
//                if (st.isEqualityTest()) {
//                  st.setFilter(hack);
//                }
//              }
//            }
//          }

          // add the dominant sort.
          hack.addSort(dominantSort.getAttribute().getName(), sd);
          // subordinate sorts cannot be applied -- GAE production doesn't like them.
        }
        
        gaeCostLogger.declareQuery(hack);
        
        // Since we are filtering locally, we need to grab a chunk of values
        // in the expectation that most will fail the filter.
        preparedHack = ds.prepare(hack);

      } catch (Exception e) {
        throw new ODKDatastoreException("[" + loggingContextTag + "] Unable to complete request", e);
      }
      FetchOptions options = FetchOptions.Builder.withDefaults().chunkSize(chunkSize)
                                            .prefetchSize(chunkSize).offset(fetchOffset).limit(32*chunkSize);

      logger.debug("hqrLoop: executing preparedQuery ");

      Iterable<com.google.appengine.api.datastore.Entity> it = preparedHack
          .asIterable(options);

      
      w.idx = idx;
      w.fetchOffset = fetchOffset;
      w.startCursorFilter = startCursorFilter;
      w.possiblyBeforeStartCursor = possiblyBeforeStartCursor;
      w.sizeQuestionableFirstMatches = sizeQuestionableFirstMatches;
      w.odkFirstEntityOfCurrentDominantValue = odkFirstEntityOfCurrentDominantValue;
      w.dominantSortAttrValueHasChanged = dominantSortAttrValueHasChanged;
      w.odkAdditionalEntities.clear();
      
      try {
        // loop while the query returns at least one result...
        hasQueryResults = fetchResults(it, dominantSort, dominantSortAttr, m, 
              mustReadEverything, fetchLimit, options.getLimit(), odkEntities.size(), w);
        // and if we succeeded, we update the actual state to that of the fetched results.
        idx = w.idx;
        fetchOffset = w.fetchOffset;
        startCursorFilter = w.startCursorFilter;
        possiblyBeforeStartCursor = w.possiblyBeforeStartCursor;
        sizeQuestionableFirstMatches = w.sizeQuestionableFirstMatches;
        odkFirstEntityOfCurrentDominantValue = w.odkFirstEntityOfCurrentDominantValue;
        dominantSortAttrValueHasChanged = w.dominantSortAttrValueHasChanged;
        for ( CommonFieldsBase cb : w.odkAdditionalEntities ) {
          odkEntities.add(cb);
        }
        
      } catch (ODKDatastoreException e) {
        // we had an exception -- this might be a cursor timeout(!)
        // Step down the chunkSize and try again.  The original 
        // WorkingValues needed to restart the query are unchanged,
        // so we can safely reissue the query.
        if ( chunkSize > 20 ) {
          logger.warn("Retrying fetch with a smaller chunk size");
          chunkSize /= 4;
        } else {
          throw e;
        }
      }
    }
    logger.debug("hqrLoop: done fetching everything!");
  }

  private final static class WorkingValues {
    int idx;
    int fetchOffset;
    SimpleFilterTracker startCursorFilter;
    boolean possiblyBeforeStartCursor;
    int sizeQuestionableFirstMatches;
    CommonFieldsBase odkFirstEntityOfCurrentDominantValue;
    boolean dominantSortAttrValueHasChanged;
    List<CommonFieldsBase> odkAdditionalEntities = new ArrayList<CommonFieldsBase>();
    
    WorkingValues() {
    }
  }
  
  /**
   * Updates WorkingValues with current status values.
   * 
   * @param it
   * @param dominantSort
   * @param dominantSortAttr
   * @param m
   * @param mustReadEverything
   * @param fetchLimit
   * @param odkEntitiesSize
   * @param w
   * @return false if completely done, true if still more work to be done.
   * @throws ODKDatastoreException 
   */
  private final boolean fetchResults(Iterable<com.google.appengine.api.datastore.Entity> it, 
      SortTracker dominantSort, DataField dominantSortAttr, EntityRowMapper m, 
      boolean mustReadEverything, int fetchLimit, Integer readSetLimit, int odkEntitiesSize, WorkingValues w) throws ODKDatastoreException {

    // loop as long as the query returns at least one result...
    boolean hasQueryResults = false;
    try {
      int readSetCount = 0;
      for (com.google.appengine.api.datastore.Entity gaeEntity : it) {
        hasQueryResults = true;
        readSetCount++;

        CommonFieldsBase odkEntity = (CommonFieldsBase) m.mapRow(datastore, gaeEntity, w.idx++);

        // determine whether this odkEntity shares the same dominantSortAttr
        // value as the one before it.
        boolean matchingDominantAttr;

        // reset the fetch offset to zero and update the startCursorFilter
        // if the matchingAttr has changed.
        if (w.odkFirstEntityOfCurrentDominantValue == null) {
          // initialize counters and update reference entity
          w.fetchOffset = 0;
          w.odkFirstEntityOfCurrentDominantValue = odkEntity;
          // matchingDominantAttr is considered true for the first result...
          matchingDominantAttr = true;
        } else {
          matchingDominantAttr = EngineUtils.hasMatchingDominantSortAttribute(
              w.odkFirstEntityOfCurrentDominantValue, odkEntity, dominantSortAttr);
          if (!matchingDominantAttr) {
            // reset counters and update reference entity
            w.fetchOffset = 0;
            w.odkFirstEntityOfCurrentDominantValue = odkEntity;
            w.dominantSortAttrValueHasChanged = true;

            w.startCursorFilter = new SimpleFilterTracker(
                dominantSortAttr,
                dominantSort.direction.equals(Direction.ASCENDING) ? FilterOperation.GREATER_THAN_OR_EQUAL
                    : FilterOperation.LESS_THAN_OR_EQUAL,
                EngineUtils.getDominantSortAttributeValue(odkEntity, dominantSortAttr));
          }
        }
        w.fetchOffset++;

        // if we have read enough records to satisfy the fetchLimit, we
        // only need to continue reading records until matchingDominantAttr
        // becomes false. This indicates that we have collected all the
        // records
        // that could possibly be rearranged by subordinate sorts.
        if (!mustReadEverything && !matchingDominantAttr && !w.possiblyBeforeStartCursor
            && odkEntitiesSize + w.odkAdditionalEntities.size() 
                > fetchLimit + w.sizeQuestionableFirstMatches + 1) {
          // we're done!
          return false;
        }

        // determine if this result passes all filters...
        boolean passed = true;
        for (Tracker t : filterList) {
          if (!t.passFilter(odkEntity)) {
            passed = false;
            break;
          }
        }

        if (passed) {
          if (w.possiblyBeforeStartCursor && w.dominantSortAttrValueHasChanged) {
            // We are starting to process the result set.
            //
            // Until the dominantSortAttr value changes from the
            // value initially given in the constraint, we cannot
            // be sure that we have fetched any values that will
            // eventually be returned to the caller. Subordinate
            // sorts will rearrange the those values and until we
            // have the final sorted ordering, we cannot exclude
            // values before the last previously-returned URI.
            //
            // So we must accumulate all of those values and not
            // count them toward the fetch limit. Once we do see
            // a change in the dominantSortAttr value, we know
            // we will have at least one value in the result set.
            //
            // Remember the number of questionable elements...
            w.possiblyBeforeStartCursor = false;
            w.sizeQuestionableFirstMatches = odkEntitiesSize + w.odkAdditionalEntities.size();
          }
          // add the match...
          w.odkAdditionalEntities.add(odkEntity);
        }
      }
      
      if ( readSetLimit != null && readSetCount < readSetLimit ) {
        // we read all the records that were available on the server 
        // and didn't hit our batch fetch limit... we can stop now.
        return false;
      }
      logger.debug("hqrLoop: done with one batch fetch!");
    } catch (SQLException e) {
      e.printStackTrace();
      throw new ODKDatastoreException("[" + loggingContextTag + "] SQL: " + gaeCostLogger.queryString + " exception: " + e.getMessage(), e);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ODKDatastoreException("[" + loggingContextTag + "] SQL: " + gaeCostLogger.queryString + " exception: " + e.getMessage(), e);
    }
    return hasQueryResults;
  }
  
  private static final class CoreResultContainer implements ResultContainer {
    private List<CommonFieldsBase> odkEntities = new ArrayList<CommonFieldsBase>();

    CoreResultContainer() {
    };

    @Override
    public void add(CommonFieldsBase record) {
      odkEntities.add(record);
    }

    @Override
    public int size() {
      return odkEntities.size();
    }

    public List<CommonFieldsBase> getEntities() {
      return odkEntities;
    }
  }

  private CoreResult coreExecuteQuery(QueryResumePoint startCursor, int fetchLimit)
      throws ODKDatastoreException {

    // get the dominant sort definition
    if (sortList.size() == 0) {
      throw new IllegalStateException("expected at least one sort criteria");
    }

    // this is the dominant sort:
    SortTracker dominantSort = sortList.get(0);
    DataField dominantSortAttr = dominantSort.getAttribute();

    // if we don't have any sort on the PK, add one
    // direction of PK sort matches that of dominant sort
    boolean isUriSortAlreadyPresent = false;
    for (SortTracker st : sortList) {
      if (st.attribute.equals(relation.primaryKey)) {
        isUriSortAlreadyPresent = true;
        break;
      }
    }

    if (!isUriSortAlreadyPresent) {
      // direction of PK sort matches that of dominant sort
      //
      // NOTE: if a PK sort is already defined, it is up to the
      // caller to alter its sense in a new query when using
      // a resume point to fetch the records preceding
      // that resume point.
      addSort(relation.primaryKey, dominantSort.direction);
    }

    // we allow the same query to be executed multiple times with different
    // start cursors (resume points). To do that, we maintain the additional
    // start cursor filter separately from the caller-specified list of
    // query filters.
    SimpleFilterTracker startCursorFilter = null;

    if (startCursor != null) {
      DataField matchingStartCursorAttr = null;
      for (DataField d : relation.getFieldList()) {
        if (d.getName().equals(startCursor.getAttributeName())) {
          matchingStartCursorAttr = d;
          break;
        }
      }
      if (matchingStartCursorAttr == null) {
        throw new IllegalStateException("unable to find the matching attribute name "
            + "for dominant sort attribute in start cursor: " + startCursor.getAttributeName());
      }

      if (!matchingStartCursorAttr.equals(dominantSortAttr)) {
        // the dominant sort column is different
        // -- the start cursor is not appropriate for this query.
        throw new IllegalStateException("start cursor is inappropriate for query");
      }

      Object value = EngineUtils.getDominantSortAttributeValueFromString(startCursor.getValue(),
          dominantSortAttr);

      startCursorFilter = new SimpleFilterTracker(
          dominantSortAttr,
          dominantSort.direction.equals(Direction.ASCENDING) ? FilterOperation.GREATER_THAN_OR_EQUAL
              : FilterOperation.LESS_THAN_OR_EQUAL, value);
    }

    CoreResultContainer odkContainer = new CoreResultContainer();
    chunkFetch(odkContainer, startCursorFilter, fetchLimit);

    List<CommonFieldsBase> odkEntities = odkContainer.getEntities();
    // OK. We have our list of results. Now sort it...
    // Stable sorts nest backwards, so we do this by
    // applying the sorts in reverse order from their definitions.
    //
    // NOTE: since executions may be called repeatedly on the same
    // query, we do not want to in-place reverse or otherwise
    // alter the sortList.
    for (int i = sortList.size() - 1; i >= 0; --i) {
      Collections.sort(odkEntities, sortList.get(i));
    }

    // process the list...
    boolean hasMoreResults = false;
    List<CommonFieldsBase> finalEntities = new ArrayList<CommonFieldsBase>();
    boolean beforeUri = (startCursor != null);
    for (CommonFieldsBase cb : odkEntities) {
      if (beforeUri) {
        if (startCursor.getUriLastReturnedValue().equals(cb.getUri())) {
          beforeUri = false;
        }
      } else if (fetchLimit == 0 || finalEntities.size() < fetchLimit) {
        finalEntities.add(cb);
      } else {
        hasMoreResults = true;
        break;
      }
    }

    return new CoreResult(finalEntities, hasMoreResults);
  }

  @Override
  public QueryResult executeQuery(QueryResumePoint startCursor, int fetchLimit)
      throws ODKDatastoreException {
    try {
      CoreResult r = coreExecuteQuery(startCursor, fetchLimit);
  
      // quick exit -- empty set
      if (r.results.isEmpty()) {
        return new QueryResult(startCursor, r.results, null, startCursor, false);
      }
      // otherwise, we need to get the values of the dominantAttr and uri of the
      // last field.
      DataField dominantSortAttr = sortList.get(0).getAttribute();
  
      // otherwise, we need to get the values of the dominantAttr and uri of the
      // last field.
      CommonFieldsBase cb;
      String value;
      // determine the resume cursor...
      cb = r.results.get(r.results.size() - 1);
      value = EngineUtils.getDominantSortAttributeValueAsString(cb, dominantSortAttr);
      QueryResumePoint resumeCursor = new QueryResumePoint(dominantSortAttr.getName(), value,
          cb.getUri(), ((startCursor != null) ? startCursor.isForwardCursor() : true));
      // determine the backward cursor...
      cb = r.results.get(0);
      value = EngineUtils.getDominantSortAttributeValueAsString(cb, dominantSortAttr);
      QueryResumePoint backwardCursor = new QueryResumePoint(dominantSortAttr.getName(), value,
          cb.getUri(), !((startCursor != null) ? startCursor.isForwardCursor() : true));
  
      return new QueryResult(startCursor, r.results, backwardCursor, resumeCursor, r.hasMoreResults);
    } finally {
      gaeCostLogger.wrapUp();
    }
  }

  @Override
  public List<? extends CommonFieldsBase> executeQuery() throws ODKDatastoreException {

    try {
      // Ensure at least one dominant sort is applied to the result set.
      // This allows the methods that return all matches to leverage the
      // core query execution logic.
  
      if (sortList.isEmpty()) {
        if (filterList.isEmpty()) {
          // use primary key, as we know that is never null.
          addSort(relation.primaryKey, Direction.ASCENDING);
        } else {
          // we want to sort by whatever the first filter
          // criteria is. The callers of the fetch-all
          // methods should be applying the filters in an
          // order that maximally excludes records so we
          // want to pass the first filter down to the GAE
          // layer. Get that to happen by sorting along that
          // filter dimension (which will pass down to GAE the
          // sort directive and the filters for that column).
          for ( Tracker t : filterList ) {
            if ( t instanceof SimpleFilterTracker ) {
              SimpleFilterTracker st = (SimpleFilterTracker) t;
              if ( st.isEqualityTest() ) continue;
              addSort(st.getAttribute(), Direction.ASCENDING);
              break;
            }
          }
          
          if ( sortList.isEmpty() ) {
            addSort( relation.primaryKey, Direction.ASCENDING );
          }
        }
      }
      CoreResult result = coreExecuteQuery(null, 0);
      return result.results;
    } finally {
      gaeCostLogger.wrapUp();
    }
  }

  @Override
  public Set<EntityKey> executeForeignKeyQuery(CommonFieldsBase topLevelTable,
      DataField topLevelAuri) throws ODKDatastoreException {

    try {
      List<?> keys = doExecuteDistinctValueForDataField(topLevelAuri);
  
      Set<EntityKey> keySet = new HashSet<EntityKey>();
      for (Object o : keys) {
        String key = (String) o;
        // we don't have the top level records themselves. Construct the entity
        // keys
        // from the supplied relation and the value of the AURI fields in the
        // records
        // we do have.
        keySet.add(new EntityKey(topLevelTable, key));
      }
      return keySet;
    } finally {
      gaeCostLogger.wrapUp();
    }
  }

  private static final class DistinctResultContainer implements ResultContainer {
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
  
  @Override
  public List<?> executeDistinctValueForDataField(DataField dataField) throws ODKDatastoreException {
    try {
      return doExecuteDistinctValueForDataField(dataField);
    } finally {
      gaeCostLogger.wrapUp();
    }
  }
  
  private List<?> doExecuteDistinctValueForDataField(DataField dataField) throws ODKDatastoreException {
    // use a cursor, since we have to bring everything into memory...
    // this means we need to have at least one sort criteria in place.
    if (sortList.isEmpty()) {
      if (filterList.isEmpty()) {
        // use primary key, as we know that is never null.
        addSort(relation.primaryKey, Direction.ASCENDING);
      } else {
        // we want to sort by whatever the first filter
        // criteria is. The callers of the fetch-all
        // methods should be applying the filters in an
        // order that maximally excludes records so we
        // want to pass the first filter down to the GAE
        // layer. Get that to happen by sorting along that
        // filter dimension (which will pass down to GAE the
        // sort directive and the filters for that column).
        for ( Tracker t : filterList ) {
          if ( t instanceof SimpleFilterTracker ) {
            SimpleFilterTracker st = (SimpleFilterTracker) t;
            if ( st.isEqualityTest() ) continue;
            addSort(st.getAttribute(), Direction.ASCENDING);
            break;
          }
        }
        
        if ( sortList.isEmpty() ) {
          addSort( relation.primaryKey, Direction.ASCENDING );
        }
      }
    }

    SortTracker dominantSort = sortList.get(0);

    // if we don't have any sort on the PK, add one
    // direction of PK sort matches that of dominant sort
    boolean isUriSortAlreadyPresent = false;
    for (SortTracker st : sortList) {
      if (st.attribute.equals(relation.primaryKey)) {
        isUriSortAlreadyPresent = true;
        break;
      }
    }

    if (!isUriSortAlreadyPresent) {
      // direction of PK sort matches that of dominant sort
      //
      // NOTE: if a PK sort is already defined, it is up to the
      // caller to alter its sense in a new query when using
      // a resume point to fetch the records preceding
      // that resume point.
      addSort(relation.primaryKey, dominantSort.direction);
    }

    DistinctResultContainer uniqueResultContainer = new DistinctResultContainer(dataField);

    chunkFetch(uniqueResultContainer, null, 0);

    // OK. We have our list of results. Now sort it...
    // Stable sorts nest backwards, so we do this by
    // applying the sorts in reverse order from their definitions.

    List<Object> values = new ArrayList<Object>(uniqueResultContainer.getValueSet());

    // and apply the sorting, if any, appropriate to this dataField
    for (int i = sortList.size() - 1; i >= 0; --i) {
      SortTracker t = sortList.get(i);
      if (t.getAttribute().equals(dataField)) {
        Collections.sort(values, t.getComparator());
      }
    }
    return values;
  }
}
