/*
 * Copyright (C) 2012-2013 University of Washington
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions.DbColumnDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbLogTable;
import org.opendatakit.aggregate.odktables.relation.DbTable;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions.DbTableDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry.DbTableEntryEntity;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowList;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome.OutcomeType;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Query;
import org.opendatakit.common.ermodel.Query.WebsafeQueryResult;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.PersistenceUtils;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Manages read, insert, update, and delete operations on the rows of a table.
 *
 * @author the.dylan.price@gmail.com
 * @author sudar.sam@gmail.com
 *
 */

public class DataManager {

  private static final Log logger = LogFactory.getLog(DataManager.class);

  public static class WebsafeRows {
    public final List<Row> rows;

    public final String websafeRefetchCursor;
    public final String websafeBackwardCursor;
    public final String websafeResumeCursor;
    public final boolean hasMore;
    public final boolean hasPrior;

    public WebsafeRows(List<Row> rows, String websafeRefetchCursor, String websafeBackwardCursor,
        String websafeResumeCursor, boolean hasMore, boolean hasPrior) {
      this.rows = rows;
      this.websafeRefetchCursor = websafeRefetchCursor;
      this.websafeBackwardCursor = websafeBackwardCursor;
      this.websafeResumeCursor = websafeResumeCursor;
      this.hasMore = hasMore;
      this.hasPrior = hasPrior;
    }
  }

  private CallingContext cc;
  private TablesUserPermissions userPermissions;
  private EntityConverter converter;
  private EntityCreator creator;
  private String appId;
  private String tableId;

  /**
   * Construct a new DataManager.
   *
   * @param tableId
   *          the unique identifier of the table
   * @param userPermissions
   *          the requesting user's permissions
   * @param cc
   *          the calling context
   * @throws ODKEntityNotFoundException
   *           if no table with the given id exists
   * @throws ODKDatastoreException
   *           if there is an internal error in the datastore
   */
  public DataManager(String appId, String tableId, TablesUserPermissions userPermissions,
      CallingContext cc) throws ODKEntityNotFoundException, ODKDatastoreException {
    Validate.notEmpty(appId);
    Validate.notEmpty(tableId);
    Validate.notNull(cc);
    this.cc = cc;
    this.userPermissions = userPermissions;
    this.converter = new EntityConverter();
    this.creator = new EntityCreator();
    this.appId = appId;
    this.tableId = tableId;
  }

  public String getAppId() {
    return appId;
  }

  public String getTableId() {
    return tableId;
  }

  private void revertPendingChanges(DbTableEntryEntity entry,
      List<DbColumnDefinitionsEntity> columns, DbTable table, DbLogTable logTable)
      throws ODKDatastoreException, BadColumnNameException {

    // we have nothing to do if the pending dataETag is null...
    String dataETag = entry.getPendingDataETag();
    if (dataETag == null) {
      return;
    }

    logger.warn("Reverting changes for dataETag " + dataETag);
    // search for log entries matching the TableEntry dataETag
    // log entries are written first, so these should exist, and the
    // row entries may or may not reflect the log contents.

    Query query = logTable.query("DataManager.revertPendingChanges", cc);
    query.equal(DbLogTable.DATA_ETAG_AT_MODIFICATION, dataETag);
    List<Entity> logEntries = query.execute();

    for (Entity logEntity : logEntries) {
      // Log entries maintain the history of previous rowETags
      // Chain back through that to get the previous log record.
      // If the previous rowETag is null, it means that the rowId
      // did not exist prior to this log entry.
      String priorETag = logEntity.getString(DbLogTable.PREVIOUS_ROW_ETAG);
      if (priorETag == null) {
        // no prior state -- so rowId may not exist now...
        try {
          // try to retrieve the rowId from the DbTable
          Entity rowEntity = table.getEntity(logEntity.getString(DbLogTable.ROW_ID), cc);
          // if found, delete it
          rowEntity.delete(cc);
        } catch (ODKEntityNotFoundException e) {
          // ignore... it was never created, which is OK!
        }
        // remove the entry in DbLogTable for the pending state
        logEntity.delete(cc);
      } else {
        // there is prior state, so the rowId should exist
        Entity rowEntity = table.getEntity(logEntity.getString(DbLogTable.ROW_ID), cc);
        // and the prior state should exist in the log...
        Entity priorLogEntity = logTable.getEntity(
            logEntity.getString(DbLogTable.PREVIOUS_ROW_ETAG), cc);

        // reset the row to the prior row state
        creator.setRowFields(rowEntity, priorLogEntity.getId(),
            priorLogEntity.getString(DbLogTable.DATA_ETAG_AT_MODIFICATION),
            priorLogEntity.getString(DbLogTable.LAST_UPDATE_USER),
            priorLogEntity.getBoolean(DbLogTable.DELETED),
            converter.getDbLogTableFilterScope(priorLogEntity),
            priorLogEntity.getString(DbLogTable.FORM_ID),
            priorLogEntity.getString(DbLogTable.LOCALE),
            priorLogEntity.getString(DbLogTable.SAVEPOINT_TYPE),
            priorLogEntity.getString(DbLogTable.SAVEPOINT_TIMESTAMP),
            priorLogEntity.getString(DbLogTable.SAVEPOINT_CREATOR),
            converter.getRowValues(priorLogEntity, columns), columns);

        // revert DbTable to the prior row state
        rowEntity.put(cc);
        // remove the entry in DbLogTable for the pending state
        logEntity.delete(cc);
      }
    }
  }

  /**
   * Retrieve all current rows of the table.
   *
   * @return all the rows of the table.
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   * @throws InconsistentStateException
   * @throws BadColumnNameException
   */
  public WebsafeRows getRows(QueryResumePoint startCursor, int fetchLimit)
      throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException,
      InconsistentStateException, BadColumnNameException {

    userPermissions.checkPermission(appId, tableId, TablePermission.READ_ROW);

    List<DbColumnDefinitionsEntity> columns = null;
    WebsafeQueryResult result = null;
    LockTemplate propsLock = new LockTemplate(tableId,
        ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      propsLock.acquire();

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if (schemaETag == null) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      DbTableDefinitionsEntity tableDefn = DbTableDefinitions
          .getDefinition(tableId, schemaETag, cc);
      columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

      DbTable table = DbTable.getRelation(tableDefn, columns, cc);
      DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

      revertPendingChanges(entry, columns, table, logTable);

      Query query = buildRowsQuery(table);
      query.addSort(table.getDataField(CommonFieldsBase.CREATION_DATE_COLUMN_NAME),
          (startCursor == null || startCursor.isForwardCursor()) ? Direction.ASCENDING
              : Direction.DESCENDING);
      // we need the filter to activate the sort...
      query.addFilter(table.getDataField(CommonFieldsBase.CREATION_DATE_COLUMN_NAME),
          org.opendatakit.common.persistence.Query.FilterOperation.GREATER_THAN, BasicConsts.EPOCH);
      result = query.execute(startCursor, fetchLimit);

    } finally {
      propsLock.release();
    }

    if (result.entities == null || columns == null) {
      throw new InconsistentStateException("Unable to retrieve rows for table " + tableId + ".");
    }

    ArrayList<Row> rows = new ArrayList<Row>();
    for (Entity entity : result.entities) {
      Row row = converter.toRow(entity, columns);
      if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
        rows.add(row);
      } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW,
          row.getRowId(), row.getFilterScope())) {
        rows.add(row);
      }
    }
    return new WebsafeRows(rows, result.websafeRefetchCursor, result.websafeBackwardCursor,
        result.websafeResumeCursor, result.hasMore, result.hasPrior);
  }

  /**
   * @return the query for current rows in the table
   */
  private Query buildRowsQuery(DbTable table) {
    Query query = table.query("DataManager.buildRowsQuery", cc);
    query.equal(DbTable.DELETED, false);
    return query;
  }

  /**
   * Retrieves a set of rows representing the changes since the given data etag.
   *
   * @param dataETag
   *          the data ETag
   * @return the rows which have changed or been added since the given data etag
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws InconsistentStateException
   * @throws PermissionDeniedException
   * @throws BadColumnNameException
   */
  public WebsafeRows getRowsSince(String dataETag, QueryResumePoint startCursor, int fetchLimit)
      throws ODKDatastoreException, ODKTaskLockException, InconsistentStateException,
      PermissionDeniedException, BadColumnNameException {

    userPermissions.checkPermission(appId, tableId, TablePermission.READ_ROW);

    List<DbColumnDefinitionsEntity> columns = null;
    WebsafeQueryResult result = null;
    LockTemplate propsLock = new LockTemplate(tableId,
        ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      propsLock.acquire();

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if (schemaETag == null) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      DbTableDefinitionsEntity tableDefn = DbTableDefinitions
          .getDefinition(tableId, schemaETag, cc);
      columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

      DbTable table = DbTable.getRelation(tableDefn, columns, cc);
      DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

      revertPendingChanges(entry, columns, table, logTable);

      String sequenceValue = null;
      if (dataETag != null) {
        try {
          sequenceValue = getSequenceValueForDataETag(logTable, dataETag);
        } catch (ODKEntityNotFoundException e) {
          // TODO: log this as a warning -- may be returning a very large set
          sequenceValue = null;
        }
      }

      Query query;
      if (sequenceValue == null) {
        query = buildRowsFromBeginningQuery(logTable, entry, (startCursor == null ? true
            : startCursor.isForwardCursor()));
      } else {
        query = buildRowsSinceQuery(logTable, sequenceValue, (startCursor == null ? true
            : startCursor.isForwardCursor()));
      }

      result = query.execute(startCursor, fetchLimit);
    } finally {
      propsLock.release();
    }

    if (result.entities == null || columns == null) {
      throw new InconsistentStateException("Unable to retrieve rows for table " + tableId + ".");
    }

    // TODO: properly handle reporting of rows that the user no longer has
    // access to because of a access / permissions change for that user and / or
    // row.
    ArrayList<Row> rows = new ArrayList<Row>();
    for (Entity entity : result.entities) {
      Row row = converter.toRowFromLogTable(entity, columns);
      if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
        rows.add(row);
      } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW,
          row.getRowId(), row.getFilterScope())) {
        rows.add(row);
      }
    }
    return new WebsafeRows(computeDiff(rows), result.websafeRefetchCursor,
        result.websafeBackwardCursor, result.websafeResumeCursor, result.hasMore, result.hasPrior);
  }
  
  /**
   * Retrieves a set of rows representing the changes since the given timestamp.
 * @param dateToUse TODO
 * @param startTime - the timestamp to start at in the format of yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS
 * @param endTime - the timestamp to end at in the format of yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS
 * @param startCursor - the cursor to start with
 * @param fetchLimit - the number of rows to return in the response
   * 
   * @return the rows which have changed or been added since the given timestamp
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws InconsistentStateException
   * @throws PermissionDeniedException
   * @throws BadColumnNameException
   * @throws ParseException 
   */
  public WebsafeRows getRowsInTimeRange(String dateToUse, String startTime, String endTime, QueryResumePoint startCursor, int fetchLimit)
      throws ODKDatastoreException, ODKTaskLockException, InconsistentStateException,
      PermissionDeniedException, BadColumnNameException, ParseException {
     
    String query_col = DbLogTable.LAST_UPDATE_DATE_COLUMN_NAME;
     
   if (dateToUse.equals(DbLogTable.SAVEPOINT_TIMESTAMP.getName())) {
      query_col = DbLogTable.SAVEPOINT_TIMESTAMP.getName();
   }

    userPermissions.checkPermission(appId, tableId, TablePermission.READ_ROW);

    List<DbColumnDefinitionsEntity> columns = null;
    WebsafeQueryResult result = null;
    LockTemplate propsLock = new LockTemplate(tableId,
        ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      propsLock.acquire();

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if (schemaETag == null) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      DbTableDefinitionsEntity tableDefn = DbTableDefinitions
          .getDefinition(tableId, schemaETag, cc);
      columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

      DbTable table = DbTable.getRelation(tableDefn, columns, cc);
      DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

      revertPendingChanges(entry, columns, table, logTable);

     SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
     Date startDateToCompare = null;
      String startSequenceValue = null;
      if (startTime != null) {
        try {
        startDateToCompare = sf.parse(startTime);
          startSequenceValue = getSequenceValueForStartTime(logTable, query_col, startTime, startDateToCompare, Direction.ASCENDING);
              //(startCursor == null || startCursor.isForwardCursor()) ? Direction.ASCENDING : Direction.DESCENDING);
        } catch (ODKEntityNotFoundException e) {
          // No values to display should return empty list
          ArrayList<Row> rows = new ArrayList<Row>();
          return new WebsafeRows(rows, null, null, null, false, false);
        }
      } else {
        throw new IllegalArgumentException("startTime must be specified.");
      }
      
      // endTime is an optional parameter
      // and does not have to have a valid value
     Date endDateToCompare = null;
      String endSequenceValue = null;
      if (endTime != null) {
        try {
         endDateToCompare = sf.parse(endTime);
         // For the end time stamp we want the last one
         endSequenceValue = getSequenceValueForEndTime(logTable, query_col, endTime, endDateToCompare, Direction.DESCENDING);
             // (startCursor == null || startCursor.isForwardCursor()) ? Direction.DESCENDING : Direction.ASCENDING);
        } catch (ODKEntityNotFoundException e) {
          // If a sequence values is not found,
          // the query should still work
        }
      } 

      // CAL: From getRowsSince
      Query query;
      if (startSequenceValue == null) {
        throw new IllegalArgumentException("No sequence value exists for the specified startTime.");
      } else {
        query = buildRowsIncludingQuery(logTable, startSequenceValue, endSequenceValue, (startCursor == null ? true
            : startCursor.isForwardCursor()));
      }

      result = query.execute(startCursor, fetchLimit);
    } finally {
      propsLock.release();
    }

    if (result.entities == null || columns == null) {
      throw new InconsistentStateException("Unable to retrieve rows for table " + tableId + ".");
    }

    // TODO: properly handle reporting of rows that the user no longer has
    // access to because of a access / permissions change for that user and / or
    // row.
    ArrayList<Row> rows = new ArrayList<Row>();
    for (Entity entity : result.entities) {
      Row row = converter.toRowFromLogTable(entity, columns);
      if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
        rows.add(row);
      } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW,
          row.getRowId(), row.getFilterScope())) {
        rows.add(row);
      }
    }
    
    List<Row> diffRows = computeDiff(rows);
    
    // Display rows in the order that they were meant to be 
    // displayed
    List<Row> orderedRows = new ArrayList<Row>();
    for (int i = 0; i < rows.size(); i++) {
      if (diffRows.contains(rows.get(i))) {
         orderedRows.add(rows.get(i));
      }
    }
    return new WebsafeRows(orderedRows, result.websafeRefetchCursor,
        result.websafeBackwardCursor, result.websafeResumeCursor, result.hasMore, result.hasPrior);
  }

  /**
   * Perform direct query on DATA_ETAG_AT_MODIFICATION to retrieve the
   * SEQUENCE_VALUE of that row. This is then used to construct the
   * get-rows-since queries.
   *
   * @param dataETag
   * @return SEQUENCE_VALUE of that row
   * @throws ODKDatastoreException
   */
  private String getSequenceValueForDataETag(DbLogTable logTable, String dataETag)
      throws ODKDatastoreException {
    Query query = logTable.query("DataManager.getSequenceValueForDataETag", cc);
    query.equal(DbLogTable.DATA_ETAG_AT_MODIFICATION, dataETag);

    // we need the filter to activate the sort for the sequence value
    query.addFilter(DbLogTable.SEQUENCE_VALUE,
            org.opendatakit.common.persistence.Query.FilterOperation.GREATER_THAN, " ");
    
    query.addSort(DbLogTable.SEQUENCE_VALUE, Direction.DESCENDING);

    List<Entity> values = query.execute();
    if (values == null || values.size() == 0) {
      throw new ODKEntityNotFoundException("ETag " + dataETag + " was not found in log table!");
    } else if (values.size() != 1) {
      // the descending sort on the sequence value ensures we get the last change for 
      // this dataETagAtModification. This assumes the client has gotten all records
      // matching this tag, and is requesting changes *after* the tag.
      logger.info("Multiple records for dataETagAtModification " + dataETag + " count: " + values.size());
    }
    Entity e = values.get(0);
    return e.getString(DbLogTable.SEQUENCE_VALUE);
  }
  
  /**
   * Perform direct query on dateColToUseForCompare to retrieve the
   * SEQUENCE_VALUE of that row. This is then used to construct the
   * query for getting data with that using this start time.
   * 
   * @param logTable - the log table to use
   * @param dateColToUseForCompare - the date field to use for the comparison
   * @param givenTimestamp - the original string value the user passed in
   * @param dateToCompare - the date to compare against the LAST_UPDATE_DATE_COLUMN_NAME
   * @param dir - the sort direction to use when retrieving the data
   * @return SEQUENCE_VALUE of that row
   * @throws ODKDatastoreException
   */
  private String getSequenceValueForStartTime(DbLogTable logTable, String dateColToUseForCompare, String givenTimestamp, Date dateToCompare, Direction dir)
      throws ODKDatastoreException {
    Query query = logTable.query("DataManager.getSequenceValueForTimestamp", cc);
    
    // we need the filter to activate the sort for the sequence value
    query.addFilter(DbLogTable.SEQUENCE_VALUE,
            org.opendatakit.common.persistence.Query.FilterOperation.GREATER_THAN, " ");
    
    query.addSort(DbLogTable.SEQUENCE_VALUE, dir);
    
    // _LAST_UPDATE_DATE is a datetime field
    // _SAVEPOINT_TIMESTAMP is a String field
    if (dateColToUseForCompare.equals(DbLogTable.LAST_UPDATE_DATE_COLUMN_NAME)) {
      query.addFilter(dateColToUseForCompare, 
           org.opendatakit.common.persistence.Query.FilterOperation.GREATER_THAN_OR_EQUAL, dateToCompare);
    } else if (dateColToUseForCompare.equals(DbLogTable.SAVEPOINT_TIMESTAMP.getName())) {
      query.addFilter(dateColToUseForCompare,
           org.opendatakit.common.persistence.Query.FilterOperation.GREATER_THAN_OR_EQUAL, givenTimestamp);
    }
    
    List<Entity> values = query.execute();
    if (values == null || values.size() == 0) {
      throw new ODKEntityNotFoundException("Timestamp " + dateToCompare.toString() + " was not found in log table!");
    } 
    Entity e = values.get(0);
    return e.getString(DbLogTable.SEQUENCE_VALUE);
  }
  
  /**
   * Perform direct query on dateColToUseForCompare to retrieve the
   * SEQUENCE_VALUE of that row. This is then used to construct the
   * query for getting data with that using this end time.
   * 
   * @param logTable - the log table to use
   * @param dateColToUseForCompare - the date field to use for the comparison
   * @param givenTimestamp - the original string value the user passed in
   * @param dateToCompare - the date to compare against the LAST_UPDATE_DATE_COLUMN_NAME
   * @param dir - the sort direction to use when retrieving the data
   * @return SEQUENCE_VALUE of that row
   * @throws ODKDatastoreException
   */
  private String getSequenceValueForEndTime(DbLogTable logTable, String dateColToUseForCompare, String givenTimestamp, Date dateToCompare, Direction dir)
      throws ODKDatastoreException {
    Query query = logTable.query("DataManager.getSequenceValueForTimestamp", cc);
    
    // we need the filter to activate the sort for the sequence value
    query.addFilter(DbLogTable.SEQUENCE_VALUE,
            org.opendatakit.common.persistence.Query.FilterOperation.GREATER_THAN, " ");
    
    query.addSort(DbLogTable.SEQUENCE_VALUE, dir);
    
    // _LAST_UPDATE_DATE is a datetime field
    // _SAVEPOINT_TIMESTAMP is a String field
    if (dateColToUseForCompare.equals(DbLogTable.LAST_UPDATE_DATE_COLUMN_NAME)) {
      query.addFilter(dateColToUseForCompare, 
           org.opendatakit.common.persistence.Query.FilterOperation.LESS_THAN_OR_EQUAL, dateToCompare);
    } else if (dateColToUseForCompare.equals(DbLogTable.SAVEPOINT_TIMESTAMP.getName())) {
      query.addFilter(dateColToUseForCompare,
           org.opendatakit.common.persistence.Query.FilterOperation.LESS_THAN_OR_EQUAL, givenTimestamp);
    }
    
    List<Entity> values = query.execute();
    if (values == null || values.size() == 0) {
      throw new ODKEntityNotFoundException("Timestamp " + dateToCompare.toString() + " was not found in log table!");
    } 
    Entity e = values.get(0);
    return e.getString(DbLogTable.SEQUENCE_VALUE);
  }

  /**
   * @return the query for rows which have been changed or added from the
   *         beginning
   * @throws ODKDatastoreException
   */
  private Query buildRowsFromBeginningQuery(DbLogTable logTable, DbTableEntryEntity entry,
      boolean isForwardCursor) throws ODKDatastoreException {
    Query query = logTable.query("DataManager.buildRowsBeginningFromQuery", cc);
    query.greaterThanOrEqual(DbLogTable.SEQUENCE_VALUE, entry.getAprioriDataSequenceValue());
    if (isForwardCursor) {
      query.sortAscending(DbLogTable.SEQUENCE_VALUE);
    } else {
      query.sortDescending(DbLogTable.SEQUENCE_VALUE);
    }
    return query;
  }

  /**
   * @param sequenceValue
   * @return the query for rows which have been changed or added since the given
   *         sequenceValue
   * @throws ODKDatastoreException
   */
  private Query buildRowsSinceQuery(DbLogTable logTable, String sequenceValue,
      boolean isForwardCursor) throws ODKDatastoreException {
    Query query = logTable.query("DataManager.buildRowsSinceQuery", cc);
    query.greaterThan(DbLogTable.SEQUENCE_VALUE, sequenceValue);
    if (isForwardCursor) {
      query.sortAscending(DbLogTable.SEQUENCE_VALUE);
    } else {
      query.sortDescending(DbLogTable.SEQUENCE_VALUE);
    }
    return query;
  }
  
  /**
   * @param startSequenceValue
   * @param endSequenceValue 
   * @return the query for rows which have been changed or added since the given
   *         sequenceValue
   * @throws ODKDatastoreException
   */
  private Query buildRowsIncludingQuery(DbLogTable logTable, String startSequenceValue,
      String endSequenceValue, boolean isForwardCursor) throws ODKDatastoreException {
    Query query = logTable.query("DataManager.buildRowsIncludingQuery", cc);
    query.greaterThanOrEqual(DbLogTable.SEQUENCE_VALUE, startSequenceValue);
    if (endSequenceValue != null) {
      query.lessThanOrEqual(DbLogTable.SEQUENCE_VALUE, endSequenceValue);
    }

    if (isForwardCursor) {
      query.sortAscending(DbLogTable.SEQUENCE_VALUE);
    } else {
      query.sortDescending(DbLogTable.SEQUENCE_VALUE);
    }
    return query;
  }

  /**
   * Takes a list of rows which are not necessarily all unique and returns a
   * list of unique rows. In the case where there is more than one row with the
   * same rowId, only the last (highest index) row is included in the returned
   * list.
   *
   * @param rows
   *          the rows
   * @return the list of unique rows
   */
  private List<Row> computeDiff(List<Row> rows) {
    Map<String, Row> diff = new HashMap<String, Row>();
    for (Row logRow : rows) {
      diff.put(logRow.getRowId(), logRow);
    }
    return new ArrayList<Row>(diff.values());
  }

  /**
   * Retrieve a row from the table.
   *
   * @param rowId
   *          the id of the row
   * @return the row
   * @throws ODKEntityNotFoundException
   *           if the row with the given id does not exist
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   * @throws ODKTaskLockException
   * @throws BadColumnNameException
   */
  public Row getRow(String rowId) throws ODKEntityNotFoundException, ODKDatastoreException,
      PermissionDeniedException, InconsistentStateException, ODKTaskLockException,
      BadColumnNameException {
    try {
      Validate.notEmpty(rowId);

      userPermissions.checkPermission(appId, tableId, TablePermission.READ_ROW);

      List<DbColumnDefinitionsEntity> columns = null;
      Entity entity = null;
      LockTemplate propsLock = new LockTemplate(tableId,
          ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
      try {
        propsLock.acquire();

        DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
        String schemaETag = entry.getSchemaETag();

        if (schemaETag == null) {
          throw new InconsistentStateException("Schema for table " + tableId
              + " is not yet defined.");
        }

        DbTableDefinitionsEntity tableDefn = DbTableDefinitions.getDefinition(tableId, schemaETag,
            cc);
        columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

        DbTable table = DbTable.getRelation(tableDefn, columns, cc);
        DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

        revertPendingChanges(entry, columns, table, logTable);

        entity = table.getEntity(rowId, cc);

      } finally {
        propsLock.release();
      }

      if (columns == null) {
        throw new InconsistentStateException("Unable to retrieve rows for table " + tableId + ".");
      }

      Row row = converter.toRow(entity, columns);
      if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
        return row;
      } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW,
          row.getRowId(), row.getFilterScope())) {
        return row;
      }
      throw new PermissionDeniedException(String.format("Denied table %s row %s access to user %s",
          tableId, rowId, userPermissions.getOdkTablesUserId()));

    } catch (NullPointerException e) {
      e.printStackTrace();
      throw e;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      throw e;
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKEntityPersistException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw e;
    } catch (BadColumnNameException e) {
      e.printStackTrace();
      throw e;
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw e;
    } catch (InconsistentStateException e) {
      e.printStackTrace();
      throw e;
    }
  }

  public ArrayList<RowOutcome> insertOrUpdateRows(RowList rows) throws ODKEntityPersistException,
      ODKEntityNotFoundException, ODKDatastoreException, ODKTaskLockException,
      ETagMismatchException, BadColumnNameException, PermissionDeniedException,
      InconsistentStateException {
    try {
      Validate.notNull(rows);
      ArrayList<RowOutcome> rowOutcomes = new ArrayList<RowOutcome>();

      userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_ROW);

      List<DbColumnDefinitionsEntity> columns = null;
      Entity entity = null;
      LockTemplate propsLock = new LockTemplate(tableId,
          ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
      try {
        propsLock.acquire();
        Sequencer sequencer = new Sequencer(cc);

        DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
        String schemaETag = entry.getSchemaETag();

        if (schemaETag == null) {
          throw new InconsistentStateException("Schema for table " + tableId
              + " is not yet defined.");
        }

        DbTableDefinitionsEntity tableDefn = DbTableDefinitions.getDefinition(tableId, schemaETag,
            cc);
        columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

        DbTable table = DbTable.getRelation(tableDefn, columns, cc);
        DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

        revertPendingChanges(entry, columns, table, logTable);
        
        DataKeyValueDeepComparator dc = new DataKeyValueDeepComparator(columns);

        for (Row row : rows.getRows()) {

          RowOutcome outcome = null;
          while (true) {
            String rowId = row.getRowId();
            boolean newRowId = false;
            if (rowId == null) {
              newRowId = true;
              rowId = PersistenceUtils.newUri();
              row.setRowId(rowId);
            }
            boolean nullIncomingScope = false;
            Scope scope = row.getFilterScope();
            if (scope == null) {
              nullIncomingScope = true;
              scope = Scope.EMPTY_SCOPE;
              row.setFilterScope(scope);
            }

            try {
              entity = table.getEntity(rowId, cc);

              if (newRowId) {
                // this is an impossible special case --
                // the UUID we generated conflicts; 
                // set row outcome to FAILED
                outcome = new RowOutcome(row);
                row.setRowId(null);
                outcome.setOutcome(OutcomeType.FAILED);
                break;
              }

              if (nullIncomingScope) {
                // preserve the scope of the existing entity if the incoming Row
                // didn't specify one.
                scope = converter.getDbTableFilterScope(entity);
              }

              // confirm that the user has the ability to read the row
              boolean hasPermissions = false;
              if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
                hasPermissions = true;
              } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW,
                  rowId, scope)) {
                hasPermissions = true;
              }

              if (!hasPermissions) {
                outcome = new RowOutcome(row);
                outcome.setOutcome(OutcomeType.DENIED);
                break;
              }

              if (row.isDeleted()) {

                // check for delete access
                hasPermissions = false;
                if (userPermissions
                    .hasPermission(appId, tableId, TablePermission.UNFILTERED_DELETE)) {
                  hasPermissions = true;
                } else if (userPermissions.hasFilterScope(appId, tableId,
                    TablePermission.DELETE_ROW, rowId, scope)) {
                  hasPermissions = true;
                }

              } else {
                // confirm they have the ability to write to it
                hasPermissions = false;
                if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_WRITE)) {
                  hasPermissions = true;
                } else if (userPermissions.hasFilterScope(appId, tableId,
                    TablePermission.WRITE_ROW, rowId, scope)) {
                  hasPermissions = true;
                }
              }

              if (!hasPermissions) {
                outcome = new RowOutcome(row);
                outcome.setOutcome(OutcomeType.DENIED);
                break;
              }

              String rowETag = entity.getString(DbTable.ROW_ETAG);
              String currentETag = row.getRowETag();
              if (currentETag == null || !currentETag.equals(rowETag)) {

                // Take the hit to convert the row we have.
                // If the row matches everywhere except on the rowETag, return
                // it.
                Row currentRow = converter.toRow(entity, columns);
                if (row.hasMatchingSignificantFieldValues(currentRow, dc)) {
                  outcome = new RowOutcome(currentRow);
                  outcome.setOutcome(OutcomeType.SUCCESS);
                  break;
                }

                // if null, then the client thinks they are creating a new row.
                // The rows may be identical, but leave that to the client to
                // determine
                // trigger client-side conflict resolution.
                // Otherwise, if there is a mis-match, then the client needs to
                // pull and
                // perform client-side conflict resolution on the changes
                // already up on the server.
                outcome = new RowOutcome(currentRow);
                outcome.setOutcome(OutcomeType.IN_CONFLICT);
                break;
              }

            } catch (ODKEntityNotFoundException e) {

              if (row.isDeleted()) {
                // not found on server -- it is unclear whether
                // the server should create the row and a deletion
                // marker for the row, or whether it should just
                // silently do nothing and return success.
                //
                // set row outcome to FAILED (client must decide).
                outcome = new RowOutcome(row);
                outcome.setOutcome(OutcomeType.FAILED);
                break;
              }
 
                // require unfiltered write permissions to create a new record
              userPermissions.checkPermission(appId, tableId, TablePermission.UNFILTERED_WRITE);

              newRowId = true;

              // initialization for insert...
              entity = table.newEntity(rowId, cc);
              entity.set(DbTable.CREATE_USER, userPermissions.getOdkTablesUserId());
            }

            // OK we are able to update or insert or delete the record -- mark
            // as pending change.

            // get new dataETag
            String dataETagAtModification = PersistenceUtils.newUri();
            entry.setPendingDataETag(dataETagAtModification);
            entry.put(cc);

            String previousRowETag;
            Entity logEntity;

            if (row.isDeleted()) {

              // remember the previous row ETag so we can chain revisions in the
              // DbLogTable
              previousRowETag = entity.getString(DbTable.ROW_ETAG);

              // update the row ETag and deletion status
              entity.set(DbTable.ROW_ETAG, PersistenceUtils.newUri());
              entity.set(DbTable.DELETED, true);

            } else {
              // this will be null of the entity is newly created...
              previousRowETag = row.getRowETag();

              // update the fields in the DbTable entity...
              creator.setRowFields(entity, PersistenceUtils.newUri(), dataETagAtModification,
                  userPermissions.getOdkTablesUserId(), false, scope, row.getFormId(),
                  row.getLocale(), row.getSavepointType(), row.getSavepointTimestamp(),
                  row.getSavepointCreator(), row.getValues(), columns);

            }

            // create log table entry
            logEntity = creator.newLogEntity(logTable, dataETagAtModification, previousRowETag,
                entity, columns, sequencer, cc);

            // commit the log change to the database (must be done first!)
            DbLogTable.putEntity(logEntity, cc);
            // commit the row change
            DbTable.putEntity(entity, cc);

            // commit change
            entry.setDataETag(entry.getPendingDataETag());
            entry.setPendingDataETag(null);
            entry.put(cc);

            Row updatedRow = converter.toRow(entity, columns);
            outcome = new RowOutcome(updatedRow);
            outcome.setOutcome(OutcomeType.SUCCESS);
            break;
          }
          rowOutcomes.add(outcome);
        }
      } finally {
        propsLock.release();
      }

      if (columns == null) {
        throw new InconsistentStateException("Unable to retrieve rows for table " + tableId + ".");
      }
      return rowOutcomes;
    } catch (NullPointerException e) {
      e.printStackTrace();
      throw e;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      throw e;
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKEntityPersistException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw e;
    } catch (BadColumnNameException e) {
      e.printStackTrace();
      throw e;
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw e;
    } catch (InconsistentStateException e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * Inserts or Updates a row. If inserting, the row must not already exist or
   * the eTag for the row being inserted must exactly match that on the server.
   *
   * @param af
   *          -- authentication filter to be applied to this action
   * @param row
   *          the row to update. See
   *          {@link Row#forInsert(String, String, java.util.Map)}.
   *          {@link Row#forUpdate(String, String, java.util.Map)}
   *          {@link Row#isDeleted()}, {@link Row#getCreateUser()}, and
   *          {@link Row#getLastUpdateUser()} will be ignored if they are set.
   * @return a copy of the row that was inserted or updated, with the row's
   *         rowETtag populated with the new rowETtag. If the original passed in
   *         row had a null rowId, the row will contain the generated rowId.
   * @throws ODKEntityNotFoundException
   *           if the passed in row does not exist
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws ETagMismatchException
   *           if the passed in row has a different rowETtag from the row in the
   *           datastore (e.g., on insert, the row already exists, or on update,
   *           there is conflict that needs to be resolved).
   * @throws BadColumnNameException
   *           if the passed in row set a value for a column which doesn't exist
   *           in the table
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   *
   */
  public Row insertOrUpdateRow(Row row) throws ODKEntityPersistException,
      ODKEntityNotFoundException, ODKDatastoreException, ODKTaskLockException,
      ETagMismatchException, BadColumnNameException, PermissionDeniedException,
      InconsistentStateException {
    try {
      Validate.notNull(row);

      userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_ROW);

      List<DbColumnDefinitionsEntity> columns = null;
      Entity entity = null;
      LockTemplate propsLock = new LockTemplate(tableId,
          ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
      try {
        propsLock.acquire();
        Sequencer sequencer = new Sequencer(cc);

        DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
        String schemaETag = entry.getSchemaETag();

        if (schemaETag == null) {
          throw new InconsistentStateException("Schema for table " + tableId
              + " is not yet defined.");
        }

        DbTableDefinitionsEntity tableDefn = DbTableDefinitions.getDefinition(tableId, schemaETag,
            cc);
        columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

        DbTable table = DbTable.getRelation(tableDefn, columns, cc);
        DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

        revertPendingChanges(entry, columns, table, logTable);
        
        DataKeyValueDeepComparator dc = new DataKeyValueDeepComparator(columns);

        String rowId = row.getRowId();
        boolean newRowId = false;
        if (rowId == null) {
          newRowId = true;
          rowId = PersistenceUtils.newUri();
          row.setRowId(rowId);
        }
        boolean nullIncomingScope = false;
        Scope scope = row.getFilterScope();
        if (scope == null) {
          nullIncomingScope = true;
          scope = Scope.EMPTY_SCOPE;
          row.setFilterScope(scope);
        }

        try {
          entity = table.getEntity(rowId, cc);

          if (newRowId) {
            throw new InconsistentStateException(
                "Synthesized rowId collides with existing row in table " + tableId + ".");
          }

          if (nullIncomingScope) {
            // preserve the scope of the existing entity if the incoming Row
            // didn't specify one.
            scope = converter.getDbTableFilterScope(entity);
          }

          // confirm that the user has the ability to read the row
          boolean hasPermissions = false;
          if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
            hasPermissions = true;
          } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW,
              rowId, scope)) {
            hasPermissions = true;
          }

          if (!hasPermissions) {
            throw new PermissionDeniedException(String.format(
                "Denied table %s row %s read access to user %s", tableId, rowId,
                userPermissions.getOdkTablesUserId()));
          }

          // confirm they have the ability to write to it
          hasPermissions = false;
          if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_WRITE)) {
            hasPermissions = true;
          } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.WRITE_ROW,
              rowId, scope)) {
            hasPermissions = true;
          }

          if (!hasPermissions) {
            throw new PermissionDeniedException(String.format(
                "Denied table %s row %s read access to user %s", tableId, rowId,
                userPermissions.getOdkTablesUserId()));
          }

          String rowETag = entity.getString(DbTable.ROW_ETAG);
          String currentETag = row.getRowETag();
          if (currentETag == null || !currentETag.equals(rowETag)) {

            // Take the hit to convert the row we have.
            // If the row matches everywhere except on the rowETag, return it.
            Row currentRow = converter.toRow(entity, columns);
            if (row.hasMatchingSignificantFieldValues(currentRow, dc)) {
              return currentRow;
            }

            // if null, then the client thinks they are creating a new row.
            // The rows may be identical, but leave that to the client to
            // determine
            // trigger client-side conflict resolution.
            // Otherwise, if there is a mis-match, then the client needs to pull
            // and
            // perform client-side conflict resolution on the changes already up
            // on the server.
            throw new ETagMismatchException(String.format("rowETag %s does not match %s "
                + "for rowId %s", currentETag, rowETag, rowId));
          }

        } catch (ODKEntityNotFoundException e) {

          // require unfiltered write permissions to create a new record
          userPermissions.checkPermission(appId, tableId, TablePermission.UNFILTERED_WRITE);

          newRowId = true;
          // initialization for insert...
          entity = table.newEntity(rowId, cc);
          entity.set(DbTable.CREATE_USER, userPermissions.getOdkTablesUserId());
        }

        // OK we are able to update or insert the record -- mark as pending
        // change.

        // get new dataETag
        String dataETagAtModification = PersistenceUtils.newUri();
        entry.setPendingDataETag(dataETagAtModification);
        entry.put(cc);

        // this will be null of the entity is newly created...
        String previousRowETag = row.getRowETag();

        // update the fields in the DbTable entity...
        creator.setRowFields(entity, PersistenceUtils.newUri(), dataETagAtModification,
            userPermissions.getOdkTablesUserId(), false, scope, row.getFormId(), row.getLocale(),
            row.getSavepointType(), row.getSavepointTimestamp(), row.getSavepointCreator(),
            row.getValues(), columns);

        // create log table entry
        Entity logEntity = creator.newLogEntity(logTable, dataETagAtModification, previousRowETag,
            entity, columns, sequencer, cc);

        // update db
        DbLogTable.putEntity(logEntity, cc);
        DbTable.putEntity(entity, cc);

        // commit change
        entry.setDataETag(entry.getPendingDataETag());
        entry.setPendingDataETag(null);
        entry.put(cc);

      } finally {
        propsLock.release();
      }

      if (columns == null) {
        throw new InconsistentStateException("Unable to retrieve rows for table " + tableId + ".");
      }

      Row updatedRow = converter.toRow(entity, columns);
      return updatedRow;
    } catch (NullPointerException e) {
      e.printStackTrace();
      throw e;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      throw e;
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKEntityPersistException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw e;
    } catch (ETagMismatchException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw e;
    } catch (BadColumnNameException e) {
      e.printStackTrace();
      throw e;
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw e;
    } catch (InconsistentStateException e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * Delete a row.
   *
   * @param rowId
   *          the row to delete.
   * @param currentRowETag
   *          the ETag for that row, as known to the requester
   * @return returns the new dataETag that is current after deleting the row.
   * @throws ODKEntityNotFoundException
   *           if there is no row with the given id in the datastore
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   * @throws BadColumnNameException
   * @throws ETagMismatchException
   */
  public String deleteRow(String rowId, String currentRowETag) throws ODKEntityNotFoundException,
      ODKDatastoreException, ODKTaskLockException, PermissionDeniedException,
      InconsistentStateException, BadColumnNameException, ETagMismatchException {
    try {
      Validate.notNull(rowId);
      Validate.notBlank(rowId);

      userPermissions.checkPermission(appId, tableId, TablePermission.DELETE_ROW);
      String dataETagAtModification = null;
      LockTemplate propsLock = new LockTemplate(tableId,
          ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
      try {
        propsLock.acquire();
        Sequencer sequencer = new Sequencer(cc);

        DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
        String schemaETag = entry.getSchemaETag();

        if (schemaETag == null) {
          throw new InconsistentStateException("Schema for table " + tableId
              + " is not yet defined.");
        }

        DbTableDefinitionsEntity tableDefn = DbTableDefinitions.getDefinition(tableId, schemaETag,
            cc);
        List<DbColumnDefinitionsEntity> columns = DbColumnDefinitions
            .query(tableId, schemaETag, cc);

        DbTable table = DbTable.getRelation(tableDefn, columns, cc);
        DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

        revertPendingChanges(entry, columns, table, logTable);

        Entity entity = table.getEntity(rowId, cc);

        // entity exists (or we would have thrown an
        // ODKEntityNotFoundException).
        String serverRowETag = entity.getString(DbTable.ROW_ETAG);
        if (!currentRowETag.equals(serverRowETag)) {

          // if null, then the client thinks they are creating a new row.
          // The rows may be identical, but leave that to the client to
          // determine
          // trigger client-side conflict resolution.
          // Otherwise, if there is a mis-match, then the client needs to pull
          // and
          // perform client-side conflict resolution on the changes already up
          // on the server.
          throw new ETagMismatchException(String.format("rowETag %s does not match %s "
              + "for rowId %s", currentRowETag, serverRowETag, rowId));
        }

        Scope scope = converter.getDbTableFilterScope(entity);

        // check for read access
        boolean hasPermissions = false;
        if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
          hasPermissions = true;
        } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW, rowId,
            scope)) {
          hasPermissions = true;
        }

        if (!hasPermissions) {
          throw new PermissionDeniedException(String.format(
              "Denied table %s row %s read access to user %s", tableId, rowId,
              userPermissions.getOdkTablesUserId()));
        }

        // check for delete access
        hasPermissions = false;
        if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_DELETE)) {
          hasPermissions = true;
        } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.DELETE_ROW,
            rowId, scope)) {
          hasPermissions = true;
        }

        if (!hasPermissions) {
          throw new PermissionDeniedException(String.format(
              "Denied table %s row %s delete access to user %s", tableId, rowId,
              userPermissions.getOdkTablesUserId()));
        }

        // get new dataETag
        dataETagAtModification = PersistenceUtils.newUri();
        entry.setPendingDataETag(dataETagAtModification);
        entry.put(cc);

        // remember the previous row ETag so we can chain revisions in the
        // DbLogTable
        String previousRowETag = entity.getString(DbTable.ROW_ETAG);

        // update the row ETag and deletion status
        entity.set(DbTable.ROW_ETAG, PersistenceUtils.newUri());
        entity.set(DbTable.DELETED, true);

        // create log table entry
        Entity logEntity = creator.newLogEntity(logTable, dataETagAtModification, previousRowETag,
            entity, columns, sequencer, cc);

        // commit the log change to the database (must be done first!)
        DbLogTable.putEntity(logEntity, cc);
        // commit the row change
        DbTable.putEntity(entity, cc);

        // NOTE: the DbTableInstanceFiles objects are never deleted unless the
        // table is dropped.
        // They hold the file attachments and are eferred to by the records in
        // the DbLogTable
        // even if the row is deleted from the set of active records (i.e.,
        // DbTable).

        // update the TableEntry to reflect the completion of the change
        entry.setDataETag(entry.getPendingDataETag());
        entry.setPendingDataETag(null);
        entry.put(cc);

      } finally {
        propsLock.release();
      }

      return dataETagAtModification;
    } catch (NullPointerException e) {
      e.printStackTrace();
      throw e;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      throw e;
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKEntityPersistException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw e;
    } catch (ETagMismatchException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw e;
    } catch (BadColumnNameException e) {
      e.printStackTrace();
      throw e;
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw e;
    } catch (InconsistentStateException e) {
      e.printStackTrace();
      throw e;
    }
  }
}