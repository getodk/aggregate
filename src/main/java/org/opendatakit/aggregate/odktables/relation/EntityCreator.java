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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.Sequencer;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions.DbColumnDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableAcl.DbTableAclEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions.DbTableDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry.DbTableEntryEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo.DbTableFileInfoEntity;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
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
   * @param pendingSchemaETag
   *          the unique Id for the (new) schema.
   * @param aprioriDataSequenceValue
   *          the monotonically increasing sequence value
   *          that is less than the lowest such value in
   *          this table's DbLogTable.
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   */
  public DbTableEntryEntity newTableEntryEntity(String tableId, String pendingSchemaETag,
      String aprioriDataSequenceValue, CallingContext cc) throws ODKDatastoreException {
    Validate.notNull(cc);
    Validate.notNull(tableId);
    Validate.notNull(pendingSchemaETag);
    Validate.notNull(aprioriDataSequenceValue);

    DbTableEntryEntity entity = DbTableEntry.createNewEntity(tableId, cc);
    entity.setPendingSchemaETag(pendingSchemaETag);
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
   * @param odkClientVersion
   * @param tableId
   * @param pathToFile
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public DbTableFileInfoEntity newTableFileInfoEntity(String odkClientVersion,
      String tableId, String pathToFile,
      TablesUserPermissions userPermissions,
      CallingContext cc) throws ODKDatastoreException {
    // first do some preliminary checks
    Validate.notEmpty(pathToFile);
    // TODO: check permissions to create a file...
    DbTableFileInfoEntity entity = DbTableFileInfo.createNewEntity(cc);
    entity.setOdkClientVersion(odkClientVersion);
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

  public void setRowFields(Entity row, String rowETag, String dataETagAtModification,
      String lastUpdateUser, boolean deleted,
      Scope filterScope, String formId, String locale, String savepointType,
      String savepointTimestamp, String savepointCreator, Map<String, String> values, List<DbColumnDefinitionsEntity> columns)
      throws BadColumnNameException {
    row.set(DbTable.ROW_ETAG, rowETag);
    row.set(DbTable.DATA_ETAG_AT_MODIFICATION, dataETagAtModification);
    row.set(DbTable.LAST_UPDATE_USER, lastUpdateUser);
    row.set(DbTable.DELETED, deleted);

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

    row.set(DbTable.FORM_ID, formId);
    row.set(DbTable.LOCALE, locale);
    row.set(DbTable.SAVEPOINT_TYPE, savepointType);
    row.set(DbTable.SAVEPOINT_TIMESTAMP, savepointTimestamp);
    row.set(DbTable.SAVEPOINT_CREATOR, savepointCreator);

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
   * NOTE: the rowETag is the primary key for this entry.
   * This is critical!!!! Do not re-use dataETag values across
   * rows!!!!
   *
   * @param logTable
   *          the {@link DbLogTable} relation.
   * @param dataETagAtModification
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
  public Entity newLogEntity(DbLogTable logTable, String dataETagAtModification,
      String previousRowETag, Entity row,
      List<DbColumnDefinitionsEntity> columns, Sequencer sequencer, CallingContext cc)
      throws ODKDatastoreException {
    Validate.notNull(logTable);
    Validate.notEmpty(dataETagAtModification);
    Validate.notNull(row);
    Validate.noNullElements(columns);
    Validate.notNull(cc);

    Entity entity = logTable.newEntity(row.getString(DbTable.ROW_ETAG), cc);
    entity.set(DbLogTable.ROW_ID, row.getId());
    entity.set(DbLogTable.SEQUENCE_VALUE, sequencer.getNextSequenceValue());

    entity.set(DbLogTable.PREVIOUS_ROW_ETAG, previousRowETag);
    entity.set(DbLogTable.DATA_ETAG_AT_MODIFICATION, dataETagAtModification);
    entity.set(DbLogTable.CREATE_USER, row.getString(DbTable.CREATE_USER));
    entity.set(DbLogTable.LAST_UPDATE_USER, row.getString(DbTable.LAST_UPDATE_USER));
    entity.set(DbLogTable.DELETED, row.getBoolean(DbTable.DELETED));

    // common metadata
    entity.set(DbLogTable.FILTER_TYPE, row.getString(DbTable.FILTER_TYPE));
    entity.set(DbLogTable.FILTER_VALUE, row.getString(DbTable.FILTER_VALUE));
    entity.set(DbLogTable.FORM_ID, row.getString(DbTable.FORM_ID));
    entity.set(DbLogTable.LOCALE, row.getString(DbTable.LOCALE));
    entity.set(DbLogTable.SAVEPOINT_TYPE, row.getString(DbTable.SAVEPOINT_TYPE));
    entity.set(DbLogTable.SAVEPOINT_TIMESTAMP, row.getString(DbTable.SAVEPOINT_TIMESTAMP));
    entity.set(DbLogTable.SAVEPOINT_CREATOR, row.getString(DbTable.SAVEPOINT_CREATOR));

    for (DbColumnDefinitionsEntity column : columns) {
      if (column.getIsUnitOfRetention()) {
        String value = row.getAsString(column.getElementKey().toUpperCase());
        entity.setAsString(column.getElementKey().toUpperCase(), value);
      }
    }
    return entity;
  }
}