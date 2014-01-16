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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions.DbColumnDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbLogTable;
import org.opendatakit.aggregate.odktables.relation.DbTable;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry.DbTableEntryEntity;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
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
  private EntityConverter converter;
  private EntityCreator creator;
  private String tableId;
  private DbTableEntryEntity entry;
  private DbTable table;
  private DbLogTable logTable;
  private List<DbColumnDefinitionsEntity> columns;

  /**
   * Construct a new DataManager.
   *
   * @param tableId
   *          the unique identifier of the table
   * @param cc
   *          the calling context
   * @throws ODKEntityNotFoundException
   *           if no table with the given id exists
   * @throws ODKDatastoreException
   *           if there is an internal error in the datastore
   */
  public DataManager(String tableId, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException {
    Validate.notEmpty(tableId);
    Validate.notNull(cc);
    this.cc = cc;
    this.converter = new EntityConverter();
    this.creator = new EntityCreator();
    this.tableId = tableId;
    this.entry = DbTableEntry.getTableIdEntry(tableId, cc);
    String schemaETag = entry.getSchemaETag();
    this.table = DbTable.getRelation(tableId, schemaETag, cc);
    this.logTable = DbLogTable.getRelation(tableId, schemaETag, cc);
    this.columns = DbColumnDefinitions.query(tableId, schemaETag, cc);
  }

  public String getTableId() {
    return tableId;
  }

  /**
   * Retrieve all current rows of the table.
   *
   * @return all the rows of the table.
   * @throws ODKDatastoreException
   */
  public List<Row> getRows() throws ODKDatastoreException {
    Query query = buildRowsQuery();
    List<Entity> rows = query.execute();
    return converter.toRows(rows, columns, false);
  }

  /**
   * Retrieve all current rows of the table, filtered by the given scope.
   *
   * @param scope
   *          the scope to filter by
   * @return all the rows of the table, filtered by the given scope
   * @throws ODKDatastoreException
   */
  public List<Row> getRows(Scope scope) throws ODKDatastoreException {
    Query query = buildRowsQuery();
    query.equal(DbTable.FILTER_TYPE, scope.getType().name());
    query.equal(DbTable.FILTER_VALUE, scope.getValue());
    List<Entity> rows = query.execute();
    return converter.toRows(rows, columns, false);
  }

  /**
   * Retrieve all current rows of the table, filtered by rows that match any of
   * the given scopes.
   *
   * @param scopes
   *          the scopes to filter by
   * @return all the rows of the table, filtered by the given scopes
   * @throws ODKDatastoreException
   */
  public List<Row> getRows(List<Scope> scopes) throws ODKDatastoreException {
    List<Row> rows = new ArrayList<Row>();
    for (Scope scope : scopes) {
      rows.addAll(getRows(scope));
    }
    return computeDiff(rows);
  }

  /**
   * @return the query for current rows in the table
   */
  private Query buildRowsQuery() {
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
   */
  public List<Row> getRowsSince(String dataETag) throws ODKDatastoreException {
    String sequenceValue = null;
    if (dataETag != null) {
      try {
        sequenceValue = getSequenceValueForDataETag(dataETag);
      } catch (ODKEntityNotFoundException e) {
        // TODO: log this as a warning -- may be returning a very large set
        sequenceValue = null;
      }
    }
    Query query;
    if (sequenceValue == null) {
      query = buildRowsFromBeginningQuery();
    } else {
      query = buildRowsSinceQuery(sequenceValue);
    }
    List<Entity> results = query.execute();
    List<Row> logRows = converter.toRows(results, columns, true);
    return computeDiff(logRows);
  }

  /**
   * Retrieves a set of row representing the changes since the given data etag,
   * and filtered to rows which match the given scope.
   *
   * @param dataETag
   *          the data ETag
   * @param scope
   *          the scope to filter to
   * @return the rows which have changed or been added since the given data etag
   * @throws ODKDatastoreException
   */
  public List<Row> getRowsSince(String dataETag, Scope scope) throws ODKDatastoreException {
    String sequenceValue = (dataETag == null) ? null : getSequenceValueForDataETag(dataETag);
    Query query;
    if (sequenceValue == null) {
      query = buildRowsFromBeginningQuery();
    } else {
      query = buildRowsSinceQuery(sequenceValue);
    }
    query = narrowByScope(query, scope);
    List<Entity> results = query.execute();
    List<Row> logRows = converter.toRows(results, columns, true);
    return computeDiff(logRows);
  }

  /**
   * Retrieves a set of row representing the changes since the given data etag,
   * and filtered to rows which match any of the given scopes.
   *
   * @param dataETag
   *          the data ETag
   * @param scopes
   *          the scopes to filter to
   * @return the rows which have changed or been added since the given data etag
   * @throws ODKDatastoreException
   */
  public List<Row> getRowsSince(String dataETag, List<Scope> scopes) throws ODKDatastoreException {
    String sequenceValue = (dataETag == null) ? null : getSequenceValueForDataETag(dataETag);
    List<Entity> entities = new ArrayList<Entity>();
    for (Scope scope : scopes) {
      Query query;
      if (sequenceValue == null) {
        query = buildRowsFromBeginningQuery();
      } else {
        query = buildRowsSinceQuery(sequenceValue);
      }
      query = narrowByScope(query, scope);
      List<Entity> results = query.execute();
      entities.addAll(results);
    }

    Collections.sort(entities, new Comparator<Entity>() {
      public int compare(Entity o1, Entity o2) {
        return o1.getString(DbLogTable.SEQUENCE_VALUE).compareTo(
            o2.getString(DbLogTable.SEQUENCE_VALUE));
      }
    });
    List<Row> logRows = converter.toRows(entities, columns, true);
    return computeDiff(logRows);
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
  private String getSequenceValueForDataETag(String dataETag) throws ODKDatastoreException {
    Query query = logTable.query("DataManager.getSequenceValueForDataETag", cc);
    // TODO: did this break (if it ever worked) when converted to string etags
    // instead of flawed mod numbers?
    query.equal(DbLogTable.DATA_ETAG_AT_MODIFICATION, dataETag);
    List<Entity> values = query.execute();
    if (values == null || values.size() == 0) {
      throw new ODKEntityNotFoundException("ETag " + dataETag + " was not found in log table!");
    } else if (values.size() != 1) {
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
  private Query buildRowsFromBeginningQuery() throws ODKDatastoreException {
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
  private Query buildRowsSinceQuery(String sequenceValue) throws ODKDatastoreException {
    Query query = logTable.query("DataManager.buildRowsSinceQuery", cc);
    query.greaterThan(DbLogTable.SEQUENCE_VALUE, sequenceValue);
    query.sortAscending(DbLogTable.SEQUENCE_VALUE);
    return query;
  }

  /**
   * Narrows the given {@link DbLogTable} query to filter to rows which match
   * the given scope.
   *
   * @param query
   *          the query
   * @param scope
   *          the scope to narrow the query by
   * @return the query
   * @throws ODKDatastoreException
   */
  private Query narrowByScope(Query query, Scope scope) throws ODKDatastoreException {
    query.equal(DbLogTable.FILTER_TYPE, scope.getType().name());
    query.equal(DbLogTable.FILTER_VALUE, scope.getValue());
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
   * @return the row, or null if no such row exists
   * @throws ODKDatastoreException
   */
  public Row getRow(String rowId) throws ODKDatastoreException {
    Validate.notEmpty(rowId);
    try {
      return getRowNullSafe(rowId);
    } catch (ODKEntityNotFoundException e) {
      return null;
    }
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
   */
  public Row getRowNullSafe(String rowId) throws ODKEntityNotFoundException, ODKDatastoreException {
    Validate.notEmpty(rowId);
    Entity row = table.getEntity(rowId, cc);
    return converter.toRow(row, columns);
  }

  /**
   * Inserts or Updates a list of rows. If inserting, the row must not already
   * exist or the eTag for the row being inserted must exactly match that on the
   * server.
   *
   * @param af
   *          -- authentication filter to be applied to this action
   * @param rows
   *          the rows to update. See
   *          {@link Row#forInsert(String, String, java.util.Map)}.
   *          {@link Row#forUpdate(String, String, java.util.Map)}
   *          {@link Row#isDeleted()}, {@link Row#getCreateUser()}, and
   *          {@link Row#getLastUpdateUser()} will be ignored if they are set.
   * @return the rows that were inserted or updated, with each row's rowETtag
   *         populated with the new rowETtag. If the original passed in row had
   *         a null rowId, the row will contain the generated rowId.
   * @throws ODKEntityNotFoundException
   *           if one of the passed in rows does not exist
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws ETagMismatchException
   *           if one of the passed in rows has a different rowETtag from the
   *           row in the datastore (e.g., on insert, the row already exists, or
   *           on update, there is conflict that needs to be resolved).
   * @throws BadColumnNameException
   *           if one of the passed in rows set a value for a column which
   *           doesn't exist in the table
   * @throws PermissionDeniedException
   *
   */
  public List<Row> insertOrUpdateRows(AuthFilter af, List<Row> rows)
      throws ODKEntityPersistException, ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, ETagMismatchException, BadColumnNameException,
      PermissionDeniedException {
    Validate.noNullElements(rows);

    List<Entity> rowEntities;

    // lock table
    LockTemplate lock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_DATA, cc);
    try {
      lock.acquire();
      // TODO: CRITICAL need to start a transaction here!!!
      Sequencer sequencer = new Sequencer(cc);

      // refresh entry
      entry = DbTableEntry.getTableIdEntry(tableId, cc);

      // get new data ETag
      String dataETag = CommonFieldsBase.newUri();
      entry.setDataETag(dataETag);

      // create or update entities
      rowEntities = creator.insertOrUpdateRowEntities(af, table, dataETag, rows, columns, cc);

      // create log table entries
      List<Entity> logEntities = creator.newLogEntities(logTable, dataETag, rowEntities, columns,
          sequencer, cc);

      // update db
      // This is where a user-defined table actually gets created for the first
      // time.
      DbLogTable.putEntities(logEntities, cc);
      DbTable.putEntities(rowEntities, cc);
      entry.put(cc);
      // TODO: CRITICAL need to commit a transaction here!!!
    } finally {
      // TODO: CRITICAL if failure then need to rollback a transaction!!!
      lock.release();
    }
    return converter.toRows(rowEntities, columns, false);
  }

  /**
   * Delete a row. This is equivalent to calling {@link #deleteRows(List)} with
   * a list of size 1.
   *
   * @param rowId
   *          the row to delete.
   * @throws ODKEntityNotFoundException
   *           if there is no row with the given id in the datastore
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   */
  public String deleteRow(String rowId) throws ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException {
    List<String> rowIds = new ArrayList<String>();
    rowIds.add(rowId);
    return deleteRows(rowIds);
  }

  /**
   * Deletes a set of rows.
   *
   * @param rowIds
   *          the rows to delete.
   * @return returns the new dataETag that is current after deleting the rows.
   *         Returns null if something goes wrong and the lock can never be
   *         acquired.
   * @throws ODKEntityNotFoundException
   *           if one of the rowIds does not exist in the datastore
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   */
  public String deleteRows(List<String> rowIds) throws ODKEntityNotFoundException,
      ODKDatastoreException, ODKTaskLockException {
    Validate.noNullElements(rowIds);

    // lock table
    LockTemplate lock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_DATA, cc);
    String dataETag = null;
    try {
      lock.acquire();
      Sequencer sequencer = new Sequencer(cc);

      // refresh entry
      entry = DbTableEntry.getTableIdEntry(tableId, cc);

      // get new dataETag
      dataETag = CommonFieldsBase.newUri();
      entry.setDataETag(dataETag);

      // get entities and mark deleted
      List<Entity> rows = DbTable.query(table, rowIds, cc);
      for (Entity row : rows) {
        row.set(DbTable.ROW_ETAG, CommonFieldsBase.newUri());
        row.set(DbTable.DELETED, true);
      }

      // create log table entries
      List<Entity> logRows = creator.newLogEntities(logTable, dataETag, rows, columns, sequencer,
          cc);

      // update db
      DbLogTable.putEntities(logRows, cc);
      DbTable.putEntities(rows, cc);
      entry.put(cc);
    } finally {
      lock.release();
    }
    return dataETag;
  }
}