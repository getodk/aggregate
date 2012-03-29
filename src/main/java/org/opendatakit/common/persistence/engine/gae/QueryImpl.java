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
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.WebUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.apphosting.api.ApiProxy.OverQuotaException;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryImpl implements org.opendatakit.common.persistence.Query {

  private static final boolean isWorkingZigZagEqualityFiltering = false;
  
  static final Map<FilterOperation, FilterOperator> operationMap = new HashMap<FilterOperation, FilterOperator>();

  static {
    operationMap.put(FilterOperation.EQUAL, FilterOperator.EQUAL);
    operationMap.put(FilterOperation.NOT_EQUAL, FilterOperator.NOT_EQUAL);
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

  private static final long ACTIVE_COST_LOGGING_CHECK_INTERVAL = 10 * 1000; // 10
                                                                            // seconds
  static long costLoggingMinimumMegacyclesThreshold = 10 * 1200; // 10
                                                                         // seconds
  private static long milliLastCheck = 0L;

  final static synchronized void updateCostLoggingThreshold(DatastoreImpl datastore) {
    Log logger = LogFactory.getLog(QueryImpl.class);

    long currentTime = System.currentTimeMillis();
    if (milliLastCheck + ACTIVE_COST_LOGGING_CHECK_INTERVAL < currentTime) {

      milliLastCheck = currentTime;// update early in case an exception is
                                   // thrown...
      try {
        com.google.appengine.api.datastore.Query query = new Query("_COST_LOGGING_");
        PreparedQuery pq = datastore.getDatastoreService().prepare(query);
        logger.debug("costLogging fetch.");
        List<com.google.appengine.api.datastore.Entity> eList = pq.asList(FetchOptions.Builder
            .withDefaults());
        datastore.recordQueryUsage("_COST_LOGGING_", eList.size());
        if (eList.isEmpty()) {
          costLoggingMinimumMegacyclesThreshold = 10 * 1200; // 10 seconds...
          logger.warn("writing 10-second cost logging threshold record");
          com.google.appengine.api.datastore.Entity e = new com.google.appengine.api.datastore.Entity(
              "_COST_LOGGING_", "T" + WebUtils.iso8601Date(new Date()));
          e.setProperty("COST_LOGGING_MEGACYCLE_THRESHOLD", 10 * 1200); // 10
                                                                        // seconds...
          e.setProperty("LAST_UPDATE_DATE", new Date());
          datastore.getDatastoreService().put(e);
        } else {
          Long newValue = null;
          for (com.google.appengine.api.datastore.Entity e : eList) {
            Object o = e.getProperty("COST_LOGGING_MEGACYCLE_THRESHOLD");
            if (o != null) {
              if (o instanceof Long) {
                Long l = (Long) o;
                if (newValue == null || newValue.compareTo(l) > 0) {
                  newValue = l;
                } else {
                  logger.warn("deleting superceded logging threshold record");
                  datastore.getDatastoreService().delete(e.getKey());
                }
              } else if (o instanceof Integer) {
                Integer i = (Integer) o;
                Long l = Long.valueOf(i);
                if (newValue == null || newValue.compareTo(l) > 0) {
                  newValue = l;
                } else {
                  logger.warn("deleting superceded logging threshold record");
                  datastore.getDatastoreService().delete(e.getKey());
                }
              } else if (o instanceof String) {
                String s = (String) o;
                try {
                  Long l = Long.parseLong(s);
                  if (newValue == null || newValue.compareTo(l) > 0) {
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
          if (newValue == null) {
            logger.warn("resetting cost logging to 10 second (12000 megacycle) threshold");
            costLoggingMinimumMegacyclesThreshold = 10 * 1200; // 10 seconds...
          } else if (costLoggingMinimumMegacyclesThreshold != newValue.longValue()) {
            logger.warn("changing cost logging to " + newValue + " megacycle threshold");
            costLoggingMinimumMegacyclesThreshold = newValue;
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        logger.error("exception while updating cost logging threshold: " + e.getMessage());
      }
    }
  }

  private final List<Tracker> filterList = new ArrayList<Tracker>();

  private final List<SortTracker> sortList = new ArrayList<SortTracker>();

  public QueryImpl(CommonFieldsBase relation, String loggingContextTag, DatastoreImpl datastore,
      User user) {
    this.relation = relation;
    this.loggingContextTag = loggingContextTag;
    this.datastore = datastore;
    this.user = user;
    this.logger = LogFactory.getLog(QueryImpl.class);
    this.gaeCostLogger = new ExecutionTimeLogger(datastore, loggingContextTag, relation);
  }

  private SimpleFilterTracker constructFilter( DataField attribute, FilterOperation op, Object value) {
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
        return new SimpleFilterTracker(attribute, op, bd);
      } else {
        return new SimpleFilterTracker(attribute, op, null);
      }
    } else {
      return new SimpleFilterTracker(attribute, op, value);
    }
  }
  
  @Override
  public void addFilter(DataField attribute, FilterOperation op, Object value) {
    filterList.add(constructFilter(attribute, op, value));
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

  /**
   * 
   * @param dominantSortAttr
   * @param startCursorFilter
   * @return true if there is at least one filter criteria against the dominant sort attribute
   */
  private boolean hasDominantAttributeFilter(DataField dominantSortAttr, SimpleFilterTracker startCursorFilter) {

    // apply the startCursor filter on dominant attr.
    if (startCursorFilter != null) {
      return true;
    }

    // add any other filter conditions on the dominant sort attribute.
    // e.g., for "between x and y" types of queries.
    for (Tracker t : filterList) {
      if (dominantSortAttr.equals(t.getAttribute())) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * If there is no filter criteria on the dominant sort attribute, we need
   * to query the database to find the min/max value for the attribute and then
   * apply that to the query.  This is a work-around for broken GAE functionality.
   * 
   * @return null if no records in table; otherwise, produce the needed filter.
   * @throws ODKDatastoreException
   */
  private SimpleFilterTracker getImpliedDominantAttributeFilter() throws ODKDatastoreException {
    // GAE production throws an exception when a query with equality
    // filters and a sort on a column not constrained by any filter
    // is issued.
    //
    // To work around this, issue a query to return the current min/max
    // value of the dominant sort attribute and apply a GE/LE 
    // constraint using that value. This is effectively a no-op, 
    // but keeps GAE happy.
    //
    // this is the dominant sort:
    SortTracker dominantSort = sortList.get(0);
    DataField dominantSortAttr = dominantSort.getAttribute();
    SortDirection dominantSortDirection = 
        dominantSort.direction.equals(Direction.ASCENDING) 
        ? SortDirection.ASCENDING
        : SortDirection.DESCENDING;
    

    DatastoreService ds = datastore.getDatastoreService();
    EntityRowMapper m = new EntityRowMapper(relation, user);

    Query orderingHack = new com.google.appengine.api.datastore.Query(
        relation.getSchemaName() + "." + relation.getTableName());
    orderingHack.addSort(dominantSort.getAttribute().getName(), dominantSortDirection);

    PreparedQuery orderingPrep = ds.prepare(orderingHack);
    logger.debug("hqrLoop: finding min/max in " + relation.getSchemaName() + "."
        + relation.getTableName() + " of dominantSortAttr: " + dominantSortAttr.getName());

    List<com.google.appengine.api.datastore.Entity> values = orderingPrep
        .asList(FetchOptions.Builder.withDefaults().prefetchSize(1).chunkSize(20).limit(3));
    datastore.recordQueryUsage(relation, values.size());
    if (values == null || values.isEmpty()) {
      // the table is empty -- no need to go further...
      return null;
    }

    Object dominantFilterValue = null;
    // and apply the filter...
    try {
      CommonFieldsBase odkEntity = (CommonFieldsBase) m.mapRow(datastore, values.get(0), 0);
      dominantFilterValue = EngineUtils.getDominantSortAttributeValue(odkEntity, dominantSortAttr);
    } catch (SQLException e) {
      throw new ODKDatastoreException("[" + loggingContextTag + "] Unable to complete request", e);
    }
    
    SimpleFilterTracker impliedDominantFilter = constructFilter(dominantSortAttr, 
        dominantSort.direction.equals(Direction.ASCENDING) 
        ? FilterOperation.GREATER_THAN_OR_EQUAL
        : FilterOperation.LESS_THAN_OR_EQUAL, dominantFilterValue );
    
    return impliedDominantFilter;
  }
  
  /**
   * Construct the query appropriate for this fragment of the result-set production.
   * 
   * @param startCursorFilter
   * @param impliedDominantFilter
   * @return
   * @throws ODKOverQuotaException
   * @throws ODKDatastoreException
   */
  private PreparedQuery prepareQuery( SimpleFilterTracker startCursorFilter,  SimpleFilterTracker impliedDominantFilter) throws ODKOverQuotaException, ODKDatastoreException {

    SortTracker dominantSort = sortList.get(0);
    DataField dominantSortAttr = dominantSort.getAttribute();

    try {
      Query hack = new com.google.appengine.api.datastore.Query(relation.getSchemaName() + "."
          + relation.getTableName());

      // add all the dominant filter conditions...

      // apply the startCursor filter on dominant attr.
      if (startCursorFilter != null) {
        startCursorFilter.setFilter(hack);
      }

      // add any other filter conditions on the dominant sort attribute.
      // e.g., for "between x and y" types of queries.
      for (Tracker t : filterList) {
        if (dominantSortAttr.equals(t.getAttribute())) {
          t.setFilter(hack);
        }
      }

      if (impliedDominantFilter != null) {
        // and apply the implicit filter...
        impliedDominantFilter.setFilter(hack);
      }

      // and add all other equality filter conditions.
      if ( isWorkingZigZagEqualityFiltering ) {
        // GAE: this doesn't work in production, 
        // though the ZigZag queries are supposed 
        // to support this...
        for (Tracker t : filterList) {
          if (!dominantSortAttr.equals(t.getAttribute())) {
            if (t instanceof SimpleFilterTracker) {
              SimpleFilterTracker st = (SimpleFilterTracker) t;
              if (st.isEqualityTest()) {
                st.setFilter(hack);
              }
            }
          }
        }
      }

      // add the dominant sort.
      SortDirection dominantSortDirection = 
          dominantSort.direction.equals(Direction.ASCENDING) 
          ? SortDirection.ASCENDING
          : SortDirection.DESCENDING;
      
      hack.addSort(dominantSort.getAttribute().getName(), dominantSortDirection);
      // subordinate sorts cannot be applied 
      // GAE production doesn't like them.

      gaeCostLogger.declareQuery(hack);
      DatastoreService ds = datastore.getDatastoreService();

      return ds.prepare(hack);

    } catch (OverQuotaException e) {
      throw new ODKOverQuotaException("[" + loggingContextTag + "] Quota exceeded", e);
    } catch (Exception e) {
      throw new ODKDatastoreException("[" + loggingContextTag + "] Unable to complete request", e);
    }
  }
  
  /**
   * Inner action function that can fill odkEntities with > fetchLimit+1 entries
   * beyond the set of entries matching the initial dominantSortAttr value.
   * We must gather all end values matching the dominant sort attribute's value
   * of the 'final' value and then grab the value after those as well.  This
   * is because the 'final' value can have nested sorts applied that will reorder
   * the sequence.
   * 
   * @param odkEntities
   *          -- list of entities being assembled.
   * @param startCursorFilter
   *          -- filter appropriate for this cursor.
   * @param fetchLimit
   *          -- number of records the requester wants.
   * @return
   * @throws ODKDatastoreException
   * @throws ODKOverQuotaException
   */
  private void chunkFetch(ResultContainer odkEntities, SimpleFilterTracker startCursorFilter,
      int fetchLimit) throws ODKDatastoreException, ODKOverQuotaException {

    // Step 1: create a prepared query that we may repeatedly
    // fetch values from using a chunk size, fetch limit and
    // fetch offset until we have fetched enough records into
    // the odkEntities list to ensure that we can return a
    // total of (fetchLimit+1) values (or exhaust the return set).

    // this is the dominant sort:
    SortTracker dominantSort = sortList.get(0);
    DataField dominantSortAttr = dominantSort.getAttribute();

    // Test whether we have filters on the dominant sort attribute.
    // If we don't have filters on the dominant sort attribute, 
    // then interrogate the database to establish an implied filter.
    SimpleFilterTracker impliedDominantFilter = null;
    
    if (!hasDominantAttributeFilter(dominantSortAttr, startCursorFilter)) {
      impliedDominantFilter = getImpliedDominantAttributeFilter();
      if ( impliedDominantFilter == null ) {
        // we need a filter but there are no items in the table...
        return; // no data...
      }
    }

    EntityRowMapper m = new EntityRowMapper(relation, user);
    
    // Fetch chunks bigger that the default...
    // Bulk fetches are dead slow if the chunks are small. 
    // This burns quota but makes fetches that filter data faster.
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

      logger.debug("hqrLoop: "
          + idx
          + " fetchOffset: "
          + fetchOffset
          + " startCursor "
          + ((startCursorFilter == null) ? "<<none>>" : ((startCursorFilter.value == null) ? "null"
              : startCursorFilter.value.toString())));

      // Since we are filtering locally, we need to grab a chunk of values
      // in the expectation that most will fail the filter.
      //
      // We also need to handle the timing out of the query to GAE.  This
      // means we must artificially limit the production of results for the
      // query so that the query itself does not time out in GAE.
      //
      // Prefetch only 105 records to handle the common case of an
      // unfiltered paged display of 100 submissions.
      FetchOptions options = FetchOptions.Builder.withDefaults().chunkSize(chunkSize)
          .prefetchSize(105).offset(fetchOffset).limit(8*chunkSize);

      logger.debug("hqrLoop: executing preparedQuery on " + relation.getSchemaName() + "."
          + relation.getTableName());

      w.idx = idx;
      w.fetchOffset = fetchOffset;
      w.startCursorFilter = startCursorFilter;
      w.possiblyBeforeStartCursor = possiblyBeforeStartCursor;
      w.sizeQuestionableFirstMatches = sizeQuestionableFirstMatches;
      w.odkFirstEntityOfCurrentDominantValue = odkFirstEntityOfCurrentDominantValue;
      w.dominantSortAttrValueHasChanged = dominantSortAttrValueHasChanged;
      w.odkAdditionalEntities.clear();

      try {
        { 
          // scope iterable to this block -- can be GC'd on exit from it.
          Iterable<com.google.appengine.api.datastore.Entity> it;
          try {
            PreparedQuery preparedHack = prepareQuery(startCursorFilter, impliedDominantFilter);
            it = preparedHack.asIterable(options);
          } catch (OverQuotaException e) {
            datastore.recordQueryUsage(relation, 0);
            throw new ODKOverQuotaException("[" + loggingContextTag + "] Quota exceeded", e);
          } catch (Exception e) {
            datastore.recordQueryUsage(relation, 0);
            throw new ODKDatastoreException("[" + loggingContextTag + "] Unable to complete request", e);
          }
  
          // loop while the query returns at least one result...
          // Calls recordQueryUsage -- including during exceptions.
          hasQueryResults = fetchResults(it, dominantSort, dominantSortAttr, m, mustReadEverything,
              fetchLimit, options.getLimit(), odkEntities.size(), w);
        }
        // and if we succeeded, we update the actual state to that of the
        // fetched results.  This loop makes extensive use of the 
        // fetchOffset to index into the remainder of the query set
        // even when we re-issue the query.  Note that the above
        // query-set will return only up to options.getLimit() values, 
        // and there may be many more values beyond that.
        idx = w.idx;
        fetchOffset = w.fetchOffset;
        startCursorFilter = w.startCursorFilter;
        possiblyBeforeStartCursor = w.possiblyBeforeStartCursor;
        sizeQuestionableFirstMatches = w.sizeQuestionableFirstMatches;
        odkFirstEntityOfCurrentDominantValue = w.odkFirstEntityOfCurrentDominantValue;
        dominantSortAttrValueHasChanged = w.dominantSortAttrValueHasChanged;
        for (CommonFieldsBase cb : w.odkAdditionalEntities) {
          odkEntities.add(cb);
        }

      } catch (ODKOverQuotaException e) {
        throw e;
      } catch (ODKDatastoreException e) {
        // we had an exception -- this might be a cursor timeout(!)
        // Step down the chunkSize and try again. The original
        // WorkingValues needed to restart the query are unchanged,
        // so we can safely reissue the query.
        if (chunkSize > 64) {
          logger.warn("Retrying fetch with a smaller chunk size: " + e.getMessage());
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
   * This fetches the records from the result-set iterator
   * and applies the filter criteria to them.  It assumes
   * the query production will return a consistently ordered
   * set of results.
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
   * @throws ODKOverQuotaException
   */
  private final boolean fetchResults(Iterable<com.google.appengine.api.datastore.Entity> it,
      SortTracker dominantSort, DataField dominantSortAttr, EntityRowMapper m,
      boolean mustReadEverything, int fetchLimit, Integer readSetLimit, int odkEntitiesSize,
      WorkingValues w) throws ODKDatastoreException, ODKOverQuotaException {

    // loop as long as the query returns at least one result...
    boolean hasQueryResults = false;
    try {
      int readSetCount = 0;
      try {
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
                  dominantSort.direction.equals(Direction.ASCENDING) 
                    ? FilterOperation.GREATER_THAN_OR_EQUAL
                    : FilterOperation.LESS_THAN_OR_EQUAL,
                  EngineUtils.getDominantSortAttributeValue(odkEntity, dominantSortAttr));
            }
          }
          w.fetchOffset++;
  
          // if we have read enough records to satisfy the fetchLimit, we
          // only need to continue reading records until matchingDominantAttr
          // becomes false. This indicates that we have collected all the
          // records that could possibly be rearranged by subordinate sorts.
          //
          if (!mustReadEverything
              && !matchingDominantAttr
              && !w.possiblyBeforeStartCursor
              && odkEntitiesSize + w.odkAdditionalEntities.size() > fetchLimit
                  + w.sizeQuestionableFirstMatches + 1) {
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
      } finally {
        datastore.recordQueryUsage(relation, readSetCount);
      }

      if (readSetLimit != null && readSetCount < readSetLimit) {
        // we read all the records that were available on the server
        // and didn't hit our batch fetch limit... we can stop now.
        return false;
      }
      logger.debug("hqrLoop: done with one batch fetch!");
    } catch (SQLException e) {
      e.printStackTrace();
      throw new ODKDatastoreException("[" + loggingContextTag + "] SQL: "
          + gaeCostLogger.queryString + " exception: " + e.getMessage(), e);
    } catch (OverQuotaException e) {
      throw new ODKOverQuotaException("[" + loggingContextTag + "] Quota exceeded", e);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ODKDatastoreException("[" + loggingContextTag + "] SQL: "
          + gaeCostLogger.queryString + " exception: " + e.getMessage(), e);
    }
    return hasQueryResults;
  }

  /**
   * If the primary key does not already have a sort order applied,
   * ensure that it is ordered in the same ascending/descending
   * order as the dominant sort parameter.
   * 
   * @param dominantSort
   */
  private void enforcePrimaryKeyOrdering(SortTracker dominantSort) {
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
  }
  
  /**
   * Uses chunkFetch(...) to fetch at least fetchLimit+1 values beyond
   * the start-value (or all values, if fetchLimit is zero).  Then orders
   * the returned values and filters them w.r.t. the start cursor's PK
   * restriction, constructing and returning the ordered list of results
   * to the caller.
   *
   * @param startCursor
   * @param fetchLimit
   * @return
   * @throws ODKDatastoreException
   * @throws ODKOverQuotaException
   */
  private CoreResult coreExecuteQuery(QueryResumePoint startCursor, int fetchLimit)
      throws ODKDatastoreException, ODKOverQuotaException {

    // get the dominant sort definition
    if (sortList.isEmpty()) {
      throw new IllegalStateException("expected at least one sort criteria");
    }

    // this is the dominant sort:
    SortTracker dominantSort = sortList.get(0);
    DataField dominantSortAttr = dominantSort.getAttribute();

    enforcePrimaryKeyOrdering(dominantSort);

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
    String startUri = (startCursor == null) ? null : startCursor.getUriLastReturnedValue();
    boolean beforeUri = (startUri != null);
    for (CommonFieldsBase cb : odkEntities) {
      if (beforeUri) {
        if (startUri.equals(cb.getUri())) {
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
      throws ODKDatastoreException, ODKOverQuotaException {
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

  /**
   * Incoming queries that lack an sort criteria will have the 
   * field of the first filter criteria passed down as a sort
   * criteria.  If zig-zag queries are supported, the sort 
   * field will be the first non-equality test in the initial
   * query (or the PK if there is none).
   */
  private void establishDominantSort() {
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
        for (Tracker t : filterList) {
          if (t instanceof SimpleFilterTracker) {
            SimpleFilterTracker st = (SimpleFilterTracker) t;
            if ( isWorkingZigZagEqualityFiltering ) {
              if (st.isEqualityTest() ) {
                continue; //   zig-zag allows multiple equality tests...
              }
            }
            addSort(st.getAttribute(), Direction.ASCENDING);
            break;
          }
        }

        if (sortList.isEmpty()) {
          addSort(relation.primaryKey, Direction.ASCENDING);
        }
      }
    }
  }

  @Override
  public List<? extends CommonFieldsBase> executeQuery() throws ODKDatastoreException, ODKOverQuotaException {

    try {
      establishDominantSort();
      CoreResult result = coreExecuteQuery(null, 0);
      return result.results;
    } finally {
      gaeCostLogger.wrapUp();
    }
  }

  @Override
  public Set<EntityKey> executeForeignKeyQuery(CommonFieldsBase topLevelTable,
      DataField topLevelAuri) throws ODKDatastoreException, ODKOverQuotaException {

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

  @Override
  public List<?> executeDistinctValueForDataField(DataField dataField) throws ODKDatastoreException, ODKOverQuotaException {
    try {
      return doExecuteDistinctValueForDataField(dataField);
    } finally {
      gaeCostLogger.wrapUp();
    }
  }

  private List<?> doExecuteDistinctValueForDataField(DataField dataField)
      throws ODKDatastoreException, ODKOverQuotaException {

    establishDominantSort();
    SortTracker dominantSort = sortList.get(0);

    enforcePrimaryKeyOrdering(dominantSort);

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
