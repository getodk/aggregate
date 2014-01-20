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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.Sequencer;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions.DbColumnDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbKeyValueStore.DbKeyValueStoreEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableAcl.DbTableAclEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions.DbTableDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry.DbTableEntryEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo.DbTableFileInfoEntity;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

/**
 * Creates and updates new Entity objects for relations.
 * <p>
 * sudar.sam@gmail.com: This is deserving of its own class because the entities
 * (rows in the table) exist without any setters for their columns (DataField
 * objects). To insert a row in a particular class you would therefore have to
 * go look at the datafields contained in that entity and set them by hand. This
 * class handles that for you and aggregates them all into one place.
 *
 * @author the.dylan.price@gmail.com
 * @author sudar.sam@gmail.com
 *
 */

public class EntityCreator {
  public static final Log log = LogFactory.getLog(EntityCreator.class);

  public static final int INITIAL_MODIFICATION_NUMBER = 1;

  /**
   * Create a new {@link DbTableEntry} entity.
   *
   * @param tableId
   *          the table id. May be null to auto generate.
   * @param tableKey
   *          the unique Id for disambiguation purposes.
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   */
  public DbTableEntryEntity newTableEntryEntity(String tableId, String schemaETag,
      String aprioriDataSequenceValue, CallingContext cc) throws ODKDatastoreException {
    Validate.notNull(cc);
    Validate.notNull(schemaETag);
    Validate.notNull(aprioriDataSequenceValue);

    if (tableId == null) {
      tableId = CommonFieldsBase.newUri();
    }

    DbTableEntryEntity entity = DbTableEntry.createNewEntity(tableId, cc);
    String value = null;
    entity.setDataETag(value);
    entity.setSchemaETag(schemaETag);
    entity.setAprioriDataSequenceValue(aprioriDataSequenceValue);
    return entity;
  }

  /**
   * Create a new {@link DbColumnDefinitions} entity.
   *
   * @param tableId
   *          the id of the table for the new column.
   * @param column
   *          the new column.
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   */
  public DbColumnDefinitionsEntity newColumnEntity(String tableId, String schemaETag,
      Column column, CallingContext cc) throws ODKDatastoreException {
    Validate.notEmpty(tableId);
    Validate.notEmpty(schemaETag);
    Validate.notNull(column);
    Validate.notNull(cc);

    DbColumnDefinitionsEntity entity = DbColumnDefinitions.createNewEntity(cc);
    entity.setTableId(tableId);
    entity.setschemaETag(schemaETag);
    entity.setElementKey(column.getElementKey());
    entity.setElementName(column.getElementName());
    entity.setElementType(column.getElementType());
    entity.setListChildElementKeys(column.getListChildElementKeys());
    entity.setIsUnitOfRetention(column.getIsUnitOfRetention() != 0);

    return entity;
  }

  /**
   * Create a new {@link DbTableFileInfo} entity.
   *
   * @param tableId
   * @param pathToFile
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public DbTableFileInfoEntity newTableFileInfoEntity(String tableId, String pathToFile,
      TablesUserPermissionsImpl userPermissions,
      CallingContext cc) throws ODKDatastoreException {
    // first do some preliminary checks
    Validate.notEmpty(pathToFile);
    // TODO: check permissions to create a file...
    DbTableFileInfoEntity entity = DbTableFileInfo.createNewEntity(cc);
    entity.setTableId(tableId);
    entity.setPathToFile(pathToFile);

    // now set the universal fields
    // TODO: do the appropriate time stamping and include data for the other
    // fields.
    entity.setStringField(DbTable.CREATE_USER, userPermissions.getOdkTablesUserId());
    // TODO last update same as create? correct?
    entity.setStringField(DbTable.LAST_UPDATE_USER, userPermissions.getOdkTablesUserId());
    // TODO: should DATA_ETAG_AT_MODIFICATION also be from the TableEntry
    // record? Or tracked?
    entity.setStringField(DbTable.DATA_ETAG_AT_MODIFICATION, CommonFieldsBase.newUri());
    entity.setBooleanField(DbTable.DELETED, false);
    entity.setStringField(DbTable.ROW_ETAG, CommonFieldsBase.newUri());
    // TODO is this the right kind of scope to be setting? one wonders...
    entity.setStringField(DbTable.FILTER_VALUE, (String) null);
    entity.setStringField(DbTable.FILTER_TYPE, (String) null);

    return entity;
  }

  /**
   * Return a new Entity representing a table definition.
   *
   * @param tableId
   *          cannot be null
   * @param tableKey
   *          cannot be null
   * @param dbTableName
   *          cannot be null
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public DbTableDefinitionsEntity newTableDefinitionEntity(String tableId, String schemaETag,
      String dbTableName, CallingContext cc) throws ODKDatastoreException {
    // Validate those parameters defined as non-null in the ODK Tables Schema
    // Google doc.
    Validate.notEmpty(tableId);
    Validate.notEmpty(schemaETag);
    Validate.notEmpty(dbTableName);
    Validate.notNull(cc);
    DbTableDefinitionsEntity definition = DbTableDefinitions.createNewEntity(cc);
    definition.setTableId(tableId);
    definition.setSchemaETag(schemaETag);
    definition.setDbTableName(dbTableName);
    return definition;
  }

  /**
   *
   * @param tableId
   * @param partition
   * @param aspect
   * @param key
   * @param type
   * @param value
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public DbKeyValueStoreEntity newKeyValueStoreEntity(String tableId, String propertiesETag,
      String partition, String aspect, String key, String type, String value, CallingContext cc)
      throws ODKDatastoreException {
    Validate.notEmpty(tableId);
    Validate.notEmpty(propertiesETag);
    Validate.notEmpty(partition);
    Validate.notEmpty(aspect);
    Validate.notEmpty(key);
    Validate.notEmpty(type);
    Validate.notNull(cc);

    DbKeyValueStoreEntity entry = DbKeyValueStore.createNewEntity(cc);
    entry.setTableId(tableId);
    entry.setPropertiesETag(propertiesETag);
    entry.setPartition(partition);
    entry.setAspect(aspect);
    entry.setKey(key);
    entry.setType(type);
    entry.setValue(value);
    return entry;
  }

  public DbKeyValueStoreEntity newKeyValueStoreEntity(OdkTablesKeyValueStoreEntry entry,
      String propertiesETag, CallingContext cc) throws ODKDatastoreException {
    Validate.notNull(entry);
    Validate.notEmpty(propertiesETag);
    Validate.notNull(cc);

    String tableId = entry.tableId;
    String partition = entry.partition;
    String aspect = entry.aspect;
    String key = entry.key;
    String type = entry.type;
    String value = entry.value;
    return newKeyValueStoreEntity(tableId, propertiesETag, partition, aspect, key, type, value, cc);
  }

  /**
   * Create a new {@link DbTableAcl} entity.
   *
   * @param tableId
   *          the id of the table for the new table acl entity
   * @param scope
   *          the scope of the acl
   * @param role
   *          the role to be applied to the given scope
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   */
  public DbTableAclEntity newTableAclEntity(String tableId, Scope scope, TableRole role,
      CallingContext cc) throws ODKDatastoreException {
    Validate.notEmpty(tableId);
    Validate.notNull(scope);
    // can't have an empty scope type in an acl entity
    Validate.notNull(scope.getType());
    Validate.notNull(role);
    Validate.notNull(cc);

    DbTableAclEntity tableAcl = DbTableAcl.createNewEntity(cc);
    tableAcl.setTableId(tableId);
    tableAcl.setScopeType(scope.getType().name());
    tableAcl.setScopeValue(scope.getValue());
    tableAcl.setRole(role.name());

    return tableAcl;
  }

  public Entity insertOrUpdateRowEntity(DbTable table, Entity row, boolean found, String rowId,
      String dataETag, String currentETag, Scope filter, String uriAccessControl, String formId,
      String locale, Long savepointTimestamp, Map<String, String> values,
      List<DbColumnDefinitionsEntity> columns, TablesUserPermissions userPermissions, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException, ETagMismatchException,
      BadColumnNameException, PermissionDeniedException {
    Validate.notNull(table);
    Validate.notEmpty(dataETag);
    Validate.notEmpty(rowId);
    // if currentETag is null we will catch it later
    Validate.noNullElements(values.keySet());
    // filter may be null
    Validate.noNullElements(columns);
    Validate.notNull(cc);

    if (!found) {
      row.set(DbTable.CREATE_USER, userPermissions.getOdkTablesUserId());
    } else {
      String rowETag = row.getString(DbTable.ROW_ETAG);
      if (currentETag == null || !currentETag.equals(rowETag)) {
        // TODO: make this more intelligent?
        // the rows may be identical, but leave that to the client to determine
        // trigger client-side conflict resolution
        throw new ETagMismatchException(String.format("%s does not match %s " + "for rowId %s",
            currentETag, rowETag, row.getId()));
      }
    }

    setRowFields(row, dataETag, userPermissions, filter, false, uriAccessControl, formId, locale,
        savepointTimestamp, values, columns);
    return row;
  }

  private void setRowFields(Entity row, String dataETag, TablesUserPermissions userPermissions, Scope filterScope,
      boolean deleted, String uriAccessControl, String formId, String locale,
      Long savepointTimestamp, Map<String, String> values, List<DbColumnDefinitionsEntity> columns)
      throws BadColumnNameException {
    row.set(DbTable.ROW_ETAG, CommonFieldsBase.newUri());
    row.set(DbTable.DATA_ETAG_AT_MODIFICATION, dataETag);
    row.set(DbTable.LAST_UPDATE_USER, userPermissions.getOdkTablesUserId());

    // if filterScope is null, don't change the value
    // if filterScope is the empty scope, set both filter type and value to null
    // if filterScope is the default scope, make sure filter value is null
    // else set both filter type and value to the values in filterScope
    if (filterScope != null) {
      Scope.Type filterType = filterScope.getType();
      String filterValue = filterScope.getValue();
      if (filterType == null) {
        row.set(DbTable.FILTER_TYPE, (String) null);
        row.set(DbTable.FILTER_VALUE, (String) null);
      } else if (filterType.equals(Scope.Type.DEFAULT)) {
        row.set(DbTable.FILTER_TYPE, filterType.name());
        row.set(DbTable.FILTER_VALUE, (String) null);
      } else {
        row.set(DbTable.FILTER_TYPE, filterType.name());
        row.set(DbTable.FILTER_VALUE, filterValue);
      }
    }

    row.set(DbTable.DELETED, deleted);

    row.set(DbTable.URI_ACCESS_CONTROL, uriAccessControl);
    row.set(DbTable.FORM_ID, formId);
    row.set(DbTable.LOCALE, locale);
    row.set(DbTable.SAVEPOINT_TIMESTAMP, savepointTimestamp);

    for (Entry<String, String> entry : values.entrySet()) {
      String value = entry.getValue();
      String name = entry.getKey();
      // There are three possbilities here.
      // 1) The key is a shared metadata column that SHOULD be synched.
      // 2) The key is a client only metadata column that should NOT be synched
      // 3) The key is a user-defined column that SHOULD be synched.
      if (TableConstants.CLIENT_ONLY_COLUMN_NAMES.contains(name)) {
        // 1) -- we should catch this and forbid it
        throw new BadColumnNameException("Client-only column name " + name
            + " should never be transmitted to server");
      } else if (TableConstants.SHARED_COLUMN_NAMES.contains(name)) {
        // 2) -- we should catch this and forbid it
        throw new BadColumnNameException("Shared column name " + name
            + " should be passed using its reserved field");
      } else {
        // 3) --add it to the user-defined column
        DbColumnDefinitionsEntity column = findColumn(name, columns);
        if (column == null) {
          // If we don't have a colum in the aggregate db, it's ok if it's one
          // of the Tables-only columns. Otherwise it's an error.
          log.error("bad column name: " + name);
          throw new BadColumnNameException("Bad column name " + name);
        } else if (column.getIsUnitOfRetention()) {
          row.setAsString(column.getElementKey().toUpperCase(), value);
        }
      }
    }
  }

  private DbColumnDefinitionsEntity findColumn(String elementKey,
      List<DbColumnDefinitionsEntity> columns) {
    for (DbColumnDefinitionsEntity entity : columns) {
      String colName = entity.getElementKey();
      if (elementKey.equals(colName))
        return entity;
    }
    return null;
  }

  /**
   * Create a new {@link DbLogTable} row entity.
   *
   * @param logTable
   *          the {@link DbLogTable} relation.
   * @param dataETag
   *          the data etag at the time of creation
   * @param row
   *          the row
   * @param columns
   *          the {@link DbColumnDefinitions} entities for the log table
   * @param sequencer
   *          the sequencer for ordering the log entries
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   */
  public Entity newLogEntity(DbLogTable logTable, String dataETag, Entity row,
      List<DbColumnDefinitionsEntity> columns, Sequencer sequencer, CallingContext cc)
      throws ODKDatastoreException {
    Validate.notNull(logTable);
    Validate.notEmpty(dataETag);
    Validate.notNull(row);
    Validate.noNullElements(columns);
    Validate.notNull(cc);

    Entity entity = logTable.newEntity(cc);
    entity.set(DbLogTable.ROW_ID, row.getId());
    entity.set(DbLogTable.SEQUENCE_VALUE, sequencer.getNextSequenceValue());

    entity.set(DbLogTable.ROW_ETAG, row.getString(DbTable.ROW_ETAG));
    entity.set(DbLogTable.DATA_ETAG_AT_MODIFICATION, dataETag);
    entity.set(DbLogTable.CREATE_USER, row.getString(DbTable.CREATE_USER));
    entity.set(DbLogTable.LAST_UPDATE_USER, row.getString(DbTable.LAST_UPDATE_USER));
    entity.set(DbLogTable.FILTER_TYPE, row.getString(DbTable.FILTER_TYPE));
    entity.set(DbLogTable.FILTER_VALUE, row.getString(DbTable.FILTER_VALUE));
    entity.set(DbLogTable.DELETED, row.getBoolean(DbTable.DELETED));

    // common metadata
    entity.set(DbLogTable.URI_ACCESS_CONTROL, row.getString(DbTable.URI_ACCESS_CONTROL));
    entity.set(DbLogTable.FORM_ID, row.getString(DbTable.FORM_ID));
    entity.set(DbLogTable.LOCALE, row.getString(DbTable.LOCALE));
    entity.set(DbLogTable.SAVEPOINT_TIMESTAMP, row.getLong(DbTable.SAVEPOINT_TIMESTAMP));

    for (DbColumnDefinitionsEntity column : columns) {
      if (column.getIsUnitOfRetention()) {
        String value = row.getAsString(column.getElementKey().toUpperCase());
        entity.setAsString(column.getElementKey().toUpperCase(), value);
      }
    }
    return entity;
  }

  /**
   * Create a collection of new {@link DbLogTable} entities
   *
   * @param logTable
   *          the {@link DbLogTable} relation
   * @param dataETag
   *          the data etag at the time of the updates
   * @param rows
   *          the rows
   * @param columns
   *          the {@link DbColumnDefinitions} entities for the table
   * @param sequencer
   *          the sequencer for ordering the log entries
   * @param cc
   * @return the created entities, not yet persisted
   * @throws ODKDatastoreException
   */
  public List<Entity> newLogEntities(DbLogTable logTable, String dataETag, List<Entity> rows,
      List<DbColumnDefinitionsEntity> columns, Sequencer sequencer, CallingContext cc)
      throws ODKDatastoreException {
    Validate.notNull(logTable);
    Validate.notEmpty(dataETag);
    Validate.noNullElements(rows);
    Validate.noNullElements(columns);
    Validate.notNull(cc);
    List<Entity> entities = new ArrayList<Entity>();
    for (Entity row : rows) {
      Entity entity = newLogEntity(logTable, dataETag, row, columns, sequencer, cc);
      entities.add(entity);
    }
    return entities;
  }

}