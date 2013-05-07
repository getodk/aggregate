package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Column.ColumnType;
import org.opendatakit.aggregate.odktables.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableAcl;
import org.opendatakit.aggregate.odktables.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.TableProperties;
import org.opendatakit.aggregate.odktables.entity.TableRole;
import org.opendatakit.aggregate.odktables.entity.api.TableType;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.web.CallingContext;

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
  public TableEntry toTableEntry(Entity entity, String tableKey) {
    String tableId = entity.getId();
    String dataEtag = entity.getAsString(DbTableEntry.MODIFICATION_NUMBER);
    String propertiesEtag = 
        entity.getAsString(DbTableEntry.PROPERTIES_MOD_NUM);
    TableEntry entry = new TableEntry(tableId, tableKey, dataEtag, 
        propertiesEtag);
    return entry;
  }

  /**
   * Convert a list of {@link DbTableEntry} entities to a list of
   * {@link TableEntry}
   * 
   * @param entities
   *          the entities to convert
   * @param tableKeys
   *          a map of tableIds to tableKeys
   */
  public List<TableEntry> toTableEntries(List<Entity> entities, 
      Map<String, String> tableKeys) {
    ArrayList<TableEntry> entries = new ArrayList<TableEntry>();
    for (int i = 0; i < entities.size(); i++) {
      Entity entity = entities.get(i);
      String tableKey = tableKeys.get(entity.getId());
      entries.add(toTableEntry(entity, tableKey));
    }
    return entries;
  }

  /**
   * Convert a {@link DbColumnDefinitions} entity to a {@link Column}
   */
  public Column toColumn(Entity entity) {
    String tableId = entity.getString(DbColumnDefinitions.TABLE_ID);
    String elementKey = entity.getString(DbColumnDefinitions.ELEMENT_KEY);
    String elementName = entity.getString(DbColumnDefinitions.ELEMENT_NAME);
    String elementTypeStr = entity.getString(DbColumnDefinitions.ELEMENT_TYPE);
    ColumnType elementType = ColumnType.valueOf(elementTypeStr);
    String listChildElementKeys = 
        entity.getString(DbColumnDefinitions.LIST_CHILD_ELEMENT_KEYS);
    int isPersisted = entity.getInteger(DbColumnDefinitions.IS_PERSISTED);
    String joins = entity.getString(DbColumnDefinitions.JOINS);
    Column column = new Column(tableId, elementKey, elementName, elementType,
        listChildElementKeys, isPersisted, joins);
    return column;
  }

  /**
   * Convert a list of {@link DbColumnDefinitions} entities to a list of {@link Column}
   */
  public List<Column> toColumns(List<Entity> entities) {
    List<Column> columns = new ArrayList<Column>();
    for (Entity entity : entities) {
      columns.add(toColumn(entity));
    }
    return columns;
  }

// ss: I think we can do away with this since we're doing away with the 
// DbTableProperties table.
//  /**
//   * Convert a {@link DbTableProperties} entity to a {@link TableProperties}.
//   */
//  public TableProperties toTableProperties(Entity entity, String propertiesEtag) {
//    TableProperties properties = new TableProperties(propertiesEtag,
//        entity.getString(DbTableProperties.TABLE_NAME),
//        entity.getString(DbTableProperties.TABLE_METADATA));
//    return properties;
//  }
  
  public TableProperties toTableProperties(List<Entity> kvsEntities,
      String tableKey, String propertiesEtag) {
    List<OdkTablesKeyValueStoreEntry> kvsEntries = 
        toOdkTablesKeyValueStoreEntry(kvsEntities);
    TableProperties properties = new TableProperties(propertiesEtag, tableKey,
        kvsEntries);
    return properties;
  }
  
  public OdkTablesKeyValueStoreEntry toOdkTablesKeyValueStoreEntry(
      Entity entity) {
    String tableId = entity.getString(DbKeyValueStore.TABLE_ID);
    String partition = entity.getString(DbKeyValueStore.PARTITION);
    String aspect = entity.getString(DbKeyValueStore.ASPECT);
    String key = entity.getString(DbKeyValueStore.KEY);
    String type = entity.getString(DbKeyValueStore.TYPE);
    String value = entity.getString(DbKeyValueStore.VALUE);
    OdkTablesKeyValueStoreEntry entry = new OdkTablesKeyValueStoreEntry();
    entry.tableId = tableId;
    entry.partition = partition;
    entry.aspect = aspect;
    entry.key = key;
    entry.type = type;
    entry.value = value;
    return entry;
  }
  
  /**
   * Return a TableDefinition based upon the {@link Entity} parameter, which
   * must have been generated from the {@link DbTableDefinitions} relation.
   * All fields are from the entity except the columns, which are set to null.
   * @param definitionEntity
   * @return
   */
  public TableDefinition toTableDefinition(Entity definitionEntity) {
    String tableId = definitionEntity.getString(DbTableDefinitions.TABLE_ID);
    String tableKey = definitionEntity.getString(DbTableDefinitions.TABLE_KEY);
    String dbTableName = 
        definitionEntity.getString(DbTableDefinitions.TABLE_KEY);
    String tableTypeStr = definitionEntity.getString(DbTableDefinitions.TYPE);
    TableType tableType = TableType.valueOf(tableTypeStr);
    String tableIdAccessControls = definitionEntity.getString(
        DbTableDefinitions.TABLE_ID_ACCESS_CONTROLS);
    return new TableDefinition(tableId, null, tableKey, dbTableName, tableType,
        tableIdAccessControls);
  }
  
  public List<OdkTablesKeyValueStoreEntry> toOdkTablesKeyValueStoreEntry(
      List<Entity> kvsEntities) {
    List<OdkTablesKeyValueStoreEntry> kvsEntries = 
        new ArrayList<OdkTablesKeyValueStoreEntry>();
    for (Entity entity : kvsEntities) {
      kvsEntries.add(toOdkTablesKeyValueStoreEntry(entity));
    }
    return kvsEntries;
  }

  /**
   * Convert a {@link DbTableAcl} entity to a {@link TableAcl}
   */
  public TableAcl toTableAcl(Entity entity) {
    Scope.Type scopeType = Scope.Type.valueOf(entity.getString(DbTableAcl.SCOPE_TYPE));
    String scopeValue = entity.getString(DbTableAcl.SCOPE_VALUE);
    Scope scope = new Scope(scopeType, scopeValue);
    TableRole role = TableRole.valueOf(entity.getString(DbTableAcl.ROLE));
    TableAcl acl = new TableAcl();
    acl.setRole(role);
    acl.setScope(scope);
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
    // ss: exactly what the point of this method is eludes me. However, I 
    // believe that the "type" of an ODK Tables Column on the aggregate side
    // is always a string. Tables permits more complicated types like image,
    // location, etc, and therefore there is no way/reason to map each level
    // to the aggregate side.
    DataField field = new DataField(RUtil.convertIdentifier(column.getElementKey()),
        DataType.STRING, true);
    return field;
  }

  /**
   * Convert a {@link DbColumnDefinitions} entity to a {@link DataField}
   */
  public DataField toField(Entity entity) {
    // ss: exactly what the point of this method is eludes me. However, I 
    // believe that the "type" of an ODK Tables Column on the aggregate side
    // is always a string. Tables permits more complicated types like image,
    // location, etc, and therefore there is no way/reason to map each level
    // to the aggregate side.
    DataField field = new DataField(RUtil.convertIdentifier(entity.getId()),
        DataType.STRING, true);
    return field;
  }

  /**
   * Convert a list of {@link DbColumnDefinitions} entities to a list of {@link DataField}
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
   *          the {@link DbColumnDefinitions} entities of the table
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
      if (filterType.equals(Scope.Type.DEFAULT)) {
        row.setFilterScope(new Scope(Scope.Type.DEFAULT, null));
      } else {
        String value = entity.getString(DbTable.FILTER_VALUE);
        row.setFilterScope(new Scope(type, value));
      }
    } else {
      row.setFilterScope(Scope.EMPTY_SCOPE);
    }

    row.setValues(getRowValues(entity, columns));
    return row;
  }
  
  /**
   * This method creates a row from an entity retrieved from the 
   * DbTableFileInfo table. It makes use of the static List of 
   * String column names in that class.
   * @param entity
   * @return
   * @author sudar.sam@gmail.com
   */
  public static Row toRowFromFileInfo(Entity entity) {
	  Row row = new Row();
	  row.setRowId(entity.getId());
	  row.setRowEtag(entity.getString(DbTable.ROW_VERSION));
	  row.setDeleted(entity.getBoolean(DbTable.DELETED));
	  row.setCreateUser(entity.getString(DbTable.CREATE_USER));
	  row.setLastUpdateUser(entity.getString(DbTable.LAST_UPDATE_USER));
	  String filterType = entity.getString(DbTable.FILTER_TYPE);
	  if (filterType != null) {
	      Scope.Type type = Scope.Type.valueOf(filterType);
	      if (filterType.equals(Scope.Type.DEFAULT)) {
	      row.setFilterScope(new Scope(Scope.Type.DEFAULT, null));
	        } else {
	          String value = entity.getString(DbTable.FILTER_VALUE);
	          row.setFilterScope(new Scope(type, value));
	        }
	      } else {
	        row.setFilterScope(Scope.EMPTY_SCOPE);
	      }
	  // this will be the actual values of the row
	  Map<String, String> values = new HashMap<String, String>();
	  for (String columnName : DbTableFileInfo.columnNames) {
	    String value = entity.getAsString(columnName);
	    values.put(columnName, value);
	  }
	  row.setValues(values);
	  return row;  
  }
  
  /**
   * Return a list of rows from a list of entities queried from the
   * DbTableFileInfo table. Just calls {@link toRowFromFileInfo()} 
   * for every entity in the list. However, it does NOT include
   * deleted rows.
   * @param entities
   * @return
   */
  public static List<Row> toRowsFromFileInfo(List<Entity> entities) {
	  List<Row> rows = new ArrayList<Row>();
	  for (Entity e : entities) {
		  Row row = toRowFromFileInfo(e);
		  if (!row.isDeleted()) {
			  rows.add(row);
		  }
	  }
	  return rows;
  }

  /**
   * Convert a {@link DbLogTable} entity into a {@link Row}
   * 
   * @param entity
   *          the {@link DbLogTable} entity.
   * @param columns
   *          the {@link DbColumnDefinitions} entities of the table
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
      if (type.equals(Scope.Type.DEFAULT)) {
        row.setFilterScope(new Scope(Scope.Type.DEFAULT, null));
      } else {
        String value = entity.getString(DbLogTable.FILTER_VALUE);
        row.setFilterScope(new Scope(type, value));
      }
    } else {
      row.setFilterScope(Scope.EMPTY_SCOPE);
    }

    row.setValues(getRowValues(entity, columns));
    return row;
  }

  private Map<String, String> getRowValues(Entity entity, List<Entity> columns) {
    Map<String, String> values = new HashMap<String, String>();
    for (Entity column : columns) {
      String name = column.getString(DbColumnDefinitions.ELEMENT_KEY);
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
   *          the {@link DbColumnDefinitions} of the table
   * @param fromLogTable
   *          true if the rows are from the {@link DbLogTable}
   * @return the converted rows
   */
  public List<Row> toRows(List<Entity> entities, List<Entity> columns, 
      boolean fromLogTable) {
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