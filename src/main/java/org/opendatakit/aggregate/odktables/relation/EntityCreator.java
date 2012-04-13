package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.exception.RowEtagMismatchException;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

/**
 * Creates and updates new Entity objects for relations.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */

public class EntityCreator {

  /**
   * Create a new {@link DbTableEntry} entity.
   * 
   * @param tableId
   *          the table id. May be null to auto generate.
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   */
  public Entity newTableEntryEntity(String tableId, CallingContext cc) throws ODKDatastoreException {
    Validate.notNull(cc);

    if (tableId == null)
      tableId = CommonFieldsBase.newUri();

    Entity entity = DbTableEntry.getRelation(cc).newEntity(tableId, cc);
    entity.set(DbTableEntry.MODIFICATION_NUMBER, 0);
    entity.set(DbTableEntry.PROPERTIES_MOD_NUM, 0);
    return entity;
  }

  /**
   * Create a new {@link DbColumn} entity.
   * 
   * @param tableId
   *          the id of the table for the new column.
   * @param column
   *          the new column.
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   */
  public Entity newColumnEntity(String tableId, Column column, CallingContext cc)
      throws ODKDatastoreException {
    Validate.notEmpty(tableId);
    Validate.notNull(column);
    Validate.notNull(cc);

    Entity entity = DbColumn.getRelation(cc).newEntity(cc);
    entity.set(DbColumn.TABLE_ID, tableId);
    entity.set(DbColumn.COLUMN_NAME, column.getName());
    entity.set(DbColumn.COLUMN_TYPE, column.getType().name());

    return entity;
  }

  /**
   * Create a new {@link DbTableProperties} entity.
   * 
   * @param tableId
   *          the id of the table for the new table properties entity
   * @param tableName
   *          the human readable name of the table
   * @param metadata
   *          arbitrary application defined metadata for the table (may be null)
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   */
  public Entity newTablePropertiesEntity(String tableId, String tableName, String metadata,
      CallingContext cc) throws ODKDatastoreException {
    Validate.notEmpty(tableId);
    Validate.notEmpty(tableName);
    Validate.notNull(cc);

    Entity entity = DbTableProperties.getRelation(cc).newEntity(cc);
    entity.set(DbTableProperties.TABLE_ID, tableId);
    entity.set(DbTableProperties.TABLE_NAME, tableName);
    entity.set(DbTableProperties.TABLE_METADATA, metadata);

    return entity;
  }

  /**
   * Create a new {@link DbTable} row entity.
   * 
   * @param table
   *          the {@link DbTable} relation.
   * @param rowId
   *          the id of the new row. May be null to auto generate.
   * @param modificationNumber
   *          the modification number for this row.
   * @param groupOrUserId
   *          the group or user id for this row. May be null.
   * @param values
   *          the values to set on the row.
   * @param columns
   *          the {@link DbColumn} entities for the table
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   */
  public Entity newRowEntity(Relation table, String rowId, int modificationNumber,
      String groupOrUserId, Map<String, String> values, List<Entity> columns, CallingContext cc)
      throws ODKDatastoreException {
    Validate.notNull(table);
    Validate.isTrue(modificationNumber >= 0);
    Validate.noNullElements(values.keySet());
    Validate.noNullElements(columns);
    Validate.notNull(cc);

    if (rowId == null)
      rowId = CommonFieldsBase.newUri();

    Entity row = table.newEntity(rowId, cc);
    setRowFields(row, modificationNumber, groupOrUserId, false, values, columns);
    return row;
  }

  /**
   * Create a collection of new {@link DbTable} entities
   * 
   * @param table
   *          the {@link DbTable} relation
   * @param rows
   *          the rows, see {@link Row#forInsert(String, String, Map)}
   * @param modificationNumber
   *          the modification number for the rows.
   * @param columns
   *          the {@link DbColumn} entities for the table
   * @param cc
   * @return the created entities, not yet persisted
   * @throws ODKDatastoreException
   */
  public List<Entity> newRowEntities(Relation table, List<Row> rows, int modificationNumber,
      List<Entity> columns, CallingContext cc) throws ODKDatastoreException {
    Validate.notNull(table);
    Validate.noNullElements(rows);
    Validate.isTrue(modificationNumber >= 0);
    Validate.noNullElements(columns);
    Validate.notNull(cc);

    List<Entity> entities = new ArrayList<Entity>();
    for (Row row : rows) {
      Entity entity = newRowEntity(table, row.getRowId(), modificationNumber,
          row.getGroupOrUserId(), row.getValues(), columns, cc);
      entities.add(entity);
    }
    return entities;
  }

  /**
   * Update an existing {@link DbTable} entity.
   * 
   * @param table
   *          the {@link DbTable} relation
   * @param modificationNumber
   *          the modification number for this row
   * @param rowId
   *          the id of the row
   * @param currentEtag
   *          the current etag value
   * @param groupOrUserId
   *          the group or user id for the row
   * @param values
   *          the values to set
   * @param deleted
   *          true if the row is deleted
   * @param columns
   *          the {@link DbColumn} entities for the table
   * @param cc
   * @return the updated entity, not yet persisted
   * @throws ODKEntityNotFoundException
   *           if there is no entity with the given rowId
   * @throws RowEtagMismatchException
   *           if currentEtag does not match the etag of the row
   * @throws ODKDatastoreException
   */
  public Entity updateRowEntity(Relation table, int modificationNumber, String rowId,
      String currentEtag, String groupOrUserId, Map<String, String> values, boolean deleted,
      List<Entity> columns, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException, RowEtagMismatchException {
    Validate.notNull(table);
    Validate.isTrue(modificationNumber >= 0);
    Validate.notEmpty(rowId);
    // if currentEtag is null we will catch it later
    Validate.noNullElements(values.keySet());
    Validate.noNullElements(columns);
    Validate.notNull(cc);

    Entity row = table.getEntity(rowId, cc);
    String rowEtag = row.getString(DbTable.ROW_VERSION);
    if (currentEtag == null || !currentEtag.equals(rowEtag)) {
      throw new RowEtagMismatchException(String.format("%s does not match %s for rowId %s",
          currentEtag, rowEtag, row.getId()));
    }

    setRowFields(row, modificationNumber, groupOrUserId, deleted, values, columns);
    return row;
  }

  private void setRowFields(Entity row, int modificationNumber, String groupOrUserId,
      boolean deleted, Map<String, String> values, List<Entity> columns) {
    row.set(DbTable.ROW_VERSION, CommonFieldsBase.newUri());
    row.set(DbTable.MODIFICATION_NUMBER, modificationNumber);
    row.set(DbTable.GROUP_OR_USER_ID, groupOrUserId);
    row.set(DbTable.DELETED, deleted);

    for (Entry<String, String> entry : values.entrySet()) {
      String value = entry.getValue();
      String name = entry.getKey();
      Entity column = findColumn(name, columns);
      if (column == null)
        throw new IllegalArgumentException("Bad column name " + name);
      row.setAsString(RUtil.convertIdentifier(column.getId()), value);
    }
  }

  private Entity findColumn(String name, List<Entity> columns) {
    for (Entity entity : columns) {
      String colName = entity.getString(DbColumn.COLUMN_NAME);
      if (name.equals(colName))
        return entity;
    }
    return null;
  }

  /**
   * Updates a collection of {@link DbTable} entities.
   * 
   * @param table
   *          the {@link DbTable} relation.
   * @param modificationNumber
   *          the modification number for the rows.
   * @param rows
   *          the rows to update, see {@link Row#forUpdate(String, String, Map)}
   * @param columns
   *          the {@link DbColumn} entities for the table
   * @param cc
   * @return the updated entities, not yet persisted
   * @throws ODKEntityNotFoundException
   *           if one of the rows does not exist in the datastore
   * @throws RowEtagMismatchException
   *           if one of the row's etags does not match the etag for the row in
   *           the datastore
   * @throws ODKDatastoreException
   */
  public List<Entity> updateRowEntities(Relation table, int modificationNumber, List<Row> rows,
      List<Entity> columns, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException, RowEtagMismatchException {
    Validate.notNull(table);
    Validate.isTrue(modificationNumber >= 0);
    Validate.noNullElements(rows);
    Validate.noNullElements(columns);
    Validate.notNull(cc);
    List<Entity> entities = new ArrayList<Entity>();
    for (Row row : rows) {
      entities.add(updateRowEntity(table, modificationNumber, row.getRowId(), row.getRowEtag(),
          row.getGroupOrUserId(), row.getValues(), row.isDeleted(), columns, cc));
    }
    return entities;
  }

  /**
   * Create a new {@link DbLogTable} row entity.
   * 
   * @param logTable
   *          the {@link DbLogTable} relation.
   * @param modificationNumber
   *          the modification number for the row.
   * @param row
   *          the row
   * @param columns
   *          the {@link DbColumn} entities for the log table
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   */
  public Entity newLogEntity(Relation logTable, int modificationNumber, Entity row,
      List<Entity> columns, CallingContext cc) throws ODKDatastoreException {
    Validate.notNull(logTable);
    Validate.isTrue(modificationNumber >= 0);
    Validate.notNull(row);
    Validate.noNullElements(columns);
    Validate.notNull(cc);

    Entity entity = logTable.newEntity(cc);
    entity.set(DbLogTable.ROW_ID, row.getId());
    entity.set(DbLogTable.ROW_VERSION, row.getString(DbTable.ROW_VERSION));
    entity.set(DbLogTable.MODIFICATION_NUMBER, modificationNumber);
    entity.set(DbLogTable.GROUP_OR_USER_ID, row.getString(DbTable.GROUP_OR_USER_ID));
    entity.set(DbLogTable.DELETED, row.getBoolean(DbTable.DELETED));

    for (Entity column : columns) {
      String idName = RUtil.convertIdentifier(column.getId());
      String value = row.getAsString(idName);
      entity.setAsString(idName, value);
    }
    return entity;
  }

  /**
   * Create a collection of new {@link DbLogTable} entities
   * 
   * @param logTable
   *          the {@link DbLogTable} relation
   * @param modificationNumber
   *          the modification number for the rows.
   * @param rows
   *          the rows
   * @param columns
   *          the {@link DbColumn} entities for the table
   * @param cc
   * @return the created entities, not yet persisted
   * @throws ODKDatastoreException
   */
  public List<Entity> newLogEntities(Relation logTable, int modificationNumber, List<Entity> rows,
      List<Entity> columns, CallingContext cc) throws ODKDatastoreException {
    Validate.notNull(logTable);
    Validate.isTrue(modificationNumber >= 0);
    Validate.noNullElements(rows);
    Validate.noNullElements(columns);
    Validate.notNull(cc);
    List<Entity> entities = new ArrayList<Entity>();
    for (Entity row : rows) {
      Entity entity = newLogEntity(logTable, modificationNumber, row, columns, cc);
      entities.add(entity);
    }
    return entities;
  }
}