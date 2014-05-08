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

package org.opendatakit.aggregate.odktables;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions.DbColumnDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbLogTable;
import org.opendatakit.aggregate.odktables.relation.DbTable;
import org.opendatakit.aggregate.odktables.relation.DbTableAcl;
import org.opendatakit.aggregate.odktables.relation.DbTableAcl.DbTableAclEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions.DbTableDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry.DbTableEntryEntity;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.aggregate.odktables.relation.RUtil;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
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
  private TablesUserPermissions userPermissions;
  private String appId;

  public TableManager(String appId, TablesUserPermissions userPermissions, CallingContext cc)
      throws ODKDatastoreException {
    this.cc = cc;
    this.appId = appId;
    this.userPermissions = userPermissions;
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
    List<TableEntry> tables = converter.toTableEntries(DbTableEntry.query(cc));
    List<TableEntry> filteredList = new ArrayList<TableEntry>();
    for (TableEntry e : tables) {
      if (userPermissions.hasPermission(appId, e.getTableId(), TablePermission.READ_TABLE_ENTRY)) {
        filteredList.add(e);
      }
    }
    return filteredList;
  }

  /**
   * Retrieve a list of all table entries in the datastore that the given scopes
   * are allowed to see.
   *
   * @param scopes
   *          the scopes
   * @return a list of table entries which the given scopes are allowed to see
   * @throws ODKDatastoreException
   */
  public List<TableEntry> getTables(List<Scope> scopes) throws ODKDatastoreException {
    // TODO: rework to use getTables() to get everything, then filter out
    // the non-accessible tables. Eliminate the queryNotEqual() what we
    // want to do is get the full ACL of each table, and see if the scope
    // list it has matches against one of the scopes in our list.

    // Oct15--changing this just to point to get ALL the tables. This is to
    // avoid the permissions issue we have for now, as everything should be
    // getting tables through this class.
    // TODO fix this to again get things to point at scopes.
    /*
     * // get table ids for entries that the given scopes can see List<Entity>
     * aclEntities = DbTableAcl.queryNotEqual(TableRole.NONE, cc); List<String>
     * tableIds = new ArrayList<String>();
     *
     * for (Entity aclEntity : aclEntities) { TableAcl acl =
     * converter.toTableAcl(aclEntity); if (scopes.contains(acl.getScope())) {
     * tableIds.add(aclEntity.getString(DbTableAcl.TABLE_ID)); } }
     *
     * Query query =
     * DbTableEntry.getRelation(cc).query("TableManager.getTables(List<Scope>)",
     * cc); query.include(CommonFieldsBase.URI_COLUMN_NAME, tableIds);
     * List<Entity> entries = query.execute(); return getTableEntries(entries);
     */
    // List<TableEntry> tables = getTables();
    return getTables();
  }

  /**
   * Retrieve the table entry for the given tableId.
   *
   * @param tableId
   *          the id of a table
   * @return the table entry, or null if no such table exists
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  public TableEntry getTable(String tableId) throws ODKDatastoreException,
      PermissionDeniedException {
    userPermissions.checkPermission(appId, tableId, TablePermission.READ_TABLE_ENTRY);

    Validate.notNull(tableId);
    Validate.notEmpty(tableId);

    // get table entry
    // refresh entry
    DbTableEntryEntity entryEntity = null;
    try {
      entryEntity = DbTableEntry.getTableIdEntry(tableId, cc);
    } catch (ODKEntityNotFoundException e) {
      return null;
    }

    if (entryEntity != null && entryEntity.getSchemaETag() != null) {
      return converter.toTableEntry(entryEntity);
    } else {
      return null;
    }
  }

  /**
   * Retrieve the TableDefinition for the table with the given id.
   *
   * @param tableId
   * @return
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   */
  public TableDefinition getTableDefinition(String tableId) throws ODKDatastoreException,
      PermissionDeniedException, ODKTaskLockException {
    Validate.notEmpty(tableId);
    TableEntry entry = null;
    DbTableDefinitionsEntity definitionEntity = null;
    List<DbColumnDefinitionsEntity> columnEntities = null;
    LockTemplate propsLock = new LockTemplate(tableId, ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      propsLock.acquire();

      entry = getTable(tableId);
      if (entry == null) {
        return null;
      }
      String schemaETag = entry.getSchemaETag();
      if ( schemaETag == null ) {
        return null;
      }
      definitionEntity = DbTableDefinitions.getDefinition(tableId, schemaETag, cc);
      columnEntities = DbColumnDefinitions.query(tableId, schemaETag, cc);

    } finally {
      propsLock.release();
    }

    if ( entry != null && definitionEntity != null && columnEntities != null ) {
      TableDefinition definition = converter.toTableDefinition(entry, definitionEntity);
      ArrayList<Column> columns = converter.toColumns(columnEntities);
      definition.setColumns(columns);
      return definition;
    }

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
   * @throws PermissionDeniedException
   */
  public TableEntry getTableNullSafe(String tableId) throws ODKEntityNotFoundException,
      ODKDatastoreException, PermissionDeniedException {
    Validate.notNull(tableId);
    Validate.notEmpty(tableId);
    userPermissions.checkPermission(appId, tableId, TablePermission.READ_TABLE_ENTRY);
    // get table entry entity
    DbTableEntryEntity entryEntity = DbTableEntry.getTableIdEntry(tableId, cc);
    return converter.toTableEntry(entryEntity);
  }

  /**
   * Creates a new table. The table has 'tableId' as its display name. It has no
   * table type designation.
   *
   * @param tableId
   *          the unique identifier for the table
   * @param columns
   *          the columns the table should have
   * @return a table entry representing the newly created table
   * @throws TableAlreadyExistsException
   *           if a table with the given table id already exists
   * @throws ODKEntityPersistException
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   */
  public TableEntry createTable(String tableId, List<Column> columns)
      throws ODKEntityPersistException, ODKDatastoreException,
      TableAlreadyExistsException, PermissionDeniedException, ODKTaskLockException {
    Validate.notNull(tableId);
    Validate.notEmpty(tableId);
    Validate.noNullElements(columns);

    // Idempotent action: creating a table succeeds if the table already exists with a matching schema.

    // lock table
    DbTableEntryEntity tableEntry = null;
    LockTemplate propsLock = new LockTemplate(tableId, ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      propsLock.acquire();

      try {
        tableEntry = DbTableEntry.getTableIdEntry(tableId, cc);
      } catch (ODKEntityNotFoundException e) {
        tableEntry = null;
      }

      // check if table exists
      if (tableEntry != null && tableEntry.getSchemaETag() != null) {

        TableEntry existing = converter.toTableEntry(tableEntry);

        // Table already exists -- see if schema matches

        DbTableDefinitionsEntity defn = DbTableDefinitions.getDefinition(tableId,
            existing.getSchemaETag(), cc);
        if (defn == null) {
          throw new TableAlreadyExistsException(
              String
                  .format(
                      "Table with tableId '%s' already exists with incompatible schema (null TableDefinition).",
                      tableId));
        }
        List<DbColumnDefinitionsEntity> cols = DbColumnDefinitions.query(tableId,
            existing.getSchemaETag(), cc);

        if ( cols.size() != columns.size() ) {
          throw new TableAlreadyExistsException(String.format(
              "Table with tableId '%s' already exists with a different number of columns.", tableId));
        }
        for (DbColumnDefinitionsEntity cde : cols) {
          boolean found = false;
          for (Column column : columns) {
            if (column.getElementKey().equals(cde.getElementKey())) {
              found = true;
              DbColumnDefinitionsEntity ce = creator.newColumnEntity(tableId,
                  existing.getSchemaETag(), column, cc);
              if (!ce.matchingColumnDefinition(cde)) {
                throw new TableAlreadyExistsException(String.format(
                    "Table with tableId '%s' already exists with incompatible schema.", tableId));
              }
            }
          }
          if (!found) {
            throw new TableAlreadyExistsException(
                String
                    .format(
                        "Table with tableId '%s' already exists with incompatible schema (missing Column).",
                        tableId));
          }
        }
        // Return the existing record -- schema is defined...
        return existing;
      }

      String pendingSchemaETag = CommonFieldsBase.newUri();

      if ( tableEntry == null ) {
        // create table. "entities" will store all of the things we will need to
        // persist into the datastore for the table to truly be created.
        Sequencer sequencer = new Sequencer(cc);
        String aprioriDataSequenceValue = sequencer.getNextSequenceValue();

        tableEntry = creator.newTableEntryEntity(tableId, pendingSchemaETag,
            aprioriDataSequenceValue, cc);
      } else {
        // clean up the state of the database
        deleteVersionedTable(tableEntry, false, cc);
        // and proceed to creat this record
        tableEntry.setPendingSchemaETag(pendingSchemaETag);
      }
      // write it...

      /**
       * write out the initial ACL
       */
      if ( tableEntry.getSchemaETag() == null ) {
        // remove anything we already set... (late clean-up)
        List<DbTableAclEntity> acls = DbTableAcl.queryTableIdAcls(tableId, cc);
        for (DbTableAclEntity acl : acls) {
          acl.delete(cc);
        }
        // write out the owner record
        DbTableAclEntity ownerAcl = creator.newTableAclEntity(tableId, new Scope(Scope.Type.USER,
            userPermissions.getOdkTablesUserId()), TableRole.OWNER, cc);
        ownerAcl.put(cc);
      }

      tableEntry.put(cc);
      // now build up the definition of this table

      /**
       * find a tableName that is not a collision...
       */
      String tableName = RUtil.convertIdentifier(tableId);
      int count = 1;
      while ( cc.getDatastore().hasRelation(RUtil.NAMESPACE, tableName, cc.getCurrentUser()) ||
              cc.getDatastore().hasRelation(RUtil.NAMESPACE, DbLogTable.getDbLogTableName(tableName), cc.getCurrentUser()) ) {
        tableName = RUtil.convertIdentifier(tableId + "_" + Integer.toString(count++));
      }
      /**
       * Write out the table definition
       */
      DbTableDefinitionsEntity tableDefinition = creator.newTableDefinitionEntity(tableId,
          pendingSchemaETag, tableName, cc);
      tableDefinition.put(cc);

      /**
       * Write out the column definitions
       */
      List<DbColumnDefinitionsEntity> colDefs = new ArrayList<DbColumnDefinitionsEntity>();
      for (Column column : columns) {
        colDefs.add(creator.newColumnEntity(tableId, pendingSchemaETag, column, cc));
      }
      for (DbColumnDefinitionsEntity e : colDefs) {
        e.put(cc);
      }

      /**
       * Instantiate the actual tables
       */
      @SuppressWarnings("unused")
      DbTable tableRelation = DbTable.getRelation(tableDefinition, colDefs, cc);
      @SuppressWarnings("unused")
      DbLogTable logTableRelation = DbLogTable.getRelation(tableDefinition, colDefs, cc);

      /**
       * Transition the schema to live
       */
      tableEntry.setSchemaETag(tableEntry.getPendingSchemaETag());
      tableEntry.setPendingSchemaETag(null);
      tableEntry.put(cc);

      return converter.toTableEntry(tableEntry);
    } finally {
      propsLock.release();
    }

  }

  /**
   * Routine shared by the TableManager and the PropertiesManager.
   * This cleans up the pending and stale state of the Schema and Properties of a table.
   *
   * @param tableEntry
   * @param deleteCurrent
   * @throws ODKDatastoreException
   * @throws ODKEntityPersistException
   * @throws ODKOverQuotaException
   */
  public static void deleteVersionedTable(DbTableEntryEntity tableEntry, boolean deleteCurrent, CallingContext cc) throws ODKDatastoreException, ODKEntityPersistException, ODKOverQuotaException {

    // delete stale schema
    if ( tableEntry.getStaleSchemaETag() != null ) {
      // get the column schema
      List<DbColumnDefinitionsEntity> colDefs = DbColumnDefinitions.query(tableEntry.getId(), tableEntry.getStaleSchemaETag(), cc);
      // get the table definition
      DbTableDefinitionsEntity definitionEntity = DbTableDefinitions.getDefinition(tableEntry.getId(), tableEntry.getStaleSchemaETag(), cc);
      if ( definitionEntity != null ) {
        // delete the data table
        DbTable tableRelation = DbTable.getRelation(definitionEntity, colDefs, cc);
        if ( tableRelation != null ) {
          tableRelation.dropRelation(cc);
        }
        // delete the data log table
        DbLogTable logTableRelation = DbLogTable.getRelation(definitionEntity, colDefs, cc);
        if ( logTableRelation != null ) {
          logTableRelation.dropRelation(cc);
        }
        // delete the user-defined columns
        for (DbColumnDefinitionsEntity colDef : colDefs) {
          colDef.delete(cc);
        }
        // delete the table definition
        definitionEntity.delete(cc);
      }
      tableEntry.setStaleSchemaETag(null);
    }

    // clean up the pending DataETag state
    // (not null IFF schemaETag is not null)
    if (tableEntry.getPendingDataETag() != null) {
      // we have a pendingDataETag that needs to be cleaned up
      String schemaETag = tableEntry.getSchemaETag();
      // get changes associated with this ETag
      List<DbColumnDefinitionsEntity> colDefs = DbColumnDefinitions.query(tableEntry.getId(), schemaETag, cc);
      // get the table definition
      DbTableDefinitionsEntity definitionEntity = DbTableDefinitions.getDefinition(tableEntry.getId(), schemaETag, cc);

      @SuppressWarnings("unused")
      DbTable tableRelation = DbTable.getRelation(definitionEntity, colDefs, cc);
      @SuppressWarnings("unused")
      DbLogTable logTableRelation = DbLogTable.getRelation(definitionEntity, colDefs, cc);

      // TODO: recover the DbTable values prior to the pendingDataETag from the log.
      // The steps are:
      // (1) get all log entries with the pendingDataETag.
      // (2) for all row ids in these, search for the prior status of the row.
      // (3) overwrite the current row id entry in the DbTable with that prior status.
      // (4) delete the log entry for that pendingDataETag of that row

      // Finally, update the pendingDataETag
      tableEntry.setPendingDataETag(null);
      tableEntry.put(cc);
    }

    // NOW: remove any pending schema or property changes
    tableEntry.setStaleSchemaETag(tableEntry.getPendingSchemaETag());
    tableEntry.setPendingSchemaETag(null);

    if ( tableEntry.getStaleSchemaETag() != null ) {
      // move the pending properties and schema to stale
      tableEntry.put(cc);
      // delete them (tail-recursive)
      deleteVersionedTable(tableEntry, deleteCurrent, cc);
    } else if ( deleteCurrent ) {
      // in addition to deleting the non-current cruft, we also need to delete the current data.
      tableEntry.setStaleSchemaETag(tableEntry.getSchemaETag());
      tableEntry.setSchemaETag(null);
      if ( tableEntry.getStaleSchemaETag() != null ) {
        // what had been the current schema, properties and row data now needs to be deleted
        tableEntry.put(cc);
        deleteVersionedTable(tableEntry, deleteCurrent, cc);
      } else {
        // we have completely deleted all schemas, properties and row data
        // delete the ACLs
        List<DbTableAclEntity> heldBack = new ArrayList<DbTableAclEntity>();
        List<DbTableAclEntity> acls = DbTableAcl.queryTableIdAcls(tableEntry.getId(), cc);
        for (DbTableAclEntity acl : acls) {
          if ( TableRole.valueOf(acl.getRole()).equals(TableRole.OWNER) ) {
            heldBack.add(acl);
          } else {
            acl.delete(cc);
          }
        }
        // delete the table entry itself
        tableEntry.delete(cc);

        // and clean up the entry...
        for (DbTableAclEntity acl : heldBack) {
          acl.delete(cc);
        }
      }
    }
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
   * @throws PermissionDeniedException
   */
  public void deleteTable(String tableId) throws ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, PermissionDeniedException {
    Validate.notNull(tableId);
    Validate.notEmpty(tableId);

    userPermissions.checkPermission(appId, tableId, TablePermission.DELETE_TABLE);

    DbTableEntryEntity tableEntry = DbTableEntry.getTableIdEntry(tableId, cc);

    LockTemplate propsLock = new LockTemplate(tableId, ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      propsLock.acquire();
      deleteVersionedTable(tableEntry, true, cc);
    } finally {
      propsLock.release();
    }
  }

}
