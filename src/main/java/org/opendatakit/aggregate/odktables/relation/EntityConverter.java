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

package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions.DbColumnDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableAcl.DbTableAclEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions.DbTableDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry.DbTableEntryEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo.DbTableFileInfoEntity;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableAcl;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;

/**
 * Converts between datastore {@link Entity} objects and domain objects in
 * org.opendatakit.aggregate.odktables.entity.
 *
 * @author the.dylan.price@gmail.com
 * @author sudar.sam@gmail.com
 *
 */

public class EntityConverter {

  /**
   * Convert a {@link DbTableEntry} entity to a {@link TableEntry}
   */
  public TableEntry toTableEntry(DbTableEntryEntity entity) {
    String tableId = entity.getId();
    String dataETag = entity.getDataETag();
    String schemaETag = entity.getSchemaETag();
    TableEntry entry = new TableEntry(tableId, dataETag, schemaETag);
    return entry;
  }

  /**
   * Convert a list of {@link DbTableEntry} entities to a list of
   * {@link TableEntry}
   *
   * @param entities
   *          the entities to convert
   */
  public List<TableEntry> toTableEntries(List<DbTableEntryEntity> entities) {
    ArrayList<TableEntry> entries = new ArrayList<TableEntry>();
    if (entities != null) {
      for (DbTableEntryEntity entity : entities) {
        entries.add(toTableEntry(entity));
      }
    }
    return entries;
  }

  /**
   * Convert a {@link DbColumnDefinitions} entity to a {@link Column}
   */
  public Column toColumn(DbColumnDefinitionsEntity entity) {
    String tableId = entity.getTableId();
    String elementKey = entity.getElementKey();
    String elementName = entity.getElementName();
    String elementTypeStr = entity.getElementType();
    String listChildElementKeys = entity.getListChildElementKeys();
    Boolean isUnitOfRetention = entity.getIsUnitOfRetention();
    Column column = new Column(tableId, elementKey, elementName, elementTypeStr,
        listChildElementKeys, isUnitOfRetention);
    return column;
  }

  /**
   * Convert a list of {@link DbColumnDefinitions} entities to a list of
   * {@link Column} objects.
   */
  public ArrayList<Column> toColumns(List<DbColumnDefinitionsEntity> entities) {
    ArrayList<Column> columns = new ArrayList<Column>();
    for (DbColumnDefinitionsEntity entity : entities) {
      columns.add(toColumn(entity));
    }
    return columns;
  }

  /**
   * Return a TableDefinition based upon the {@link DbTableDefinitionsEntity}
   * parameter, which must have been generated from the
   * {@link DbTableDefinitions} relation. All fields are from the entity except
   * the columns, which are set to null.
   *
   * @param schemaEntity
   * @return
   */
  public TableDefinition toTableDefinition(TableEntry entryEntity,
      DbTableDefinitionsEntity schemaEntity) {
    String tableId = schemaEntity.getTableId();
    String schemaETag = schemaEntity.getSchemaETag();
    TableDefinition td = new TableDefinition(tableId, schemaETag, null);
    return td;
  }

  /**
   * Convert a {@link DbTableAcl} entity to a {@link TableAcl}
   */
  public TableAcl toTableAcl(DbTableAclEntity entity) {
    Scope.Type scopeType = Scope.Type.valueOf(entity.getScopeType());
    String scopeValue = entity.getScopeValue();
    Scope scope = new Scope(scopeType, scopeValue);
    TableRole role = TableRole.valueOf(entity.getRole());
    TableAcl acl = new TableAcl();
    acl.setRole(role);
    acl.setScope(scope);
    return acl;
  }

  /**
   * Convert a list of {@link DbTableAcl} entities to a list of {@link TableAcl}
   * .
   */
  public ArrayList<TableAcl> toTableAcls(List<DbTableAclEntity> entities) {
    ArrayList<TableAcl> acls = new ArrayList<TableAcl>();
    for (DbTableAclEntity entity : entities) {
      TableAcl acl = toTableAcl(entity);
      acls.add(acl);
    }
    return acls;
  }

  /**
   * Convert a {@link DbColumnDefinitions} entity to a {@link DataField}
   * <p>
   * We create fields on the server with boolean, integer, numeric and string
   * data types. Everything else is preserved as a string data type. This
   * includes dates and times, as thier representation varies across databases.
   * Dates are stored as yyyy-mm-ddThh:mm:ss.ssszzz and should sort in a
   * predictable way even though they remain strings. Similarly, times are
   * stored as hh:mm:ss.ssszzz and should also sort predictably.
   * <p>
   * JSON objects may sort unpredictably since the serialization order of an
   * object's properties is ill-defined.
   * </p>
   * <p>
   * Schema changes are now governed by a schemaETag, which is immutable (though
   * with some rework on the server, could become mutable).
   */
  public DataField toField(DbColumnDefinitionsEntity entity) {
    if (!entity.getIsUnitOfRetention()) {
      throw new IllegalArgumentException(
          "Attempt to get DataField for a non-persisted elementKey (" + entity.getElementKey()
              + ")");
    }
    String type = entity.getElementType();
    if (type.equals("boolean")) {
      return new DataField(entity.getElementKey().toUpperCase(), DataType.BOOLEAN, true);
    } else if (type.equals("integer")) {
      return new DataField(entity.getElementKey().toUpperCase(), DataType.INTEGER, true);
    } else if (type.equals("number")) {
      return new DataField(entity.getElementKey().toUpperCase(), DataType.DECIMAL, true);
    } else {
      return new DataField(entity.getElementKey().toUpperCase(), DataType.STRING, true);
    }
  }

  /**
   * Convert a list of {@link DbColumnDefinitions} entities to a list of
   * {@link DataField}
   */
  public List<DataField> toFields(List<DbColumnDefinitionsEntity> entities) {
    List<DataField> fields = new ArrayList<DataField>();
    for (DbColumnDefinitionsEntity entity : entities)
      if (entity.getIsUnitOfRetention()) {
        fields.add(toField(entity));
      }
    return fields;
  }

  public Scope getDbLogTableFilterScope(Entity entity) {
    String filterType = entity.getString(DbLogTable.FILTER_TYPE);
    if (filterType != null) {
      Scope.Type type = Scope.Type.valueOf(filterType);
      if (filterType.equals(Scope.Type.DEFAULT)) {
        return new Scope(Scope.Type.DEFAULT, null);
      } else {
        String value = entity.getString(DbLogTable.FILTER_VALUE);
        return new Scope(type, value);
      }
    } else {
      return Scope.EMPTY_SCOPE;
    }
  }

  public Scope getDbTableFilterScope(Entity entity) {
    String filterType = entity.getString(DbTable.FILTER_TYPE);
    if (filterType != null) {
      Scope.Type type = Scope.Type.valueOf(filterType);
      if (filterType.equals(Scope.Type.DEFAULT)) {
        return new Scope(Scope.Type.DEFAULT, null);
      } else {
        String value = entity.getString(DbTable.FILTER_VALUE);
        return new Scope(type, value);
      }
    } else {
      return Scope.EMPTY_SCOPE;
    }
  }

  public static Scope getDbTableFileInfoFilterScope(DbTableFileInfoEntity entity) {
    String filterType = entity.getStringField(DbTable.FILTER_TYPE);
    if (filterType != null) {
      Scope.Type type = Scope.Type.valueOf(filterType);
      if (filterType.equals(Scope.Type.DEFAULT)) {
        return new Scope(Scope.Type.DEFAULT, null);
      } else {
        String value = entity.getStringField(DbTable.FILTER_VALUE);
        return new Scope(type, value);
      }
    } else {
      return Scope.EMPTY_SCOPE;
    }
  }

  /**
   * Convert a {@link DbTable} entity into a {@link Row}. The returned row will
   * have the {@link DbTable} metadata columns such as _savepoint_timestamp and
   * row_version set.
   *
   * @param entity
   *          the {@link DbTable} entity.
   * @param columns
   *          the {@link DbColumnDefinitions} entities of the table
   * @return the row
   */
  public Row toRow(Entity entity, List<DbColumnDefinitionsEntity> columns) {
    Row row = new Row();
    row.setRowId(entity.getId());
    row.setRowETag(entity.getString(DbTable.ROW_ETAG));
    row.setDataETagAtModification(entity.getString(DbTable.DATA_ETAG_AT_MODIFICATION));
    row.setCreateUser(entity.getString(DbTable.CREATE_USER));
    row.setLastUpdateUser(entity.getString(DbTable.LAST_UPDATE_USER));
    row.setFilterScope(getDbTableFilterScope(entity));
    row.setDeleted(entity.getBoolean(DbTable.DELETED));

    row.setFormId(entity.getString(DbTable.FORM_ID));
    row.setLocale(entity.getString(DbTable.LOCALE));
    row.setSavepointType(entity.getString(DbTable.SAVEPOINT_TYPE));
    row.setSavepointTimestamp(entity.getString(DbTable.SAVEPOINT_TIMESTAMP));
    row.setSavepointCreator(entity.getString(DbTable.SAVEPOINT_CREATOR));

    row.setValues(getRowValues(entity, columns));
    return row;
  }

  /**
   * This method creates a row from an entity retrieved from the DbTableFileInfo
   * table. It makes use of the static List of String column names in that
   * class.
   *
   * @param entity
   * @return
   * @author sudar.sam@gmail.com
   */
  public static Row toRowFromFileInfo(DbTableFileInfoEntity entity) {
    Row row = new Row();
    row.setRowId(entity.getId());
    row.setRowETag(entity.getStringField(DbTable.ROW_ETAG));
    row.setDataETagAtModification(entity.getStringField(DbTable.DATA_ETAG_AT_MODIFICATION));
    row.setDeleted(entity.getBooleanField(DbTable.DELETED));
    row.setCreateUser(entity.getStringField(DbTable.CREATE_USER));
    row.setLastUpdateUser(entity.getStringField(DbTable.LAST_UPDATE_USER));
    row.setFilterScope(getDbTableFileInfoFilterScope(entity));
    // this will be the actual values of the row
    Map<String, String> values = new HashMap<String, String>();
    for (DataField column : DbTableFileInfo.exposedColumnNames) {
      Validate.isTrue(column.getDataType() == DataType.STRING);
      String value = entity.getStringField(column);
      values.put(column.getName(), value);
    }
    row.setValues(values);
    return row;
  }

  /**
   * Return a list of rows from a list of entities queried from the
   * DbTableFileInfo table. Just calls {@link toRowFromFileInfo()} for every
   * entity in the list. However, it does NOT include deleted rows.
   *
   * @param entities
   * @return
   */
  public static List<Row> toRowsFromFileInfo(List<DbTableFileInfoEntity> entities) {
    List<Row> rows = new ArrayList<Row>();
    for (DbTableFileInfoEntity e : entities) {
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
  public Row toRowFromLogTable(Entity entity, List<DbColumnDefinitionsEntity> columns) {
    Row row = new Row();
    row.setRowId(entity.getString(DbLogTable.ROW_ID));
    row.setRowETag(entity.getId());
    row.setDataETagAtModification(entity.getString(DbLogTable.DATA_ETAG_AT_MODIFICATION));
    row.setCreateUser(entity.getString(DbLogTable.CREATE_USER));
    row.setLastUpdateUser(entity.getString(DbLogTable.LAST_UPDATE_USER));
    row.setDeleted(entity.getBoolean(DbLogTable.DELETED));

    row.setFilterScope(getDbLogTableFilterScope(entity));
    row.setFormId(entity.getString(DbLogTable.FORM_ID));
    row.setLocale(entity.getString(DbLogTable.LOCALE));
    row.setSavepointType(entity.getString(DbLogTable.SAVEPOINT_TYPE));
    row.setSavepointTimestamp(entity.getString(DbLogTable.SAVEPOINT_TIMESTAMP));
    row.setSavepointCreator(entity.getString(DbLogTable.SAVEPOINT_CREATOR));

    row.setValues(getRowValues(entity, columns));
    return row;
  }

  public Map<String, String> getRowValues(Entity entity, List<DbColumnDefinitionsEntity> columns) {
    Map<String, String> values = new HashMap<String, String>();
    for (DbColumnDefinitionsEntity column : columns) {
      if (column.getIsUnitOfRetention()) {
        String name = column.getElementKey();
        String value = entity.getAsString(name.toUpperCase());
        values.put(name, value);
      }
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
  public List<Row> toRows(List<Entity> entities, List<DbColumnDefinitionsEntity> columns,
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