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
package org.opendatakit.common.persistence.engine.pgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.QueryResult;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.engine.EngineUtils;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryImpl implements Query {

  private static final String K_IS_NULL = " IS NULL ";
  private static final String K_IS_NOT_NULL = " IS NOT NULL ";
  private static final String K_SELECT = "SELECT ";
  private static final String K_SELECT_DISTINCT = "SELECT DISTINCT ";
  private static final String K_BQ = "\"";
  private static final String K_CS = ", ";
  private static final String K_FROM = " FROM ";
  private static final String K_WHERE = " WHERE ";
  private static final String K_AND = " AND ";
  private static final String K_IN_OPEN = " IN (";
  private static final String K_IN_CLOSE = ")";
  private static final String K_BIND_VALUE = " ? ";
  private static final String K_ORDER_BY = " ORDER BY ";

  private static Map<FilterOperation, String> operationMap = new HashMap<FilterOperation, String>();
  private static Map<Direction, String> directionMap = new HashMap<Direction, String>();

  static {
    operationMap.put(FilterOperation.EQUAL, " = ");
    operationMap.put(FilterOperation.NOT_EQUAL, " <> ");
    operationMap.put(FilterOperation.GREATER_THAN, " > ");
    operationMap.put(FilterOperation.GREATER_THAN_OR_EQUAL, " >= ");
    operationMap.put(FilterOperation.LESS_THAN, " < ");
    operationMap.put(FilterOperation.LESS_THAN_OR_EQUAL, " <= ");

    directionMap.put(Direction.ASCENDING, " ASC ");
    directionMap.put(Direction.DESCENDING, " DESC ");
  }

  private final CommonFieldsBase relation;
  private final DatastoreImpl dataStoreImpl;
  private final User user;

  private DataField dominantSortAttr = null;
  private Direction dominantSortDirection = null;
  private boolean isSortedByUri = false;

  private final StringBuilder queryBindBuilder = new StringBuilder();
  private final List<Object> bindValues = new ArrayList<Object>();
  private final StringBuilder querySortBuilder = new StringBuilder();
  private final Log queryStringLogger;

  public QueryImpl(CommonFieldsBase relation, String loggingContextTag,
      DatastoreImpl dataStoreImpl, User user) {
    this.queryStringLogger = LogFactory.getLog("org.opendatakit.common.persistence.LogQueryString." + relation.getSchemaName() + "." + relation.getTableName());
    this.relation = relation;
    this.dataStoreImpl = dataStoreImpl;
    this.user = user;
  }

  private String generateQuery() {
    // generate the query
    StringBuilder baseQueryBuilder = new StringBuilder();

    baseQueryBuilder.append(K_SELECT);

    boolean first = true;
    for (DataField f : relation.getFieldList()) {
      if (!first) {
        baseQueryBuilder.append(K_CS);
      }
      first = false;
      baseQueryBuilder.append(K_BQ);
      baseQueryBuilder.append(f.getName());
      baseQueryBuilder.append(K_BQ);
    }
    baseQueryBuilder.append(K_FROM);
    baseQueryBuilder.append(K_BQ);
    baseQueryBuilder.append(relation.getSchemaName());
    baseQueryBuilder.append(K_BQ);
    baseQueryBuilder.append(".");
    baseQueryBuilder.append(K_BQ);
    baseQueryBuilder.append(relation.getTableName());
    baseQueryBuilder.append(K_BQ);

    return baseQueryBuilder.toString();
  }

  private String generateDistinctFieldValueQuery(DataField dataField) {
    if (!relation.getFieldList().contains(dataField)) {
      throw new IllegalStateException("Attempting to retrieve non-existent data field "
          + dataField.getName() + " from " + relation.getSchemaName() + "."
          + relation.getTableName());
    }

    StringBuilder baseQueryBuilder = new StringBuilder();
    // generate the query
    baseQueryBuilder.append(K_SELECT_DISTINCT);
    baseQueryBuilder.append(K_BQ);
    baseQueryBuilder.append(dataField.getName());
    baseQueryBuilder.append(K_BQ);
    baseQueryBuilder.append(K_FROM);
    baseQueryBuilder.append(K_BQ);
    baseQueryBuilder.append(relation.getSchemaName());
    baseQueryBuilder.append(K_BQ);
    baseQueryBuilder.append(".");
    baseQueryBuilder.append(K_BQ);
    baseQueryBuilder.append(relation.getTableName());
    baseQueryBuilder.append(K_BQ);

    return baseQueryBuilder.toString();
  }

  @Override
  public void addFilter(DataField attributeName, FilterOperation op, Object value) {
    if (queryBindBuilder.length() == 0) {
      queryBindBuilder.append(K_WHERE);
    } else {
      queryBindBuilder.append(K_AND);
    }
    queryBindBuilder.append(K_BQ);
    queryBindBuilder.append(attributeName.getName());
    queryBindBuilder.append(K_BQ);
    if (op.equals(FilterOperation.EQUAL) && value == null) {
      queryBindBuilder.append(K_IS_NULL);
    } else if (op.equals(FilterOperation.NOT_EQUAL) && value == null) {
      queryBindBuilder.append(K_IS_NOT_NULL);
    } else {
      queryBindBuilder.append(operationMap.get(op));
      queryBindBuilder.append(K_BIND_VALUE);
      bindValues.add(DatastoreImpl.getBindValue(attributeName, value));
    }
  }

  /**
   * Constructs the necessary filter clause to append to the Query filters to
   * support continuation cursors.
   * 
   * @param queryContinuationBindBuilder
   * @param continuationValue
   * @return the updated bindArgs
   */
  private ArrayList<Object> addContinuationFilter(StringBuilder queryContinuationBindBuilder,
      Object continuationValue) {
    if (dominantSortAttr == null) {
      throw new IllegalStateException("unexpected state");
    }
    if (continuationValue == null) {
      throw new IllegalStateException("unexpected state");
    }

    if (queryBindBuilder.length() == 0) {
      queryContinuationBindBuilder.append(K_WHERE);
    } else {
      queryContinuationBindBuilder.append(K_AND);
    }
    queryContinuationBindBuilder.append(K_BQ);
    queryContinuationBindBuilder.append(dominantSortAttr.getName());
    queryContinuationBindBuilder.append(K_BQ);
    queryContinuationBindBuilder.append(operationMap.get(dominantSortDirection
        .equals(Direction.ASCENDING) ? FilterOperation.GREATER_THAN_OR_EQUAL
        : FilterOperation.LESS_THAN_OR_EQUAL));
    queryContinuationBindBuilder.append(K_BIND_VALUE);
    
    ArrayList<Object> values = new ArrayList<Object>();
    values.addAll(bindValues);
    values.add(DatastoreImpl.getBindValue(dominantSortAttr, continuationValue));
    
    return values;
  }

  @Override
  public void addValueSetFilter(DataField attributeName, Collection<?> valueSet) {
    if (queryBindBuilder.length() == 0) {
      queryBindBuilder.append(K_WHERE);
    } else {
      queryBindBuilder.append(K_AND);
    }
    queryBindBuilder.append(K_BQ);
    queryBindBuilder.append(attributeName.getName());
    queryBindBuilder.append(K_BQ);
    queryBindBuilder.append(K_IN_OPEN);
    boolean first = true;
    for (Object o : valueSet) {
      if (!first) {
        queryBindBuilder.append(K_CS);
      }
      first = false;
      queryBindBuilder.append(K_BIND_VALUE);
      bindValues.add(DatastoreImpl.getBindValue(attributeName, o));
    }
    queryBindBuilder.append(K_IN_CLOSE);
  }

  @Override
  public void addSort(DataField attributeName, Direction direction) {
    if (querySortBuilder.length() == 0) {
      querySortBuilder.append(K_ORDER_BY);
    } else {
      querySortBuilder.append(K_CS);
    }
    querySortBuilder.append(K_BQ);
    querySortBuilder.append(attributeName.getName());
    querySortBuilder.append(K_BQ);
    querySortBuilder.append(directionMap.get(direction));

    // keep track of the dominant sort attribute...
    if (dominantSortAttr == null) {
      dominantSortAttr = attributeName;
      dominantSortDirection = direction;
    }

    // track whether or not the PK is a sort criteria
    if (attributeName.equals(relation.primaryKey)) {
      isSortedByUri = true;
    }
  }

  @Override
  public List<? extends CommonFieldsBase> executeQuery() throws ODKDatastoreException {

    String query = generateQuery() + queryBindBuilder.toString() + querySortBuilder.toString()
        + ";";
    RowMapper<? extends CommonFieldsBase> rowMapper = null;
    rowMapper = new RelationRowMapper(relation, user);

    try {
      queryStringLogger.debug(query);
      List<? extends CommonFieldsBase> l = dataStoreImpl.getJdbcConnection().query(query,
          bindValues.toArray(), rowMapper);
      dataStoreImpl.recordQueryUsage(relation, l.size());
      return l;
    } catch (Exception e) {
      dataStoreImpl.recordQueryUsage(relation, 0);
      e.printStackTrace();
      throw new ODKDatastoreException(e);
    }
  }

  @Override
  public List<?> executeDistinctValueForDataField(DataField dataField) throws ODKDatastoreException {

    String query = generateDistinctFieldValueQuery(dataField) + queryBindBuilder.toString()
        + querySortBuilder.toString() + ";";

    List<?> keys = null;
    try {
      keys = dataStoreImpl.getJdbcConnection().queryForList(query, bindValues.toArray(),
          String.class);
      dataStoreImpl.recordQueryUsage(relation, keys.size());
    } catch (Exception e) {
      dataStoreImpl.recordQueryUsage(relation, 0);
      e.printStackTrace();
      throw new ODKDatastoreException(e);
    }
    return keys;
  }

  @Override
  public Set<EntityKey> executeForeignKeyQuery(CommonFieldsBase topLevelTable,
      DataField topLevelAuri) throws ODKDatastoreException {

    List<?> keys = executeDistinctValueForDataField(topLevelAuri);

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
  }

  private class CoreResult {
    final List<CommonFieldsBase> results;
    final boolean hasMoreResults;

    CoreResult(List<CommonFieldsBase> results, boolean hasMoreResults) {
      this.results = results;
      this.hasMoreResults = hasMoreResults;
    }
  }

  private class RowMapperFilteredResultSetExtractor implements ResultSetExtractor<CoreResult> {

    private int readCount = 0;
    private final QueryResumePoint startCursor;
    private final int fetchLimit;
    private final RowMapper<? extends CommonFieldsBase> rowMapper;

    RowMapperFilteredResultSetExtractor(QueryResumePoint startCursor, int fetchLimit,
        RowMapper<? extends CommonFieldsBase> rowMapper) {
      this.startCursor = startCursor;
      this.fetchLimit = fetchLimit;
      this.rowMapper = rowMapper;
    }

    @Override
    public CoreResult extractData(ResultSet rs) throws SQLException {
      boolean hasMoreResults = false;
      List<CommonFieldsBase> results = new ArrayList<CommonFieldsBase>();
      String startUri = (startCursor == null) ? null : startCursor.getUriLastReturnedValue();
      boolean beforeUri = (startUri != null);
      while (rs.next()) {
        ++readCount;
        CommonFieldsBase cb = this.rowMapper.mapRow(rs, results.size());
        if (beforeUri) {
          if (startUri.equals(cb.getUri())) {
            beforeUri = false;
          }
        } else if (fetchLimit == 0 || results.size() < fetchLimit) {
          results.add(cb);
        } else {
          hasMoreResults = true;
          break;
        }
      }
      return new CoreResult(results, hasMoreResults);
    }
    
    public int getReadCount() {
      return readCount;
    }

  }

  @Override
  public QueryResult executeQuery(QueryResumePoint startCursor, int fetchLimit)
      throws ODKDatastoreException {

    // we must have at least one sort column defined
    if (dominantSortDirection == null) {
      throw new IllegalStateException("no sort column defined -- cannot execute cusor-style query");
    }

    // if we don't have any sort on the PK, add one
    // direction of PK sort matches that of dominant sort
    if (!isSortedByUri) {
      addSort(relation.primaryKey, dominantSortDirection);
    }

    // for continuation executions of queries
    StringBuilder queryContinuationBindBuilder = new StringBuilder();
    List<Object> values;

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

      Object continuationValue = EngineUtils.getDominantSortAttributeValueFromString(
          startCursor.getValue(), dominantSortAttr);
      values = addContinuationFilter(queryContinuationBindBuilder, continuationValue);
    } else {
      values = bindValues;
    }

    String query = generateQuery() + queryBindBuilder.toString()
        + queryContinuationBindBuilder.toString() + querySortBuilder.toString() + ";";
    RowMapper<? extends CommonFieldsBase> rowMapper = null;
    rowMapper = new RelationRowMapper(relation, user);
    RowMapperFilteredResultSetExtractor rse = new RowMapperFilteredResultSetExtractor(startCursor,
        fetchLimit, rowMapper);

    try {
      CoreResult r;
      try {
        queryStringLogger.debug(query);
        r = dataStoreImpl.getJdbcConnection().query(query, values.toArray(), rse);
      } finally {
        dataStoreImpl.recordQueryUsage(relation, rse.getReadCount());
      }

      if (r.results.size() == 0) {
        return new QueryResult(startCursor, r.results, null, startCursor, false);
      }

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
    } catch (Exception e) {
      e.printStackTrace();
      throw new ODKDatastoreException(e);
    }
  }
}
