package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.val;

import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Column.ColumnType;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;

/**
 * Converts between datastore {@link Entity} objects and domain objects in
 * org.opendatakit.aggregate.odktables.entity.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class EntityConverter {

  /**
   * Convert a {@link DbTableEntry} entity to a {@link TableEntry}
   */
  public TableEntry toTableEntry(Entity entity) {
    TableEntry entry = new TableEntry();
    entry.setTableId(entity.getId());
    entry.setDataEtag(entity.getAsString(DbTableEntry.MODIFICATION_NUMBER));
    return entry;
  }

  /**
   * Convert a list of {@link DbTableEntry} entities to a list of
   * {@link TableEntry}
   */
  public List<TableEntry> toTableEntryEntities(List<Entity> entities) {
    val entries = new ArrayList<TableEntry>();
    for (val entity : entities) {
      entries.add(toTableEntry(entity));
    }
    return entries;
  }

  /**
   * Convert a {@link DbColumn} entity to a {@link Column}
   */
  public Column toColumn(Entity entity) {
    Column column = new Column(entity.getString(DbColumn.COLUMN_NAME), ColumnType.valueOf(entity
        .getString(DbColumn.COLUMN_TYPE)));
    return column;
  }

  /**
   * Convert a list of {@link DbColumn} entities to a list of {@link Column}
   */
  public List<Column> toColumns(List<Entity> entities) {
    List<Column> columns = new ArrayList<Column>();
    for (Entity entity : entities) {
      columns.add(toColumn(entity));
    }
    return columns;
  }

  /**
   * Convert a {@link Column} to a {@link DataField}
   */
  public DataField toField(Column column) {
    DataField field = new DataField(RUtil.convertIdentifier(column.getName()),
        DataType.valueOf(column.getType().name()), true);
    return field;
  }

  /**
   * Convert a {@link DbColumn} entity to a {@link DataField}
   */
  public DataField toField(Entity entity) {
    DataField field = new DataField(RUtil.convertIdentifier(entity.getId()),
        DataType.valueOf(entity.getString(DbColumn.COLUMN_TYPE)), true);
    return field;
  }

  /**
   * Convert a list of {@link DbColumn} entities to a list of {@link DataField}
   */
  public List<DataField> toFields(List<Entity> entities) {
    List<DataField> fields = new ArrayList<DataField>();
    for (Entity entity : entities)
      fields.add(toField(entity));
    return fields;
  }

  /**
   * Convert a {@link DbTable} entity into a {@link Row}
   * 
   * @param entity
   *          the {@link DbTable} entity.
   * @param columns
   *          the {@link DbColumn} entities of the table
   * @return the row
   */
  public Row toRow(Entity entity, List<Entity> columns) {
    val row = new Row();
    row.setRowId(entity.getId());
    row.setRowEtag(entity.getString(DbTable.ROW_VERSION));
    row.setGroupOrUserId(entity.getString(DbTable.GROUP_OR_USER_ID));
    row.setDeleted(entity.getBoolean(DbTable.DELETED));

    val values = new HashMap<String, String>();
    for (Entity column : columns) {
      String name = column.getString(DbColumn.COLUMN_NAME);
      String value = entity.getAsString(RUtil.convertIdentifier(column.getId()));
      values.put(name, value);
    }
    row.setValues(values);
    return row;
  }

  /**
   * Convert a list of {@link DbTable} entities into a list of {@link Row}
   * 
   * @param entities
   *          the {@link DbTable} entities
   * @param columns
   *          the {@link DbColumn} of the table
   * @return the converted rows
   */
  public List<Row> toRows(List<Entity> entities, List<Entity> columns) {
    val rows = new ArrayList<Row>();
    for (Entity entity : entities) {
      rows.add(toRow(entity, columns));
    }
    return rows;
  }
}
