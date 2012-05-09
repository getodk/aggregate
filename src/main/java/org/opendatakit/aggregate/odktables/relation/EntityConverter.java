package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Column.ColumnType;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableAcl;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.TableProperties;
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
  public TableEntry toTableEntry(Entity entity, String tableName) {
    String tableId = entity.getId();
    String dataEtag = entity.getAsString(DbTableEntry.MODIFICATION_NUMBER);
    String propertiesEtag = entity.getAsString(DbTableEntry.PROPERTIES_MOD_NUM);
    TableEntry entry = new TableEntry(tableId, tableName, dataEtag, propertiesEtag);
    return entry;
  }

  /**
   * Convert a list of {@link DbTableEntry} entities to a list of
   * {@link TableEntry}
   * 
   * @param entities
   *          the entities to convert
   * @param tableNames
   *          each entry i in tableNames is the name of the table in entry i of
   *          entities
   */
  public List<TableEntry> toTableEntries(List<Entity> entities, List<String> tableNames) {
    ArrayList<TableEntry> entries = new ArrayList<TableEntry>();
    for (int i = 0; i < entities.size(); i++) {
      Entity entity = entities.get(i);
      String tableName = tableNames.get(i);
      entries.add(toTableEntry(entity, tableName));
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
   * Convert a {@link DbTableProperties} entity to a {@link TableProperties}.
   */
  public TableProperties toTableProperties(Entity entity, String propertiesEtag) {
    TableProperties properties = new TableProperties(propertiesEtag,
        entity.getString(DbTableProperties.TABLE_NAME),
        entity.getString(DbTableProperties.TABLE_METADATA));
    return properties;
  }

  /**
   * Convert a {@link DbTableAcl} entity to a {@link TableAcl}
   */
  public TableAcl toTableAcl(Entity entity) {
    String tableId = entity.getString(DbTableAcl.TABLE_ID);
    Scope.Type scopeType = Scope.Type.valueOf(entity.getString(DbTableAcl.SCOPE_TYPE));
    String scopeValue = entity.getString(DbTableAcl.SCOPE_VALUE);
    Scope scope = new Scope(scopeType, scopeValue);
    String permissions = entity.getString(DbTableAcl.PERMISSIONS);
    TableAcl acl = new TableAcl(tableId, scope, RUtil.toPermissionsList(permissions));
    return acl;
  }

  /**
   * Convert a list of {@link DbTableAcl} entities to a list of {@link TableAcl}
   * .
   */
  public List<TableAcl> toTableAcls(List<Entity> entities) {
    List<TableAcl> acls = new ArrayList<TableAcl>();
    for (Entity entity : entities) {
      TableAcl acl = toTableAcl(entity);
      acls.add(acl);
    }
    return acls;
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
    Row row = new Row();
    row.setRowId(entity.getId());
    row.setRowEtag(entity.getString(DbTable.ROW_VERSION));
    row.setDeleted(entity.getBoolean(DbTable.DELETED));
    row.setCreateUser(entity.getString(DbTable.CREATE_USER));
    row.setLastUpdateUser(entity.getString(DbTable.LAST_UPDATE_USER));
    String filterType = entity.getString(DbTable.FILTER_TYPE);
    if (filterType != null) {
      Scope.Type type = Scope.Type.valueOf(filterType);
      String value = entity.getString(DbTable.FILTER_VALUE);
      row.setFilterScope(new Scope(type, value));
    }

    row.setValues(getRowValues(entity, columns));
    return row;
  }

  /**
   * Convert a {@link DbLogTable} entity into a {@link Row}
   * 
   * @param entity
   *          the {@link DbLogTable} entity.
   * @param columns
   *          the {@link DbColumn} entities of the table
   * @return the row
   */
  public Row toRowFromLogTable(Entity entity, List<Entity> columns) {
    Row row = new Row();
    row.setRowId(entity.getString(DbLogTable.ROW_ID));
    row.setRowEtag(entity.getString(DbLogTable.ROW_VERSION));
    row.setDeleted(entity.getBoolean(DbLogTable.DELETED));
    row.setCreateUser(entity.getString(DbLogTable.CREATE_USER));
    row.setLastUpdateUser(entity.getString(DbLogTable.LAST_UPDATE_USER));
    String filterType = entity.getString(DbLogTable.FILTER_TYPE);
    if (filterType != null) {
      Scope.Type type = Scope.Type.valueOf(filterType);
      String value = entity.getString(DbLogTable.FILTER_VALUE);
      row.setFilterScope(new Scope(type, value));
    }

    row.setValues(getRowValues(entity, columns));
    return row;
  }

  private Map<String, String> getRowValues(Entity entity, List<Entity> columns) {
    Map<String, String> values = new HashMap<String, String>();
    for (Entity column : columns) {
      String name = column.getString(DbColumn.COLUMN_NAME);
      String value = entity.getAsString(RUtil.convertIdentifier(column.getId()));
      values.put(name, value);
    }
    return values;
  }

  /**
   * Convert a list of {@link DbTable} or {@link DbLogTable} entities into a
   * list of {@link Row}
   * 
   * @param entities
   *          the {@link DbTable} or {@link DbLogTable} entities
   * @param columns
   *          the {@link DbColumn} of the table
   * @param fromLogTable
   *          true if the rows are from the {@link DbLogTable}
   * @return the converted rows
   */
  public List<Row> toRows(List<Entity> entities, List<Entity> columns, boolean fromLogTable) {
    ArrayList<Row> rows = new ArrayList<Row>();
    for (Entity entity : entities) {
      if (fromLogTable)
        rows.add(toRowFromLogTable(entity, columns));
      else
        rows.add(toRow(entity, columns));
    }
    return rows;
  }
}