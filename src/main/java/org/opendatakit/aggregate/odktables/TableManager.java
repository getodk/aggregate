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
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions.DbColumnDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbKeyValueStore;
import org.opendatakit.aggregate.odktables.relation.DbKeyValueStore.DbKeyValueStoreEntity;
import org.opendatakit.aggregate.odktables.relation.DbLogTable;
import org.opendatakit.aggregate.odktables.relation.DbTable;
import org.opendatakit.aggregate.odktables.relation.DbTableAcl;
import org.opendatakit.aggregate.odktables.relation.DbTableAcl.DbTableAclEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions.DbTableDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry.DbTableEntryEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.aggregate.odktables.rest.KeyValueStoreConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.common.ermodel.BlobEntitySet;
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
    return converter.toTableEntries(DbTableEntry.query(cc));
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
    return getTables();
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

    if (entryEntity != null) {
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
   */
  public TableDefinition getTableDefinition(String tableId) throws ODKDatastoreException {
    Validate.notEmpty(tableId);
    TableEntry entry = getTable(tableId);
    if (entry == null) {
      return null;
    }
    String schemaEtag = entry.getSchemaEtag();
    DbTableDefinitionsEntity definitionEntity = DbTableDefinitions.getDefinition(tableId,
        schemaEtag, cc);
    TableDefinition definition = converter.toTableDefinition(entry, definitionEntity);
    List<DbColumnDefinitionsEntity> columnEntities = DbColumnDefinitions.query(tableId,
        schemaEtag, cc);
    List<Column> columns = converter.toColumns(columnEntities);
    definition.setColumns(columns);

    String propertiesEtag = entry.getPropertiesEtag();
    List<DbKeyValueStoreEntity> kvsEntries = DbKeyValueStore.getKVSEntries(tableId, propertiesEtag,
        cc);
    String displayString = null;
    for ( DbKeyValueStoreEntity kvs : kvsEntries ) {
      if ( kvs.getKey().equals(KeyValueStoreConstants.TABLE_DISPLAY_NAME) &&
           kvs.getPartition().equals(KeyValueStoreConstants.PARTITION_TABLE) &&
           kvs.getAspect().equals(KeyValueStoreConstants.ASPECT_DEFAULT) ) {
        displayString = kvs.getValue();
      }
    }
    definition.setDisplayName(displayString);
    return definition;
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
    Validate.notNull(tableId);
    Validate.notEmpty(tableId);
    // get table entry entity
    DbTableEntryEntity entryEntity = DbTableEntry.getTableIdEntry(tableId, cc);
    return converter.toTableEntry(entryEntity);
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
   * @param kvs
   *          key-value store properties of this table (may be empty)
   * @return a table entry representing the newly created table
   * @throws TableAlreadyExistsException
   *           if a table with the given table id already exists
   * @throws ODKEntityPersistException
   * @throws ODKDatastoreException
   */
  public TableEntry createTable(String tableId,
      List<Column> columns, List<OdkTablesKeyValueStoreEntry> kvsEntries) throws ODKEntityPersistException,
      ODKDatastoreException, TableAlreadyExistsException {
    Validate.notNull(tableId);
    Validate.notEmpty(tableId);
    Validate.noNullElements(columns);

    // the hope here is that it creates an empty table in the db after a single
    // odktable table has been created.
    DbTableFiles blobRelationSet = new DbTableFiles(cc);
    BlobEntitySet blobEntitySet = blobRelationSet.newBlobEntitySet(cc);
    blobRelationSet.putBlobEntitySet(blobEntitySet, cc);

    TableEntry existing = getTable(tableId);
    // check if table exists
    if (existing != null) {
      DbTableDefinitionsEntity defn = DbTableDefinitions.getDefinition(tableId, existing.getSchemaEtag(), cc);
      if ( defn == null ) {
        throw new TableAlreadyExistsException(String.format(
            "Table with tableId '%s' already exists with incompatible schema (null TableDefinition).", tableId));
      }
      List<DbColumnDefinitionsEntity> cols = DbColumnDefinitions.query(tableId, existing.getSchemaEtag(), cc);

      for (DbColumnDefinitionsEntity cde : cols ) {
        boolean found = false;
        for (Column column : columns) {
          if ( column.getElementKey().equals(cde.getElementKey()) ) {
            found = true;
            DbColumnDefinitionsEntity ce = creator.newColumnEntity(tableId, existing.getSchemaEtag(), column, cc);
            if ( !ce.matchingColumnDefinition(cde) ) {
              throw new TableAlreadyExistsException(String.format(
                  "Table with tableId '%s' already exists with incompatible schema.", tableId));
            }
          }
        }
        if ( !found ) {
          throw new TableAlreadyExistsException(String.format(
              "Table with tableId '%s' already exists with incompatible schema (missing Column).", tableId));
        }
      }
      // Don't care about displayName...
      return existing;
    }

    // TODO: do this for each of the necessary tableKey and dbTableName things.
    // Also need to figure out which of these actually need to be unique in the
    // db, if any.

    // TODO do appropriate checking for metadata issues. We need to worry about
    // dbName and the displayName.

    // create table. "entities" will store all of the things we will need to
    // persist into the datastore for the table to truly be created.
    Sequencer sequencer = new Sequencer(cc);

    String schemaEtag = CommonFieldsBase.newUri();
    String aprioriDataSequenceValue = sequencer.getNextSequenceValue();
    DbTableEntryEntity tableEntry = creator.newTableEntryEntity(tableId, schemaEtag,
        aprioriDataSequenceValue, cc);

    /**
     * TODO: guess that tableId works as dbTableName...
     */
    DbTableDefinitionsEntity tableDefinition = creator.newTableDefinitionEntity(tableId,
        schemaEtag, tableId, cc);

    List<DbColumnDefinitionsEntity> colDefs = new ArrayList<DbColumnDefinitionsEntity>();
    for (Column column : columns) {
      colDefs.add(creator.newColumnEntity(tableId, schemaEtag, column, cc));
    }

    // write the table and column definitions
    tableDefinition.put(cc);
    for (DbColumnDefinitionsEntity e : colDefs) {
      e.put(cc);
    }
    // write the initial ACL
    DbTableAclEntity ownerAcl = creator.newTableAclEntity(tableId, new Scope(Scope.Type.USER, cc
        .getCurrentUser().getEmail()), TableRole.OWNER, cc);
    ownerAcl.put(cc);

    // Do NOT write the entry yet -- we need to have a valid displayName first!!!
    // tableEntry.put(cc);

    // Now update the properties...
    String propertiesEtag = CommonFieldsBase.newUri();
    if (kvsEntries == null) {
      kvsEntries = new ArrayList<OdkTablesKeyValueStoreEntry>();
    }

    List<DbKeyValueStoreEntity> kvs = new ArrayList<DbKeyValueStoreEntity>();
    for (OdkTablesKeyValueStoreEntry kvsEntry : kvsEntries) {
      kvs.add(creator.newKeyValueStoreEntity(kvsEntry, propertiesEtag, cc));
    }

    for (DbKeyValueStoreEntity e : kvs) {
      e.put(cc);
    }

    // write the updated table entry record so everything is discover-able
    tableEntry.setPropertiesETag(propertiesEtag);
    tableEntry.put(cc);

    return converter.toTableEntry(tableEntry);
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
    Validate.notNull(tableId);
    Validate.notEmpty(tableId);

    DbTableEntryEntity tableEntry = DbTableEntry.getTableIdEntry(tableId, cc);

    String schemaEtag = tableEntry.getSchemaETag();

    List<DbColumnDefinitionsEntity> columns = DbColumnDefinitions
        .query(tableId, schemaEtag, cc);

    DbTableDefinitionsEntity definitionEntity = DbTableDefinitions.getDefinition(tableId,
        schemaEtag, cc);

    List<DbTableAclEntity> acls = DbTableAcl.queryTableIdAcls(tableId, cc);

    String propertiesEtag = tableEntry.getPropertiesETag();
    List<DbKeyValueStoreEntity> kvsEntries = DbKeyValueStore.getKVSEntries(tableId, propertiesEtag,
        cc);

    DbTable table = DbTable.getRelation(tableId, schemaEtag, cc);
    DbLogTable logTable = DbLogTable.getRelation(tableId, schemaEtag, cc);

    LockTemplate dataLock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_DATA, cc);
    LockTemplate propsLock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_PROPERTIES, cc);
    try {
      dataLock.acquire();
      propsLock.acquire();
      // TODO: this is problematic -- should ensure only owner ACL is retained
      // and all others are removed, then remove the table entry???
      for (DbTableAclEntity acl : acls) {
        acl.delete(cc);
      }
      // removal makes table id no longer visible.
      tableEntry.delete(cc);
      // Everything else is keyed off of table id
      for (DbColumnDefinitionsEntity colDef : columns) {
        colDef.delete(cc);
      }
      for (DbKeyValueStoreEntity kvs : kvsEntries) {
        kvs.delete(cc);
      }
      definitionEntity.delete(cc);
      table.dropRelation(cc);
      logTable.dropRelation(cc);
    } finally {
      propsLock.release();
      dataLock.release();
    }
  }

}
