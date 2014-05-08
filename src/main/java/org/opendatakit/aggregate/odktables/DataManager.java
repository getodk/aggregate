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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
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
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Query;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

/**
 * Manages read, insert, update, and delete operations on the rows of a table.
 *
 * @author the.dylan.price@gmail.com
 * @author sudar.sam@gmail.com
 *
 */

public class DataManager {
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
  public DataManager(String appId, String tableId, TablesUserPermissions userPermissions, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException {
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

  private void revertPendingChanges(DbTableEntryEntity entry, List<DbColumnDefinitionsEntity> columns,
        DbTable table, DbLogTable logTable) throws ODKDatastoreException, BadColumnNameException {

    // we have nothing to do if the pending dataETag is null...
    String dataETag = entry.getPendingDataETag();
    if ( dataETag == null ) {
      return;
    }

    // search for log entries matching the TableEntry dataETag
    // log entries are written first, so these should exist, and the
    // row entries may or may not reflect the log contents.

    Query query = logTable.query("DataManager.revertPendingChanges", cc);
    query.equal(DbLogTable.DATA_ETAG_AT_MODIFICATION, dataETag);
    List<Entity> logEntries = query.execute();

    for ( Entity logEntity : logEntries ) {
      // Log entries maintain the history of previous rowETags
      // Chain back through that to get the previous log record.
      // If the previous rowETag is null, it means that the rowId
      // did not exist prior to this log entry.
      String priorETag = logEntity.getString(DbLogTable.PREVIOUS_ROW_ETAG);
      if ( priorETag == null ) {
        // no prior state -- so rowId may not exist now...
        try {
          // try to retrieve the rowId from the DbTable
          Entity rowEntity = table.getEntity(logEntity.getString(DbLogTable.ROW_ID), cc);
          // if found, delete it
          rowEntity.delete(cc);
        } catch ( ODKEntityNotFoundException e ) {
          // ignore... it was never created, which is OK!
        }
        // remove the entry in DbLogTable for the pending state
        logEntity.delete(cc);
      } else {
        // there is prior state, so the rowId should exist
        Entity rowEntity = table.getEntity(logEntity.getString(DbLogTable.ROW_ID), cc);
        // and the prior state should exist in the log...
        Entity priorLogEntity = logTable.getEntity(logEntity.getString(DbLogTable.PREVIOUS_ROW_ETAG), cc);

        // reset the row to the prior row state
        creator.setRowFields(rowEntity,
            priorLogEntity.getId(),
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
  public List<Row> getRows() throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException, InconsistentStateException, BadColumnNameException {

    userPermissions.checkPermission(appId, tableId, TablePermission.READ_ROW);

    List<DbColumnDefinitionsEntity> columns = null;
    List<Entity> entities = null;
    LockTemplate propsLock = new LockTemplate(tableId, ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      propsLock.acquire();

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if ( schemaETag == null ) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      DbTableDefinitionsEntity tableDefn = DbTableDefinitions.getDefinition(tableId, schemaETag, cc);
      columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

      DbTable table = DbTable.getRelation(tableDefn, columns, cc);
      DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

      revertPendingChanges( entry, columns, table, logTable);

      Query query = buildRowsQuery(table);
      entities = query.execute();

    } finally {
      propsLock.release();
    }

    if ( entities == null || columns == null ) {
      throw new InconsistentStateException("Unable to retrieve rows for table " + tableId + ".");
    }

    ArrayList<Row> rows = new ArrayList<Row>();
    for (Entity entity : entities) {
      Row row = converter.toRow(entity, columns);
      if ( userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
        rows.add(row);
      } else if ( userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW, row.getRowId(), row.getFilterScope())) {
        rows.add(row);
      }
    }
    return rows;
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
  public List<Row> getRowsSince(String dataETag) throws ODKDatastoreException, ODKTaskLockException, InconsistentStateException, PermissionDeniedException, BadColumnNameException {

    userPermissions.checkPermission(appId, tableId, TablePermission.READ_ROW);

    List<DbColumnDefinitionsEntity> columns = null;
    List<Entity> entities = null;
    LockTemplate propsLock = new LockTemplate(tableId, ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      propsLock.acquire();

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if ( schemaETag == null ) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      DbTableDefinitionsEntity tableDefn = DbTableDefinitions.getDefinition(tableId, schemaETag, cc);
      columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

      DbTable table = DbTable.getRelation(tableDefn, columns, cc);
      DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

      revertPendingChanges( entry, columns, table, logTable);

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
        query = buildRowsFromBeginningQuery(logTable, entry);
      } else {
        query = buildRowsSinceQuery(logTable, sequenceValue);
      }

      entities = query.execute();
    } finally {
      propsLock.release();
    }

    if ( entities == null || columns == null ) {
      throw new InconsistentStateException("Unable to retrieve rows for table " + tableId + ".");
    }

    // TODO: properly handle reporting of rows that the user no longer has
    // access to because of a access / permissions change for that user and / or row.
    ArrayList<Row> rows = new ArrayList<Row>();
    for (Entity entity : entities) {
      Row row = converter.toRowFromLogTable(entity, columns);
      if ( userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
        rows.add(row);
      } else if ( userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW, row.getRowId(), row.getFilterScope())) {
        rows.add(row);
      }
    }
    return computeDiff(rows);
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
  private String getSequenceValueForDataETag(DbLogTable logTable, String dataETag) throws ODKDatastoreException {
    Query query = logTable.query("DataManager.getSequenceValueForDataETag", cc);
    query.equal(DbLogTable.DATA_ETAG_AT_MODIFICATION, dataETag);
    List<Entity> values = query.execute();
    if (values == null || values.size() == 0) {
      throw new ODKEntityNotFoundException("ETag " + dataETag + " was not found in log table!");
    } else if (values.size() != 1) {
      // TODO: at least for now, we only have one update per change event...
      throw new ODKDatastoreException("Unexpected duplicate records for ETag " + dataETag
          + " found in log table!");
    }
    Entity e = values.get(0);
    return e.getString(DbLogTable.SEQUENCE_VALUE);
  }

  /**
   * @return the query for rows which have been changed or added from the
   *         beginning
   * @throws ODKDatastoreException
   */
  private Query buildRowsFromBeginningQuery(DbLogTable logTable, DbTableEntryEntity entry) throws ODKDatastoreException {
    Query query = logTable.query("DataManager.buildRowsBeginningFromQuery", cc);
    query.greaterThanOrEqual(DbLogTable.SEQUENCE_VALUE, entry.getAprioriDataSequenceValue());
    query.sortAscending(DbLogTable.SEQUENCE_VALUE);
    return query;
  }

  /**
   * @param sequenceValue
   * @return the query for rows which have been changed or added since the given
   *         sequenceValue
   * @throws ODKDatastoreException
   */
  private Query buildRowsSinceQuery(DbLogTable logTable, String sequenceValue) throws ODKDatastoreException {
    Query query = logTable.query("DataManager.buildRowsSinceQuery", cc);
    query.greaterThan(DbLogTable.SEQUENCE_VALUE, sequenceValue);
    query.sortAscending(DbLogTable.SEQUENCE_VALUE);
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
  public Row getRow(String rowId) throws ODKEntityNotFoundException, ODKDatastoreException, PermissionDeniedException, InconsistentStateException, ODKTaskLockException, BadColumnNameException {
    Validate.notEmpty(rowId);

    userPermissions.checkPermission(appId, tableId, TablePermission.READ_ROW);

    List<DbColumnDefinitionsEntity> columns = null;
    Entity entity = null;
    LockTemplate propsLock = new LockTemplate(tableId, ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      propsLock.acquire();

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if ( schemaETag == null ) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      DbTableDefinitionsEntity tableDefn = DbTableDefinitions.getDefinition(tableId, schemaETag, cc);
      columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

      DbTable table = DbTable.getRelation(tableDefn, columns, cc);
      DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

      revertPendingChanges( entry, columns, table, logTable);

      entity = table.getEntity(rowId, cc);

    } finally {
      propsLock.release();
    }

    if ( columns == null ) {
      throw new InconsistentStateException("Unable to retrieve rows for table " + tableId + ".");
    }

    Row row = converter.toRow(entity, columns);
    if ( userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
      return row;
    } else if ( userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW, row.getRowId(), row.getFilterScope())) {
      return row;
    }
    throw new PermissionDeniedException(String.format("Denied table %s row %s access to user %s",
        tableId, rowId, userPermissions.getOdkTablesUserId()));
  }

  /**
   * Inserts or Updates a row. If inserting, the row must not already
   * exist or the eTag for the row being inserted must exactly match that on the
   * server.
   *
   * @param af
   *          -- authentication filter to be applied to this action
   * @param row
   *          the row to update. See
   *          {@link Row#forInsert(String, String, java.util.Map)}.
   *          {@link Row#forUpdate(String, String, java.util.Map)}
   *          {@link Row#isDeleted()}, {@link Row#getCreateUser()}, and
   *          {@link Row#getLastUpdateUser()} will be ignored if they are set.
   * @return a copy of the row that was inserted or updated, with the row's rowETtag
   *         populated with the new rowETtag. If the original passed in row had
   *         a null rowId, the row will contain the generated rowId.
   * @throws ODKEntityNotFoundException
   *           if the passed in row does not exist
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws ETagMismatchException
   *           if the passed in row has a different rowETtag from the
   *           row in the datastore (e.g., on insert, the row already exists, or
   *           on update, there is conflict that needs to be resolved).
   * @throws BadColumnNameException
   *           if the passed in row set a value for a column which
   *           doesn't exist in the table
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   *
   */
  public Row insertOrUpdateRow(Row row)
      throws ODKEntityPersistException, ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, ETagMismatchException, BadColumnNameException,
      PermissionDeniedException, InconsistentStateException {
    Validate.notNull(row);

    userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_ROW);

    List<DbColumnDefinitionsEntity> columns = null;
    Entity entity = null;
    LockTemplate propsLock = new LockTemplate(tableId, ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      propsLock.acquire();
      Sequencer sequencer = new Sequencer(cc);

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if ( schemaETag == null ) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      DbTableDefinitionsEntity tableDefn = DbTableDefinitions.getDefinition(tableId, schemaETag, cc);
      columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

      DbTable table = DbTable.getRelation(tableDefn, columns, cc);
      DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

      revertPendingChanges( entry, columns, table, logTable);

      String rowId = row.getRowId();
      boolean newRowId = false;
      if (rowId == null) {
        newRowId = true;
        rowId = CommonFieldsBase.newUri();
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

        if ( newRowId ) {
          throw new  InconsistentStateException("Synthesized rowId collides with existing row in table " + tableId + ".");
        }

        if ( nullIncomingScope ) {
          // preserve the scope of the existing entity if the incoming Row didn't specify one.
          scope = converter.getDbTableFilterScope(entity);
        }

        // confirm that the user has the ability to read the row
        boolean hasPermissions = false;
        if ( userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
          hasPermissions = true;
        } else if ( userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW, rowId, scope)) {
          hasPermissions = true;
        }

        if (!hasPermissions ) {
          throw new PermissionDeniedException(String.format("Denied table %s row %s read access to user %s",
              tableId, rowId, userPermissions.getOdkTablesUserId()));
        }

        // confirm they have the ability to write to it
        hasPermissions = false;
        if ( userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_WRITE)) {
          hasPermissions = true;
        } else if ( userPermissions.hasFilterScope(appId, tableId, TablePermission.WRITE_ROW, rowId, scope)) {
          hasPermissions = true;
        }

        if (!hasPermissions ) {
          throw new PermissionDeniedException(String.format("Denied table %s row %s read access to user %s",
              tableId, rowId, userPermissions.getOdkTablesUserId()));
        }

        String rowETag = entity.getString(DbTable.ROW_ETAG);
        String currentETag = row.getRowETag();
        if (currentETag == null || !currentETag.equals(rowETag)) {

          // Take the hit to convert the row we have.
          // If the row matches everywhere except on the rowETag, return it.
          Row currentRow = converter.toRow(entity, columns);
          if ( row.hasMatchingSignificantFieldValues(currentRow) ) {
            return currentRow;
          }

          // if null, then the client thinks they are creating a new row.
          // The rows may be identical, but leave that to the client to determine
          // trigger client-side conflict resolution.
          // Otherwise, if there is a mis-match, then the client needs to pull and
          // perform client-side conflict resolution on the changes already up on the server.
          throw new ETagMismatchException(String.format("%s does not match %s " + "for rowId %s",
              currentETag, rowETag, rowId));
        }

      } catch ( ODKEntityNotFoundException e ) {

        // require unfiltered write permissions to create a new record
        userPermissions.checkPermission(appId, tableId, TablePermission.UNFILTERED_WRITE);

        newRowId = true;
        // initialization for insert...
        entity = table.newEntity(rowId, cc);
        entity.set(DbTable.CREATE_USER, userPermissions.getOdkTablesUserId());
      }

      // OK we are able to update or insert the record -- mark as pending change.

      // get new dataETag
      String dataETagAtModification = CommonFieldsBase.newUri();
      entry.setPendingDataETag(dataETagAtModification);
      entry.put(cc);

      // this will be null of the entity is newly created...
      String previousRowETag = row.getRowETag();

      // update the fields in the DbTable entity...
      creator.setRowFields(entity, CommonFieldsBase.newUri(), dataETagAtModification,
          userPermissions.getOdkTablesUserId(), false, scope, row.getFormId(), row.getLocale(),
          row.getSavepointType(), row.getSavepointTimestamp(), row.getSavepointCreator(), row.getValues(), columns);

      // create log table entry
      Entity logEntity = creator.newLogEntity(logTable, dataETagAtModification, previousRowETag, entity, columns, sequencer, cc);

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

    if ( columns == null ) {
      throw new InconsistentStateException("Unable to retrieve rows for table " + tableId + ".");
    }

    Row updatedRow = converter.toRow(entity, columns);
    return updatedRow;
  }

  /**
   * Delete a row.
   *
   * @param rowId
   *          the row to delete.
   * @return returns the new dataETag that is current after deleting the row.
   * @throws ODKEntityNotFoundException
   *           if there is no row with the given id in the datastore
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   * @throws BadColumnNameException
   */
  public String deleteRow(String rowId) throws ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, PermissionDeniedException, InconsistentStateException, BadColumnNameException {
    Validate.notNull(rowId);
    Validate.notBlank(rowId);

    userPermissions.checkPermission(appId, tableId, TablePermission.DELETE_ROW);
    String dataETagAtModification = null;
    LockTemplate propsLock = new LockTemplate(tableId, ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      propsLock.acquire();
      Sequencer sequencer = new Sequencer(cc);

      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      String schemaETag = entry.getSchemaETag();

      if ( schemaETag == null ) {
        throw new InconsistentStateException("Schema for table " + tableId + " is not yet defined.");
      }

      DbTableDefinitionsEntity tableDefn = DbTableDefinitions.getDefinition(tableId, schemaETag, cc);
      List<DbColumnDefinitionsEntity> columns = DbColumnDefinitions.query(tableId, schemaETag, cc);

      DbTable table = DbTable.getRelation(tableDefn, columns, cc);
      DbLogTable logTable = DbLogTable.getRelation(tableDefn, columns, cc);

      revertPendingChanges( entry, columns, table, logTable);

      Entity entity = table.getEntity(rowId, cc);

      Scope scope = converter.getDbTableFilterScope(entity);

      // check for read access
      boolean hasPermissions = false;
      if ( userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_READ)) {
        hasPermissions = true;
      } else if ( userPermissions.hasFilterScope(appId, tableId, TablePermission.READ_ROW, rowId, scope)) {
        hasPermissions = true;
      }

      if (!hasPermissions ) {
        throw new PermissionDeniedException(String.format("Denied table %s row %s read access to user %s",
            tableId, rowId, userPermissions.getOdkTablesUserId()));
      }

      // check for delete access
      hasPermissions = false;
      if ( userPermissions.hasPermission(appId, tableId, TablePermission.UNFILTERED_DELETE)) {
        hasPermissions = true;
      } else if ( userPermissions.hasFilterScope(appId, tableId, TablePermission.DELETE_ROW, rowId, scope)) {
        hasPermissions = true;
      }

      if (!hasPermissions ) {
        throw new PermissionDeniedException(String.format("Denied table %s row %s delete access to user %s",
            tableId, rowId, userPermissions.getOdkTablesUserId()));
      }

      // get new dataETag
      dataETagAtModification = CommonFieldsBase.newUri();
      entry.setPendingDataETag(dataETagAtModification);
      entry.put(cc);

      // remember the previous row ETag so we can chain revisions in the DbLogTable
      String previousRowETag = entity.getString(DbTable.ROW_ETAG);

      // update the row ETag and deletion status
      entity.set(DbTable.ROW_ETAG, CommonFieldsBase.newUri());
      entity.set(DbTable.DELETED, true);

      // create log table entry
      Entity logEntity = creator.newLogEntity(logTable, dataETagAtModification, previousRowETag, entity, columns, sequencer, cc);

      // commit the log change to the database (must be done first!)
      DbLogTable.putEntity(logEntity, cc);
      // commit the row change
      DbTable.putEntity(entity, cc);

      // update the TableEntry to reflect the completion of the change
      entry.setDataETag(entry.getPendingDataETag());
      entry.setPendingDataETag(null);
      entry.put(cc);

    } finally {
      propsLock.release();
    }

    return dataETagAtModification;
  }
}