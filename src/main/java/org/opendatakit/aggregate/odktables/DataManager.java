package org.opendatakit.aggregate.odktables;

import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import lombok.val;

import org.apache.commons.lang.Validate;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.exception.RowVersionMismatchException;
import org.opendatakit.aggregate.odktables.relation.DbColumn;
import org.opendatakit.aggregate.odktables.relation.DbLogTable;
import org.opendatakit.aggregate.odktables.relation.DbTable;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.util.EntityConverter;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

/**
 * Manages read, insert, update, and delete operations on the rows of a table.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class DataManager {

  private CallingContext cc;
  private EntityConverter converter;
  private String tableId;
  private Entity entry;
  private Relation table;
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
    this.tableId = tableId;
    this.entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);
    this.table = DbTable.getRelation(tableId, cc);
    this.columns = DbColumn.query(tableId, cc);
  }

  /**
   * Retrieve all rows of the table.
   * 
   * @return all the rows of the table.
   * @throws ODKDatastoreException
   */
  public List<Row> getRows() throws ODKDatastoreException {
    val query = table.query("DataManager.getRows", cc);
    query.equal(DbTable.DELETED, false);
    val rows = query.execute();
    return converter.toRows(rows, columns);
  }

  /**
   * Insert a single row into the table. This is equivalent to calling
   * {@link #insertRows(List)} with a list of size 1.
   * 
   * @param row
   *          the row to insert.
   * @return the row with rowEtag populated. If the passed in row had a null
   *         rowId, then the generated rowId will also be populated.
   * @throws ODKEntityPersistException
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   */
  public Row insertRow(Row row) throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException {
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
   * @return the list of inserted rows, with each row's rowEtag populated. For
   *         each row, if the original passed in row had a null rowId, the row
   *         will contain the generated rowId.
   * @throws ODKEntityPersistException
   *           if the passed in rows contained a row that already exists.
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   */
  @SneakyThrows(RowVersionMismatchException.class)
  public List<Row> insertRows(List<Row> rows) throws ODKEntityPersistException,
      ODKDatastoreException, ODKTaskLockException {
    return insertOrUpdateRows(rows, true);
  }

  /**
   * Updates a row. This is equivalent to calling {@link #updateRows(List)}.
   * 
   * @param row
   *          the row to update
   * @return the row that was updated, with a new rowEtag.
   * @throws ODKEntityNotFoundException
   *           if the row does not exist
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws RowVersionMismatchException
   *           if the passed in row has a different rowEtag from the row in the
   *           datastore
   */
  public Row updateRow(Row row) throws ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, RowVersionMismatchException {
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
   * @return the rows that were updated, with each row's rowEtag populated with
   *         the new rowEtag.
   * @throws ODKEntityNotFoundException
   *           if one of the passed in rows does not exist
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws RowVersionMismatchException
   *           if one of the passed in rows has a different rowEtag from the row
   *           in the datastore
   */
  public List<Row> updateRows(List<Row> rows) throws ODKEntityNotFoundException,
      ODKDatastoreException, ODKTaskLockException, RowVersionMismatchException {
    return insertOrUpdateRows(rows, false);
  }

  private List<Row> insertOrUpdateRows(List<Row> rows, boolean insert)
      throws ODKEntityPersistException, ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, RowVersionMismatchException {
    Validate.noNullElements(rows);

    // increment modification number
    int modificationNumber = entry.getInteger(DbTableEntry.MODIFICATION_NUMBER);
    modificationNumber++;
    entry.set(DbTableEntry.MODIFICATION_NUMBER, modificationNumber);

    // create or update entities
    List<Entity> rowEntities;
    if (insert) {
      rowEntities = converter.newRowEntities(table, rows, modificationNumber, columns, cc);
    } else {
      rowEntities = converter.updateRowEntities(table, modificationNumber, rows, columns, cc);
    }

    Relation logTable = DbLogTable.getRelation(tableId, cc);
    List<Entity> logEntities = converter.newLogEntities(logTable, modificationNumber, rowEntities,
        columns, cc);

    // lock and update db
    LockTemplate lock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_DATA, cc);
    try {
      lock.acquire();
      Relation.putEntities(logEntities, cc);
      Relation.putEntities(rowEntities, cc);
      entry.put(cc);
    } finally {
      lock.release();
    }

    return converter.toRows(rowEntities, columns);
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

    // increment modification number
    int modificationNumber = entry.getInteger(DbTableEntry.MODIFICATION_NUMBER);
    modificationNumber++;
    entry.set(DbTableEntry.MODIFICATION_NUMBER, modificationNumber);

    // get entities and mark deleted
    List<Entity> rows = DbTable.query(table, rowIds, cc);
    for (val row : rows) {
      row.set(DbTable.DELETED, true);
    }

    Relation logTable = DbLogTable.getRelation(tableId, cc);
    List<Entity> logRows = converter
        .newLogEntities(logTable, modificationNumber, rows, columns, cc);

    // lock and update db
    LockTemplate lock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_DATA, cc);
    try {
      lock.acquire();
      Relation.putEntities(logRows, cc);
      Relation.putEntities(rows, cc);
      entry.put(cc);
    } finally {
      lock.release();
    }
  }

}
