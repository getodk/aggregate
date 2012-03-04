package org.opendatakit.aggregate.odktables;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.relation.DbColumn;
import org.opendatakit.aggregate.odktables.relation.DbTable;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
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

  public TableManager(CallingContext cc) {
    this.cc = cc;
    this.converter = new EntityConverter();
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
  public TableEntry getTable(String tableId) throws ODKEntityNotFoundException,
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
   * @param columns
   *          the columns the table should have
   * @return a table entry representing the newly created table
   * @throws ODKEntityPersistException
   *           if a table with the given table id already exists
   * @throws ODKDatastoreException
   */
  public TableEntry createTable(String tableId, List<Column> columns)
      throws ODKEntityPersistException, ODKDatastoreException {
    Validate.notEmpty(tableId);
    Validate.noNullElements(columns);

    List<Entity> entities = new ArrayList<Entity>();

    Entity entry = converter.newTableEntryEntity(tableId, cc);
    entities.add(entry);

    for (Column column : columns) {
      entities.add(converter.newColumnEntity(tableId, column, cc));
    }

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
   */
  public void deleteTable(String tableId) throws ODKEntityNotFoundException, ODKDatastoreException {
    Validate.notEmpty(tableId);

    List<Entity> entities = new ArrayList<Entity>();

    Entity entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);
    entities.add(entry);

    List<Entity> columns = DbColumn.query(tableId, cc);
    entities.addAll(columns);

    Relation relation = DbTable.getRelation(tableId, cc);

    Relation.deleteEntities(entities, cc);
    relation.dropRelation(cc);
  }

}
