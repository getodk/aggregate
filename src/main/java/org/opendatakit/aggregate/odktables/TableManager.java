package org.opendatakit.aggregate.odktables;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.relation.DbColumn;
import org.opendatakit.aggregate.odktables.relation.DbLogTable;
import org.opendatakit.aggregate.odktables.relation.DbTable;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableProperties;
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
 * Manages creating, deleting, and getting tables.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class TableManager {

  private CallingContext cc;
  private EntityConverter converter;
  private EntityCreator creator;

  public TableManager(CallingContext cc) {
    this.cc = cc;
    this.converter = new EntityConverter();
    this.creator = new EntityCreator();
  }

  /**
   * Retrieve a list of all table entries in the datastore.
   * 
   * @return a list of all table entries.
   * @throws ODKDatastoreException
   */
  public List<TableEntry> getTables() throws ODKDatastoreException {
    Query query = DbTableEntry.getRelation(cc).query("TableManager.getTables", cc);
    List<Entity> enries = query.execute();
    return converter.toTableEntries(enries);
  }

  /**
   * Retrieve the table entry for the given tableId.
   * 
   * @param tableId
   *          the id of a table
   * @return the table entry, or null if no such table exists
   * @throws ODKDatastoreException
   */
  public TableEntry getTable(String tableId) throws ODKDatastoreException {
    Validate.notEmpty(tableId);
    Query query = DbTableEntry.getRelation(cc).query("TableManager.getTable", cc);
    query.equal(CommonFieldsBase.URI_COLUMN_NAME, tableId);
    Entity table = query.get();
    if (table != null)
      return converter.toTableEntry(table);
    else
      return null;
  }

  /**
   * Retrieve the table entry for the given tableId.
   * 
   * @param tableId
   *          the id of the table previously created with
   *          {@link #createTable(String, List)}
   * @return the table entry representing the table
   * @throws ODKEntityNotFoundException
   *           if no table with the given table id was found
   * @throws ODKDatastoreException
   */
  public TableEntry getTableNullSafe(String tableId) throws ODKEntityNotFoundException,
      ODKDatastoreException {
    Validate.notEmpty(tableId);

    Entity entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);
    return converter.toTableEntry(entry);
  }

  /**
   * Creates a new table.
   * 
   * @param tableId
   *          the unique identifier for the table
   * @param tableName
   *          a human readable name for the table
   * @param columns
   *          the columns the table should have
   * @param metadata
   *          application defined metadata to store with the table (may be null)
   * @return a table entry representing the newly created table
   * @throws TableAlreadyExistsException
   *           if a table with the given table id already exists
   * @throws ODKEntityPersistException
   * @throws ODKDatastoreException
   */
  public TableEntry createTable(String tableId, String tableName, List<Column> columns,
      String metadata) throws ODKEntityPersistException, ODKDatastoreException,
      TableAlreadyExistsException {
    Validate.notEmpty(tableId);
    Validate.notEmpty(tableName);
    Validate.noNullElements(columns);

    // check if table exists
    if (getTable(tableId) != null) {
      throw new TableAlreadyExistsException(String.format(
          "Table with tableId '%s' already exists.", tableId));
    }

    // create table
    List<Entity> entities = new ArrayList<Entity>();

    Entity entry = creator.newTableEntryEntity(tableId, cc);
    entities.add(entry);

    for (Column column : columns) {
      entities.add(creator.newColumnEntity(tableId, column, cc));
    }

    Entity properties = creator.newTablePropertiesEntity(tableId, tableName, metadata, cc);
    entities.add(properties);

    Relation.putEntities(entities, cc);

    return converter.toTableEntry(entry);
  }

  /**
   * Deletes a table.
   * 
   * @param tableId
   *          the unique identifier of the table to delete.
   * @throws ODKEntityNotFoundException
   *           if no table with the given table id was found
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   */
  public void deleteTable(String tableId) throws ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException {
    Validate.notEmpty(tableId);

    List<Entity> entities = new ArrayList<Entity>();

    Entity entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);
    entities.add(entry);

    List<Entity> columns = DbColumn.query(tableId, cc);
    entities.addAll(columns);

    Entity properties = DbTableProperties.getProperties(tableId, cc);
    entities.add(properties);

    Relation table = DbTable.getRelation(tableId, cc);
    Relation logTable = DbLogTable.getRelation(tableId, cc);

    LockTemplate dataLock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_DATA, cc);
    LockTemplate propsLock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_PROPERTIES, cc);
    try {
      dataLock.acquire();
      propsLock.acquire();
      Relation.deleteEntities(entities, cc);
      table.dropRelation(cc);
      logTable.dropRelation(cc);
    } finally {
      propsLock.release();
      dataLock.release();
    }
  }

}
