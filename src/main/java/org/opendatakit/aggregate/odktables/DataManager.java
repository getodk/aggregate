package org.opendatakit.aggregate.odktables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbLogTable;
import org.opendatakit.aggregate.odktables.relation.DbTable;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Query;
import org.opendatakit.common.ermodel.simple.Relation;
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
  private Entity entry;
  private Relation table;
  private Relation logTable;
  private List<Entity> columns;

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
    this.entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);
    this.table = DbTable.getRelation(tableId, cc);
    this.logTable = DbLogTable.getRelation(tableId, cc);
    this.columns = DbColumnDefinitions.query(tableId, cc);
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
   * @param dataEtag
   *          the data etag
   * @return the rows which have changed or been added since the given data etag
   * @throws ODKDatastoreException
   */
  public List<Row> getRowsSince(String dataEtag) throws ODKDatastoreException {
    Query query = buildRowsSinceQuery(dataEtag);
    List<Entity> results = query.execute();
    List<Row> logRows = converter.toRows(results, columns, true);
    return computeDiff(logRows);
  }

  /**
   * Retrieves a set of row representing the changes since the given data etag,
   * and filtered to rows which match the given scope.
   * 
   * @param dataEtag
   *          the data etag
   * @param scope
   *          the scope to filter to
   * @return the rows which have changed or been added since the given data etag
   * @throws ODKDatastoreException
   */
  public List<Row> getRowsSince(String dataEtag, Scope scope) throws ODKDatastoreException {
    Query query = buildRowsSinceQuery(dataEtag);
    query = narrowByScope(query, scope);
    List<Entity> results = query.execute();
    List<Row> logRows = converter.toRows(results, columns, true);
    return computeDiff(logRows);
  }

  /**
   * Retrieves a set of row representing the changes since the given data etag,
   * and filtered to rows which match any of the given scopes.
   * 
   * @param dataEtag
   *          the data etag
   * @param scopes
   *          the scopes to filter to
   * @return the rows which have changed or been added since the given data etag
   * @throws ODKDatastoreException
   */
  public List<Row> getRowsSince(String dataEtag, List<Scope> scopes) throws ODKDatastoreException {
    List<Entity> entities = new ArrayList<Entity>();
    for (Scope scope : scopes) {
      Query query = buildRowsSinceQuery(dataEtag);
      // TODO: don't forget to add this back in when scopes are figured out
      //query = narrowByScope(query, scope);
      List<Entity> results = query.execute();
      entities.addAll(results);
    }
    // TODO: this may be broken after switching mod numbers to etag at the time
    // of creation/update--which is what it was really doing but with just a
    // mod number
    Collections.sort(entities, new Comparator<Entity>() {
      public int compare(Entity o1, Entity o2) {
        Long time1 = 
            Long.parseLong(o1.getString(DbLogTable.DATA_ETAG_AT_MODIFICATION));
        Long time2 = 
            Long.parseLong(o2.getString(DbLogTable.DATA_ETAG_AT_MODIFICATION));
        // TODO: is this a safe cast? likely no...
        return (int) (time1 - time2);
      }
    });
    List<Row> logRows = converter.toRows(entities, columns, true);
    return computeDiff(logRows);
  }

  /**
   * @param dataEtag
   * @return the query for rows which have been changed or added since the given
   *         dataEtag
   */
  private Query buildRowsSinceQuery(String dataEtag) {
    Query query = logTable.query("DataManager.buildRowsSinceQuery", cc);
    // TODO: did this break (if it ever worked) when converted to string etags
    // instead of flawed mod numbers?
    query.greaterThanOrEqual(DbLogTable.DATA_ETAG_AT_MODIFICATION, dataEtag);
    query.sortAscending(DbLogTable.DATA_ETAG_AT_MODIFICATION);
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
   * Insert a single row into the table. This is equivalent to calling
   * {@link #insertRows(List)} with a list of size 1.
   * 
   * @param row
   *          the row to insert. See {@link Row#forInsert(String, String, Map)}.
   *          {@link Row#getRowEtag()}, {@link Row#isDeleted()},
   *          {@link Row#getCreateUser()}, and {@link Row#getLastUpdateUser()}
   *          will be ignored if they are set.
   * @return the row with rowEtag populated. If the passed in row had a null
   *         rowId, then the generated rowId will also be populated.
   * @throws ODKEntityPersistException
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws BadColumnNameException
   *           if the passed in row set a value for a column which doesn't exist
   *           in the table
   */
  public Row insertRow(Row row) throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException {
    List<Row> rows = new ArrayList<Row>();
    rows.add(row);
    rows = insertRows(rows);
    assert rows.size() == 1;
    return rows.get(0);
  }

  /**
   * Insert a list of rows.
   * 
   * @param rows
   *          the list of rows. See
   *          {@link Row#forInsert(String, String, java.util.Map)}.
   *          {@link Row#getRowEtag()}, {@link Row#isDeleted()},
   *          {@link Row#getCreateUser()}, and {@link Row#getLastUpdateUser()}
   *          will be ignored if they are set.
   * @return the list of inserted rows, with each row's rowEtag populated. For
   *         each row, if the original passed in row had a null rowId, the row
   *         will contain the generated rowId.
   * @throws ODKEntityPersistException
   *           if the passed in rows contained a row that already exists.
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws BadColumnNameException
   *           if one of the passed in rows set a value for a column which
   *           doesn't exist in the table
   */
  public List<Row> insertRows(List<Row> rows) throws ODKEntityPersistException,
      ODKDatastoreException, ODKTaskLockException, BadColumnNameException {
    try {
      return insertOrUpdateRows(rows, true);
    } catch (EtagMismatchException e) {
      throw new RuntimeException("RowVersionMismatch happened on insert!", e);
    }
  }

  /**
   * Updates a row. This is equivalent to calling {@link #updateRows(List)}.
   * 
   * @param row
   *          the row to update. See {@link Row#forUpdate(String, String, Map)}.
   *          {@link Row#isDeleted()}, {@link Row#getCreateUser()}, and
   *          {@link Row#getLastUpdateUser()} will be ignored if they are set.
   * @return the row that was updated, with a new rowEtag.
   * @throws ODKEntityNotFoundException
   *           if the row does not exist
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws EtagMismatchException
   *           if the passed in row has a different rowEtag from the row in the
   *           datastore
   * @throws BadColumnNameException
   *           if the passed in row set a value for a column which doesn't exist
   *           in the table
   */
  public Row updateRow(Row row) throws ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, EtagMismatchException, BadColumnNameException {
    List<Row> rows = new ArrayList<Row>();
    rows.add(row);
    rows = updateRows(rows);
    assert rows.size() == 1;
    return rows.get(0);
  }

  /**
   * Updates a list of rows.
   * 
   * @param rows
   *          the rows to update. See
   *          {@link Row#forUpdate(String, String, java.util.Map)}
   *          {@link Row#isDeleted()}, {@link Row#getCreateUser()}, and
   *          {@link Row#getLastUpdateUser()} will be ignored if they are set.
   * @return the rows that were updated, with each row's rowEtag populated with
   *         the new rowEtag.
   * @throws ODKEntityNotFoundException
   *           if one of the passed in rows does not exist
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws EtagMismatchException
   *           if one of the passed in rows has a different rowEtag from the row
   *           in the datastore
   * @throws BadColumnNameException
   *           if one of the passed in rows set a value for a column which
   *           doesn't exist in the table
   * 
   */
  public List<Row> updateRows(List<Row> rows) throws ODKEntityNotFoundException,
      ODKDatastoreException, ODKTaskLockException, EtagMismatchException, BadColumnNameException {
    return insertOrUpdateRows(rows, false);
  }

  private List<Row> insertOrUpdateRows(List<Row> rows, boolean insert)
      throws ODKEntityPersistException, ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, EtagMismatchException, BadColumnNameException {
    Validate.noNullElements(rows);

    List<Entity> rowEntities;

    // lock table
    LockTemplate lock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_DATA, cc);
    try {
      lock.acquire();

      // refresh entry
      entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);

      // get new data etag
      String dataEtag = entry.getString(DbTableEntry.DATA_ETAG);
      dataEtag = Long.toString(System.currentTimeMillis());
      entry.set(DbTableEntry.DATA_ETAG, dataEtag);

      // create or update entities
      if (insert) {
        rowEntities = 
            creator.newRowEntities(table, rows, dataEtag, columns, cc);
      } else {
        rowEntities = 
            creator.updateRowEntities(table, dataEtag, rows, columns, cc);
      }

      // create log table entries
      List<Entity> logEntities = 
          creator.newLogEntities(logTable, dataEtag, rowEntities, columns, cc);

      // update db
      Relation.putEntities(logEntities, cc);
      Relation.putEntities(rowEntities, cc);
      entry.put(cc);
    } finally {
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
  public void deleteRow(String rowId) throws ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException {
    List<String> rowIds = new ArrayList<String>();
    rowIds.add(rowId);
    deleteRows(rowIds);
  }

  /**
   * Deletes a set of rows.
   * 
   * @param rowIds
   *          the rows to delete.
   * @throws ODKEntityNotFoundException
   *           if one of the rowIds does not exist in the datastore
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   */
  public void deleteRows(List<String> rowIds) throws ODKEntityNotFoundException,
      ODKDatastoreException, ODKTaskLockException {
    Validate.noNullElements(rowIds);

    // lock table
    LockTemplate lock = new LockTemplate(tableId, 
        ODKTablesTaskLockType.UPDATE_DATA, cc);
    try {
      lock.acquire();

      // refresh entry
      entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);

      // get new dataEtag
      String dataEtag = entry.getString(DbTableEntry.DATA_ETAG);
      dataEtag = Long.toString(System.currentTimeMillis());
      entry.set(DbTableEntry.DATA_ETAG, dataEtag);

      // get entities and mark deleted
      List<Entity> rows = DbTable.query(table, rowIds, cc);
      for (Entity row : rows) {
        row.set(DbTable.ROW_VERSION, CommonFieldsBase.newUri());
        row.set(DbTable.DELETED, true);
      }

      // create log table entries
      List<Entity> logRows = 
          creator.newLogEntities(logTable, dataEtag, rows, columns, cc);

      // update db
      Relation.putEntities(logRows, cc);
      Relation.putEntities(rows, cc);
      entry.put(cc);
    } finally {
      lock.release();
    }
  }
}