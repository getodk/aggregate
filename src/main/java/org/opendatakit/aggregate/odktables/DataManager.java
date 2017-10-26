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
import org.opendatakit.aggregate.odktables.exception.TableDataETagMismatchException;
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
import org.opendatakit.aggregate.odktables.rest.entity.ChangeSetList;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.opendatakit.aggregate.odktables.rest.entity.RowList;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome.OutcomeType;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcomeList;
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

    public final String dataETag;
    public final String websafeRefetchCursor;
    public final String websafeBackwardCursor;
    public final String websafeResumeCursor;
    public final boolean hasMore;
    public final boolean hasPrior;

    public WebsafeRows(List<Row> rows, String dataETag, 
        String websafeRefetchCursor, String websafeBackwardCursor,
        String websafeResumeCursor, boolean hasMore, boolean hasPrior) {
      this.rows = rows;
      this.dataETag = dataETag;
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
            EntityConverter.getDbLogTableRowFilterScope(priorLogEntity),
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

    String currentDataETag = null;
    
    List<DbColumnDefinitionsEntity> columns = null;
    WebsafeQueryResult result = null;
    OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId,
        ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.SHORT, cc);
    try {
      propsLock.acquire();

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if (schemaETag == null) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      currentDataETag = entry.getDataETag();
      
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
          row.getRowId(), /* row.getFilterScope() */ Scope.EMPTY_SCOPE )) {
        rows.add(row);
      }
    }
    return new WebsafeRows(rows, currentDataETag, result.websafeRefetchCursor, result.websafeBackwardCursor,
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

    String currentDataETag = null;
    
    List<DbColumnDefinitionsEntity> columns = null;
    WebsafeQueryResult result = null;
    OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId,
        ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.SHORT, cc);
    try {
      propsLock.acquire();

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if (schemaETag == null) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      currentDataETag = entry.getDataETag();
      
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
          row.getRowId(), /* row.getFilterScope() */ Scope.EMPTY_SCOPE)) {
        rows.add(row);
      }
    }
    return new WebsafeRows(computeDiff(rows), currentDataETag, result.websafeRefetchCursor,
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
    
    String currentDataETag = null;
    
    List<DbColumnDefinitionsEntity> columns = null;
    WebsafeQueryResult result = null;
    OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId,
        ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.SHORT, cc);
    try {
      propsLock.acquire();

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if (schemaETag == null) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      currentDataETag = entry.getDataETag();

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
          return new WebsafeRows(rows, currentDataETag, null, null, null, false, false);
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
          row.getRowId(), /* row.getFilterScope() */ Scope.EMPTY_SCOPE)) {
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
    return new WebsafeRows(orderedRows, currentDataETag, result.websafeRefetchCursor,
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
      OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId,
          ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.SHORT, cc);
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
          row.getRowId(), /* row.getFilterScope() */ Scope.EMPTY_SCOPE)) {
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

  private void prepareRowForInsertUpdateOrDelete(BulkRowObjWrapper rowWrapper,
      List<DbColumnDefinitionsEntity> columns, DbTable table,
      DataKeyValueDeepComparator dc) throws ODKDatastoreException,
      PermissionDeniedException {

    Row row = rowWrapper.getRow();
    Entity entity = rowWrapper.getEntity();
    String rowId = rowWrapper.getRowId();
    RowFilterScope rowFilterScope = rowWrapper.getRowFilterScope();

    if (rowWrapper.hasNullIncomingScope() && entity.isFromDatabase()) {
      // use the row scope for the existing entity if the incoming Row didn't specify one.
      rowFilterScope = EntityConverter.getDbTableRowFilterScope(entity);
      rowWrapper.setRowFilterScope(rowFilterScope);
    }

    // confirm that the user has the ability to read the row
    boolean hasPermissions = false;
    if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
      hasPermissions = true;
    } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW, rowId,
        /* some transform of rowFilterScope */ Scope.EMPTY_SCOPE)) {
      hasPermissions = true;
    }

    if (!hasPermissions) {
      rowWrapper.setOutcome(OutcomeType.DENIED);
      return;
    }

    if (row.isDeleted()) {

      // check for delete access
      hasPermissions = false;
      if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_DELETE)) {
        hasPermissions = true;
      } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.DELETE_ROW, rowId,
          /* some transform of rowFilterScope */ Scope.EMPTY_SCOPE)) {
        hasPermissions = true;
      }

    } else {
      // confirm they have the ability to write to it
      hasPermissions = false;
      if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_WRITE)) {
        hasPermissions = true;
      } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.WRITE_ROW, rowId,
          /* some transform of rowFilterScope */ Scope.EMPTY_SCOPE)) {
        hasPermissions = true;
      }
    }

    if (!hasPermissions) {
      rowWrapper.setOutcome(OutcomeType.DENIED);
      return;
    }

    if (entity.isFromDatabase()) {
      String rowETag = entity.getString(DbTable.ROW_ETAG);
      String currentETag = row.getRowETag();
      // there was an existing record for the row in the database...
      if (currentETag == null || !currentETag.equals(rowETag)) {
        // Take the hit to convert the row we have.
        Row currentRow = converter.toRow(entity, columns);
        if (row.hasMatchingSignificantFieldValues(currentRow, dc)) {
          // If the row matches everywhere except on the rowETag,
          // return the row on the server.
          rowWrapper.setOutcome(currentRow, OutcomeType.SUCCESS);
          return;
        }

        // Otherwise, if there is a mis-match, then the client needs to
        // perform client-side conflict resolution on the changes already
        // up on the server. Return the row on the server.
        rowWrapper.setOutcome(currentRow, OutcomeType.IN_CONFLICT);
        return;
      }
    }
  }

  /**
   * The tableUri of the returned rowOutcomeList is null.
   *  
   * @param rows
   * @return
   * @throws ODKEntityPersistException
   * @throws ODKEntityNotFoundException
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws BadColumnNameException
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   * @throws TableDataETagMismatchException 
   */
  public RowOutcomeList insertOrUpdateRows(RowList rows) throws ODKEntityPersistException,
      ODKEntityNotFoundException, ODKDatastoreException, ODKTaskLockException,
      BadColumnNameException, PermissionDeniedException,
      InconsistentStateException, TableDataETagMismatchException {

    long startTime = System.currentTimeMillis();

    try {
      Validate.notNull(rows);
      ArrayList<RowOutcome> rowOutcomes = new ArrayList<RowOutcome>();

      userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_ROW);
      
      String dataETagAtModification = null;
      
      OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId,
          ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.SHORT, cc);

      List<DbColumnDefinitionsEntity> columns = null;
      try {
        propsLock.acquire();
        Sequencer sequencer = new Sequencer(cc);

        DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
        String schemaETag = entry.getSchemaETag();

        if (schemaETag == null) {
          throw new InconsistentStateException("Schema for table " + tableId
              + " is not yet defined.");
        }
        
        String dataETag = entry.getDataETag();
        if (!((dataETag == null) ? (rows.getDataETag() == null) : dataETag.equals(rows.getDataETag())) ) {
          throw new TableDataETagMismatchException("The dataETag for table " + tableId + " does not match that supplied in the RowList");          
        }

        DbTableDefinitionsEntity tableDefn = DbTableDefinitions.getDefinition(tableId, schemaETag,
            cc);
        columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

        DbTable table = DbTable.getRelation(tableDefn, columns, cc);
        DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

        revertPendingChanges(entry, columns, table, logTable);

        logger.info("Before loop Time elpased: " + (System.currentTimeMillis() - startTime));

        DataKeyValueDeepComparator dc = new DataKeyValueDeepComparator(columns);

        // mark as pending change.
        // get new dataETag
        dataETagAtModification = PersistenceUtils.newUri();
        entry.setPendingDataETag(dataETagAtModification);
        entry.put(cc);

        List<Entity> entityInsertList = new ArrayList<Entity>();
        List<Entity> entityUpdateList = new ArrayList<Entity>();
        List<Entity> logEntityList = new ArrayList<Entity>();

        ArrayList<BulkRowObjWrapper> rowWrapperList = new ArrayList<BulkRowObjWrapper>();

        for (Row row : rows.getRows()) {

          BulkRowObjWrapper rowWrapper = new BulkRowObjWrapper(row);

          // and add row wrapper for bulk processing
          rowWrapperList.add(rowWrapper);

          Entity entity = null;
          try {
            entity = table.getEntity(rowWrapper.getRowId(), cc);

            if (rowWrapper.hasNewRowId()) {
              // yikes! -- generated UUID conflicts with an existing one.
              rowWrapper.setOutcome(OutcomeType.IN_CONFLICT);
              rowWrapperList.add(rowWrapper);
              continue;
            }

          } catch (ODKEntityNotFoundException e) {

            if (row.isDeleted()) {
              rowWrapper.setOutcome(OutcomeType.DENIED);
              rowWrapperList.add(rowWrapper);
              continue;
            }

            // presumptive initialization for insert...
            entity = table.newEntity(rowWrapper.getRowId(), cc);
            entity.set(DbTable.CREATE_USER, userPermissions.getOdkTablesUserId());
          }

          // add entity to row wrapper
          rowWrapper.setEntity(entity);

          // determine whether the update or insert should go through or not.
          // if entity.isFromDatabase() is true, it is an update or delete
          prepareRowForInsertUpdateOrDelete(rowWrapper, columns, table, dc);

          // OK we are able to update or insert or delete the record
          if (!rowWrapper.outcomeAlreadySet()) {
            RowFilterScope rowFilterScope = rowWrapper.getRowFilterScope();

            String previousRowETag;

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
                  userPermissions.getOdkTablesUserId(), false, rowFilterScope, row.getFormId(),
                  row.getLocale(), row.getSavepointType(), row.getSavepointTimestamp(),
                  row.getSavepointCreator(), row.getValues(), columns);

            }

            // create log table entry
            Entity logEntity = creator.newLogEntity(logTable, dataETagAtModification,
                previousRowETag, entity, columns, sequencer, cc);

            logEntityList.add(logEntity);

            if (entity.isFromDatabase()) {
              entityUpdateList.add(entity);
            } else {
              entityInsertList.add(entity);
            }
          }

        }

        // commit the log change to the database (must be done first!)
        if (!logEntityList.isEmpty()) {
          logTable.bulkAlterEntities(logEntityList, cc);
        }

        // commit the row updates
        if (!entityUpdateList.isEmpty()) {
          table.bulkAlterEntities(entityUpdateList, cc);
        }
        // commit the row inserts
        if (!entityInsertList.isEmpty()) {
          table.bulkAlterEntities(entityInsertList, cc);
        }

        // commit change
        entry.setDataETag(entry.getPendingDataETag());
        entry.setPendingDataETag(null);
        entry.put(cc);

        for (BulkRowObjWrapper rowWrapper : rowWrapperList) {
          if (!rowWrapper.outcomeAlreadySet()) {
            // we need to return the fields from the entity we upserted.
            Row newServer = converter.toRow(rowWrapper.getEntity(), columns);
            rowWrapper.setOutcome(newServer, OutcomeType.SUCCESS);
          }
          // update the outcomes set...
          rowOutcomes.add(rowWrapper.getOutcome());
        }

        logger.info("End loop Time elpased: " + (System.currentTimeMillis() - startTime));
      } finally {
        propsLock.release();
      }

      if (columns == null) {
        throw new InconsistentStateException("Unable to retrieve rows for table " + tableId + ".");
      }

      if (rows != null) {
        long time = (System.currentTimeMillis() - startTime);
        int numRows = rows.getRows().size();
        logger.info("Time: " + time + " size: " + numRows + " per iteration " + (time / numRows));
      }

      return new RowOutcomeList(rowOutcomes, dataETagAtModification);
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
      OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId,
          ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.SHORT, cc);
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
        RowFilterScope rowFilterScope = row.getRowFilterScope();
        if (rowFilterScope == null) {
          nullIncomingScope = true;
          rowFilterScope = RowFilterScope.EMPTY_ROW_FILTER;
          row.setRowFilterScope(rowFilterScope);
        }

        try {
          entity = table.getEntity(rowId, cc);

          if (newRowId) {
            throw new InconsistentStateException(
                "Synthesized rowId collides with existing row in table " + tableId + ".");
          }

          if (nullIncomingScope) {
            // preserve the scope of the existing entity if the incoming Row didn't specify one.
        	  rowFilterScope = EntityConverter.getDbTableRowFilterScope(entity);
        	  // and update the value
        	  row.setRowFilterScope(rowFilterScope);
          }

          // confirm that the user has the ability to read the row
          boolean hasPermissions = false;
          if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
            hasPermissions = true;
          } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW,
              rowId, /* row.getFilterScope() */ Scope.EMPTY_SCOPE)) {
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
              rowId, /* row.getFilterScope() */ Scope.EMPTY_SCOPE)) {
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
            userPermissions.getOdkTablesUserId(), false, rowFilterScope, row.getFormId(), row.getLocale(),
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
      OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId,
          ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.SHORT, cc);
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

        RowFilterScope rowFilterScope = EntityConverter.getDbTableRowFilterScope(entity);

        // check for read access
        boolean hasPermissions = false;
        if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
          hasPermissions = true;
        } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW, rowId,
            /* scope from dbTable */ Scope.EMPTY_SCOPE)) {
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
            rowId, /* scope from dbTable */ Scope.EMPTY_SCOPE)) {
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

  /**
   * Returns the dataETag values that were applied after the given dataETag
   * and/or sequenceValue.
   * 
   * There is no meaningful ordering of this returned set. For consistency,
   * the list is ordered by the dataETag values themselves. The returned object
   * includes the dataETag at the beginning of the processing of this request 
   * and a sequenceValue. Either of these can be used to return any changes 
   * made after this request.
   * 
   * @param dataETag
   * @param sequenceValue
   * @return
   * @throws PermissionDeniedException
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws InconsistentStateException
   * @throws BadColumnNameException
   */
  public ChangeSetList getChangeSetsSince(String dataETag, String sequenceValue) throws PermissionDeniedException, ODKDatastoreException, ODKTaskLockException, InconsistentStateException, BadColumnNameException {

    userPermissions.checkPermission(appId, tableId, TablePermission.READ_ROW);

    String currentDataETag = null;
    String retrievalSequenceValue = null;
    
    List<DbColumnDefinitionsEntity> columns = null;
    List<?> result = null;
    OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId,
        ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.SHORT, cc);
    try {
      propsLock.acquire();
      Sequencer sequencer = new Sequencer(cc);
      retrievalSequenceValue = sequencer.getNextSequenceValue();

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if (schemaETag == null) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      currentDataETag = entry.getDataETag();
      
      DbTableDefinitionsEntity tableDefn = DbTableDefinitions
          .getDefinition(tableId, schemaETag, cc);
      columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

      DbTable table = DbTable.getRelation(tableDefn, columns, cc);
      DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

      revertPendingChanges(entry, columns, table, logTable);

      String unifiedSequenceValue = null;
      if (dataETag != null) {
        try {
          unifiedSequenceValue = getSequenceValueForDataETag(logTable, dataETag);
        } catch (ODKEntityNotFoundException e) {
          // TODO: log this as a warning -- may be returning a very large set
          unifiedSequenceValue = null;
        }
      }

      if ( sequenceValue != null && 
          (unifiedSequenceValue == null || (unifiedSequenceValue.compareTo(sequenceValue) < 0)) ) {
        unifiedSequenceValue = sequenceValue;
      }
      
      Query query;
      if (unifiedSequenceValue == null) {
        query = buildRowsFromBeginningQuery(logTable, entry, true);
      } else {
        query = buildRowsSinceQuery(logTable, unifiedSequenceValue, true);
      }
      
      result = query.getDistinct(DbLogTable.DATA_ETAG_AT_MODIFICATION);
    } finally {
      propsLock.release();
    }

    if (result == null || result.isEmpty() ) {
      return new ChangeSetList(null, currentDataETag, retrievalSequenceValue);
    }

    ArrayList<String> dataETags = new ArrayList<String>();
    for (Object o : result) {
      String value = (String) o;
      dataETags.add(value);
    }
    
    return new ChangeSetList(dataETags, currentDataETag, retrievalSequenceValue);
  }

  /**
   * Returns the set of rows for a given dataETag (changeSet).
   * If the isActive flag is true, then return only the subset
   * of these that are the most current (in the DbTable).
   * Otherwise, return the full set of changes from the DbLogTable.
   * 
   * @param dataETag
   * @param isActive
   * @param startCursor
   * @param fetchLimit
   * @return
   * @throws PermissionDeniedException
   * @throws ODKDatastoreException
   * @throws InconsistentStateException
   * @throws ODKTaskLockException
   * @throws BadColumnNameException
   */
  public WebsafeRows getChangeSetRows(String dataETag, boolean isActive,
      QueryResumePoint startCursor, int fetchLimit) throws PermissionDeniedException, ODKDatastoreException, InconsistentStateException, ODKTaskLockException, BadColumnNameException {

    userPermissions.checkPermission(appId, tableId, TablePermission.READ_ROW);

    String currentDataETag = null;
    
    List<DbColumnDefinitionsEntity> columns = null;
    WebsafeQueryResult result = null;
    OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId,
        ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.SHORT, cc);
    try {
      propsLock.acquire();

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if (schemaETag == null) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      currentDataETag = entry.getDataETag();
      
      DbTableDefinitionsEntity tableDefn = DbTableDefinitions
          .getDefinition(tableId, schemaETag, cc);
      columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

      DbTable table = DbTable.getRelation(tableDefn, columns, cc);
      DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

      revertPendingChanges(entry, columns, table, logTable);

      boolean isForwardCursor = (startCursor == null ? true
          : startCursor.isForwardCursor());
      
      if ( isActive ) {
        // query is against DbTable
        Query query = table.query("DataManager.getChangeSetRows", cc);
        query.equal(DbTable.DATA_ETAG_AT_MODIFICATION, dataETag);
        if (isForwardCursor) {
          query.greaterThan(DbTable.ROW_ETAG,"");
          query.sortAscending(DbTable.ROW_ETAG);
        } else {
          query.greaterThan(DbTable.ROW_ETAG,"");
          query.sortDescending(DbTable.ROW_ETAG);
        }

        result = query.execute(startCursor, fetchLimit);
        
      } else {
        // query is against DbLogTable
        Query query = logTable.query("DataManager.getChangeSetRows", cc);
        query.equal(DbLogTable.DATA_ETAG_AT_MODIFICATION, dataETag);
        if (isForwardCursor) {
          query.greaterThan(DbLogTable.ROW_ID,"");
          query.sortAscending(DbLogTable.ROW_ID);
        } else {
          query.greaterThan(DbLogTable.ROW_ID,"");
          query.sortDescending(DbLogTable.ROW_ID);
        }
        
        result = query.execute(startCursor, fetchLimit);
      }

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
    if ( isActive ) {
      // query is against DbTable
      for (Entity entity : result.entities) {
        Row row = converter.toRow(entity, columns);
        if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
          rows.add(row);
        } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW,
            row.getRowId(), /* row.getFilterScope() */ Scope.EMPTY_SCOPE)) {
          rows.add(row);
        }
      }

    } else {
      // query is against DbLogTable
      for (Entity entity : result.entities) {
        Row row = converter.toRowFromLogTable(entity, columns);
        if (userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
          rows.add(row);
        } else if (userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW,
            row.getRowId(), /* row.getFilterScope() */ Scope.EMPTY_SCOPE)) {
          rows.add(row);
        }
      }
      
    }
    return new WebsafeRows(computeDiff(rows), currentDataETag, result.websafeRefetchCursor,
        result.websafeBackwardCursor, result.websafeResumeCursor, result.hasMore, result.hasPrior);
  }
}