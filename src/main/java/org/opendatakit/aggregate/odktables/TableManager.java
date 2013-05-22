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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.TableRole;
import org.opendatakit.aggregate.odktables.entity.api.TableType;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbLogTable;
import org.opendatakit.aggregate.odktables.relation.DbTable;
import org.opendatakit.aggregate.odktables.relation.DbTableAcl;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Query;
import org.opendatakit.common.ermodel.simple.Relation;
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
    // get table entries
    Query query =
        DbTableEntry.getRelation(cc).query("TableManager.getTables", cc);
    List<Entity> entries = query.execute();

    return getTableEntries(entries);
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
    // Oct15--changing this just to point to get ALL the tables. This is to
    // avoid the permissions issue we have for now, as everything should be
    // getting tables through this class.
    // TODO fix this to again get things to point at scopes.
    /*
    // get table ids for entries that the given scopes can see
    List<Entity> aclEntities = DbTableAcl.queryNotEqual(TableRole.NONE, cc);
    List<String> tableIds = new ArrayList<String>();

    for (Entity aclEntity : aclEntities) {
      TableAcl acl = converter.toTableAcl(aclEntity);
      if (scopes.contains(acl.getScope())) {
        tableIds.add(aclEntity.getString(DbTableAcl.TABLE_ID));
      }
    }

    Query query = DbTableEntry.getRelation(cc).query("TableManager.getTables(List<Scope>)", cc);
    query.include(CommonFieldsBase.URI_COLUMN_NAME, tableIds);
    List<Entity> entries = query.execute();
    return getTableEntries(entries);

  */
    return getTables();
  }

  /**
   * Retrieve a list of {@link TableEntry} objects from a list of
   * {@link Entity} objects retrieved from {@link DbTableEntry}.
   * @param entries
   * @return
   * @throws ODKDatastoreException
   */
  private List<TableEntry> getTableEntries(List<Entity> entries)
      throws ODKDatastoreException {
    // get table names
    List<String> tableIds = new ArrayList<String>();
    for (Entity entry : entries) {
      tableIds.add(entry.getId());
    }
    // Will map ID to tableKey. To get the tableKey we need the TableDefinition
    Map<String, String> tableKeys = new HashMap<String, String>();
    Query definitionsQuery =
        DbTableDefinitions.getRelation(cc).query("TableManager.getTables", cc);
    definitionsQuery.include(DbTableDefinitions.TABLE_ID, tableIds);
    List<Entity> definitionEntities = definitionsQuery.execute();
    for (Entity properties : definitionEntities) {
      String tableId = properties.getString(DbTableDefinitions.TABLE_ID);
      String tableKey = properties.getString(DbTableDefinitions.TABLE_KEY);
      tableKeys.put(tableId, tableKey);
    }
    return converter.toTableEntries(entries, tableKeys);
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
    Query query =
        DbTableEntry.getRelation(cc).query("TableManager.getTable", cc);
    query.equal(CommonFieldsBase.URI_COLUMN_NAME, tableId);
    Entity entryEntity = query.get();

    // get table key
    Entity definition = DbTableDefinitions.getDefinition(tableId, cc);

    if (entryEntity != null && definition != null) {
      String tableKey = definition.getString(DbTableDefinitions.TABLE_KEY);
      return converter.toTableEntry(entryEntity, tableKey);
    } else {
      return null;
    }
  }

  /**
   * Retrieve the TableDefinition for the table with the given id.
   * @param tableId
   * @return
   * @throws ODKDatastoreException
   */
  public TableDefinition getTableDefinition(String tableId)
      throws ODKDatastoreException {
    Validate.notEmpty(tableId);
    Entity definitionEntity = DbTableDefinitions.getDefinition(tableId, cc);
    TableDefinition definition = converter.toTableDefinition(definitionEntity);
    List<Entity> columnEntities = DbColumnDefinitions.query(tableId, cc);
    List<Column> columns = converter.toColumns(columnEntities);
    definition.setColumns(columns);
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
    Entity entryEntity = DbTableEntry.getRelation(cc).getEntity(tableId, cc);
    // get tableKey
    Entity definitionEntity = DbTableDefinitions.getDefinition(tableId, cc);
    String tableKey = definitionEntity.getString(DbTableDefinitions.TABLE_KEY);
    return converter.toTableEntry(entryEntity, tableKey);
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
   * @param metadata
   *          application defined metadata to store with the table (may be null)
   * @return a table entry representing the newly created table
   * @throws TableAlreadyExistsException
   *           if a table with the given table id already exists
   * @throws ODKEntityPersistException
   * @throws ODKDatastoreException
   */
  public TableEntry createTable(String tableId, String tableKey,
      String dbTableName, TableType type, String tableIdAccessControls,
      List<Column> columns, List<OdkTablesKeyValueStoreEntry> kvsEntries)
          throws ODKEntityPersistException,
      ODKDatastoreException, TableAlreadyExistsException {
	Validate.notNull(tableId);
    Validate.notEmpty(tableId);
	Validate.notNull(tableKey);
    Validate.notEmpty(tableKey);
	Validate.notNull(dbTableName);
    Validate.notEmpty(dbTableName);
    Validate.notNull(type);
    // tableIdAccessControls can be null.
    Validate.noNullElements(columns);

    // the hope here is that it creates an empty table in the db after a single
    // odktable table has been created.
    DbTableFiles blobRelationSet = new DbTableFiles(cc);
    BlobEntitySet blobEntitySet = blobRelationSet.newBlobEntitySet(cc);
    blobRelationSet.putBlobEntitySet(blobEntitySet, cc);

    // check if table exists
    if (getTable(tableId) != null) {
      throw new TableAlreadyExistsException(String.format(
          "Table with tableId '%s' already exists.", tableId));
    }
    // TODO: do this for each of the necessary tableKey and dbTableName things.
    // Also need to figure out which of these actually need to be unique in the
    // db, if any.

    // TODO do appropriate checking for metadata issues. We need to worry about
    // dbName and  the displayName.

    // create table. "entities" will store all of the things we will need to
    // persist into the datastore for the table to truly be created.
    List<Entity> entities = new ArrayList<Entity>();

    Entity entry = creator.newTableEntryEntity(tableId, cc);
    entities.add(entry);

    Entity tableDefinition = creator.newTableDefinitionEntity(tableId,
        tableKey, dbTableName, type, tableIdAccessControls, cc);
    entities.add(tableDefinition);

    for (Column column : columns) {
      entities.add(creator.newColumnEntity(tableId, column, cc));
    }

    // SS: I think the DbTableProperties table should be phased out.
    //Entity properties = creator.newTablePropertiesEntity(tableId, tableName, metadata, cc);
    //entities.add(properties);

    if (kvsEntries == null) {
      kvsEntries = new ArrayList<OdkTablesKeyValueStoreEntry>();
    }
    for (OdkTablesKeyValueStoreEntry kvsEntry : kvsEntries) {
      entities.add(creator.newKeyValueStoreEntity(kvsEntry, cc));
    }

    Entity ownerAcl = creator.newTableAclEntity(tableId,
        new Scope(Scope.Type.USER, cc.getCurrentUser().getEmail()),
        TableRole.OWNER, cc);
    entities.add(ownerAcl);

    Relation.putEntities(entities, cc);

    return converter.toTableEntry(entry, tableKey);
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

    List<Entity> entities = new ArrayList<Entity>();

    Entity entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);
    entities.add(entry);

    List<Entity> columns = DbColumnDefinitions.query(tableId, cc);
    entities.addAll(columns);

    Entity definitionEntity = DbTableDefinitions.getDefinition(tableId, cc);
    entities.add(definitionEntity);

    List<Entity> acls = DbTableAcl.query(tableId, cc);
    entities.addAll(acls);

    Relation table = DbTable.getRelation(tableId, cc);
    Relation logTable = DbLogTable.getRelation(tableId, cc);

    LockTemplate dataLock =
        new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_DATA, cc);
    LockTemplate propsLock =
        new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_PROPERTIES, cc);
    try {
      dataLock.acquire();
      propsLock.acquire();
      Relation.deleteEntities(entities, cc);
      table.dropRelation(cc);
      logTable.dropRelation(cc);
    } finally {
      propsLock.release();
      dataLock.release();
    }
  }

}
