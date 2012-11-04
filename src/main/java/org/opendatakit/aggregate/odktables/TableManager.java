package org.opendatakit.aggregate.odktables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableAcl;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.TableRole;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.relation.DbColumn;
import org.opendatakit.aggregate.odktables.relation.DbLogTable;
import org.opendatakit.aggregate.odktables.relation.DbTable;
import org.opendatakit.aggregate.odktables.relation.DbTableAcl;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.relation.DbTableProperties;
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
    Query query = DbTableEntry.getRelation(cc).query("TableManager.getTables", cc);
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

  private List<TableEntry> getTableEntries(List<Entity> entries) throws ODKDatastoreException {
    // get table names
    List<String> tableIds = new ArrayList<String>();
    for (Entity entry : entries) {
      tableIds.add(entry.getId());
    }

    Map<String, String> tableNames = new HashMap<String, String>();
    Query propsQuery = DbTableProperties.getRelation(cc).query("TableManager.getTables", cc);
    propsQuery.include(DbTableProperties.TABLE_ID, tableIds);
    List<Entity> propertiesEntities = propsQuery.execute();
    for (Entity properties : propertiesEntities) {
      String tableId = properties.getString(DbTableProperties.TABLE_ID);
      String tableName = properties.getString(DbTableProperties.TABLE_NAME);
      tableNames.put(tableId, tableName);
    }

    return converter.toTableEntries(entries, tableNames);
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
    Validate.notEmpty(tableId);

    // get table entry
    Query query = DbTableEntry.getRelation(cc).query("TableManager.getTable", cc);
    query.equal(CommonFieldsBase.URI_COLUMN_NAME, tableId);
    Entity table = query.get();

    // get table name
    Entity properties = DbTableProperties.getProperties(tableId, cc);

    if (table != null && properties != null) {
      String tableName = properties.getString(DbTableProperties.TABLE_NAME);
      return converter.toTableEntry(table, tableName);
    } else {
      return null;
    }
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
    Validate.notEmpty(tableId);

    // get table entry
    Entity entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);

    // get table name
    Entity properties = DbTableProperties.getProperties(tableId, cc);
    String tableName = properties.getString(DbTableProperties.TABLE_NAME);

    return converter.toTableEntry(entry, tableName);
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
  public TableEntry createTable(String tableId, String tableName, 
      List<Column> columns, String metadata) throws ODKEntityPersistException, 
      ODKDatastoreException, TableAlreadyExistsException {
    Validate.notEmpty(tableId);
    Validate.notEmpty(tableName);
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
    
    // TODO do appropriate checking for metadata issues. We need to worry about
    // dbName and  the displayName.

    // create table
    List<Entity> entities = new ArrayList<Entity>();

    Entity entry = creator.newTableEntryEntity(tableId, cc);
    entities.add(entry);

    for (Column column : columns) {
      entities.add(creator.newColumnEntity(tableId, column, cc));
    }
    
    Entity properties = creator.newTablePropertiesEntity(tableId, tableName, metadata, cc);
    entities.add(properties);

    Entity ownerAcl = creator.newTableAclEntity(tableId, new Scope(Scope.Type.USER, cc
        .getCurrentUser().getEmail()), TableRole.OWNER, cc);
    entities.add(ownerAcl);

    Relation.putEntities(entities, cc);

    return converter.toTableEntry(entry, tableName);
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
    Validate.notEmpty(tableId);

    List<Entity> entities = new ArrayList<Entity>();

    Entity entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);
    entities.add(entry);

    List<Entity> columns = DbColumn.query(tableId, cc);
    entities.addAll(columns);

    Entity properties = DbTableProperties.getProperties(tableId, cc);
    entities.add(properties);

    List<Entity> acls = DbTableAcl.query(tableId, cc);
    entities.addAll(acls);

    Relation table = DbTable.getRelation(tableId, cc);
    Relation logTable = DbLogTable.getRelation(tableId, cc);

    LockTemplate dataLock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_DATA, cc);
    LockTemplate propsLock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_PROPERTIES, cc);
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
