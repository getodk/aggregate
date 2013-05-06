package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.OdkTablesFileManifestEntry;
import org.opendatakit.aggregate.odktables.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableRole;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Relation;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Creates and updates new Entity objects for relations.
 * <p>
 * sudar.sam@gmail.com: This is deserving of its own class because
 * the entities (rows in the table) exist without any setters for their
 * columns (DataField objects). To insert a row in a particular class you
 * would therefore have to go look at the datafields contained in that entity
 * and set them by hand. This class handles that for you and aggregates them
 * all into one place.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */

public class EntityCreator {
	
  public static final int INITIAL_MODIFICATION_NUMBER = 1;

  /**
   * Create a new {@link DbTableEntry} entity.
   * 
   * @param tableId
   *          the table id. May be null to auto generate.
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   */
  public Entity newTableEntryEntity(String tableId, CallingContext cc) 
      throws ODKDatastoreException {
    Validate.notNull(cc);

    if (tableId == null)
      tableId = CommonFieldsBase.newUri();

    Entity entity = DbTableEntry.getRelation(cc).newEntity(tableId, cc);
    entity.set(DbTableEntry.MODIFICATION_NUMBER, 0);
    entity.set(DbTableEntry.PROPERTIES_MOD_NUM, 0);
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
  public Entity newColumnEntity(String tableId, Column column, 
      CallingContext cc) throws ODKDatastoreException {
    Validate.notEmpty(tableId);
    Validate.notNull(column);
    Validate.notNull(cc);

    Entity entity = DbColumnDefinitions.getRelation(cc).newEntity(cc);
    entity.set(DbColumnDefinitions.TABLE_ID, tableId);
    entity.set(DbColumnDefinitions.ELEMENT_KEY, column.getElementKey());
    entity.set(DbColumnDefinitions.ELEMENT_NAME, column.getElementName());
    entity.set(DbColumnDefinitions.ELEMENT_TYPE, column.getElementType());
    entity.set(DbColumnDefinitions.LIST_CHILD_ELEMENT_KEYS,
        column.getListChildElementKeys());
    entity.set(DbColumnDefinitions.IS_PERSISTED, column.getIsPersisted());
    entity.set(DbColumnDefinitions.JOINS, column.getJoins());

    return entity;
  }
  
  /**
   * Create a new {@link DbTableFileInfo} entity. This should be called
   * whenever you are adding a file to the 
   * @param tableId
   * 			the id of the table the file is associated with
   * @param type
   * 			the type of the file
   * @param key
   *        the key associated with this file
   * @param URI
   * 			the URI of the blobset of size one, of which the one entry is the file
   * @param isMedia
   *        whether or not this is a media file.
   * @return
   */
  public Entity newTableFileInfoEntity(String tableId, String type, String key, 
      String uri, boolean isMedia, CallingContext cc) 
      throws ODKDatastoreException {
	  // first do some preliminary checks
	  Validate.notEmpty(tableId);
	  Validate.notEmpty(uri);
	  
	  Entity entity = DbTableFileInfo.getRelation(cc).newEntity(cc);
	  entity.set(DbTableFileInfo.TABLE_ID, tableId);
	  entity.set(DbTableFileInfo.VALUE_TYPE, type);
	  entity.set(DbTableFileInfo.KEY, key);
	  entity.set(DbTableFileInfo.VALUE, uri);
	  entity.set(DbTableFileInfo.IS_MEDIA, isMedia);
	  
	  // now set the universal fields
	  entity.set(DbTable.CREATE_USER, cc.getCurrentUser().getEmail());
	  // TODO last update same as create? correct?
	  entity.set(DbTable.LAST_UPDATE_USER, cc.getCurrentUser().getEmail());
	  entity.set(DbTable.MODIFICATION_NUMBER, INITIAL_MODIFICATION_NUMBER);
	  entity.set(DbTable.DELETED, false);
	  entity.set(DbTable.ROW_VERSION, CommonFieldsBase.newUri());
	  // TODO is this the right kind of scope to be setting? one wonders...
	  entity.set(DbTable.FILTER_VALUE, (String) null);
	  entity.set(DbTable.FILTER_TYPE, (String) null);	  
	  
	  return entity;
  }

//  /**
//   * Create a new {@link DbTableProperties} entity.
//   * 
//   * @param tableId
//   *          the id of the table for the new table properties entity
//   * @param tableKey
//   *          a human readable name for the table
//   * @param metadata
//   *          application defined metadata to store with the table (may be null)
//   * @param cc
//   * @return the created entity, not yet persisted
//   * @throws ODKDatastoreException
//   */
//  public Entity newTablePropertiesEntity(String tableId, String tableKey, 
//      List<OdkTablesKeyValueStoreEntry> kvsEntries, CallingContext cc) 
//          throws ODKDatastoreException {
//    Validate.notEmpty(tableId);
//    Validate.notEmpty(tableKey);
//    // kvsEntries can be null. Will change to empty list.
//    Validate.notNull(cc);
//
//    Entity properties = DbTableProperties.getRelation(cc).newEntity(cc);
//    properties.set(DbTableProperties.TABLE_ID, tableId);
//    properties.set(DbTableProperties.TABLE_NAME, tableKey);
//    if (kvsEntries == null) {
//      kvsEntries = new ArrayList<OdkTablesKeyValueStoreEntry>();
//    }
//    properties.set(DbTableProperties.TABLE_METADATA, metadata);
//
//    return properties;
//  }
  
  /**
   * Return a new Entity representing a table definition. 
   * @param tableId cannot be null
   * @param tableKey cannot be null
   * @param dbTableName cannot be null
   * @param type cannot be null
   * @param tableIdAccessControls if null, not set.
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public Entity newTableDefinitionEntity(String tableId, String tableKey,
      String dbTableName, String type, String tableIdAccessControls,
      CallingContext cc) throws ODKDatastoreException {
    // Validate those parameters defined as non-null in the ODK Tables Schema
    // Google doc.
    Validate.notEmpty(tableId);
    Validate.notEmpty(tableKey);
    Validate.notEmpty(dbTableName);
    Validate.notEmpty(type);
    // tableIdAccessControls can be null.
    Validate.notNull(cc);
    Entity definition = DbTableDefinitions.getRelation(cc).newEntity(cc);
    definition.set(DbTableDefinitions.TABLE_ID, tableId);
    definition.set(DbTableDefinitions.TABLE_KEY, tableKey);
    definition.set(DbTableDefinitions.DB_TABLE_NAME, dbTableName);
    definition.set(DbTableDefinitions.TYPE, type);
    if (tableIdAccessControls != null) {
      definition.set(DbTableDefinitions.TABLE_ID_ACCESS_CONTROLS, 
          tableIdAccessControls);
    }
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
  public Entity newKeyValueStoreEntity(String tableId, String partition,
      String aspect, String key, String type, String value, 
      CallingContext cc) throws ODKDatastoreException {
    Validate.notEmpty(tableId);
    Validate.notEmpty(partition);
    Validate.notEmpty(aspect);
    Validate.notEmpty(key);
    Validate.notEmpty(type);
    Validate.notEmpty(value);
    Validate.notNull(cc);
    Entity entry = DbKeyValueStore.getRelation(cc).newEntity(cc);
    entry.set(DbKeyValueStore.TABLE_ID, tableId);
    entry.set(DbKeyValueStore.PARTITION, partition);
    entry.set(DbKeyValueStore.ASPECT, aspect);
    entry.set(DbKeyValueStore.KEY, key);
    entry.set(DbKeyValueStore.TYPE, type);
    entry.set(DbKeyValueStore.VALUE, value);
    return entry;
  }
  
  public Entity newKeyValueStoreEntity(OdkTablesKeyValueStoreEntry entry,
      CallingContext cc) throws ODKDatastoreException {
    return newKeyValueStoreEntity(entry.tableId, entry.partition, 
        entry.aspect, entry.key, entry.type, entry.value, cc);
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
  public Entity newTableAclEntity(String tableId, Scope scope, TableRole role, CallingContext cc)
      throws ODKDatastoreException {
    Validate.notEmpty(tableId);
    Validate.notNull(scope);
    // can't have an empty scope type in an acl entity
    Validate.notNull(scope.getType());
    Validate.notNull(role);
    Validate.notNull(cc);

    Entity tableAcl = DbTableAcl.getRelation(cc).newEntity(cc);
    tableAcl.set(DbTableAcl.TABLE_ID, tableId);
    tableAcl.set(DbTableAcl.SCOPE_TYPE, scope.getType().name());
    tableAcl.set(DbTableAcl.SCOPE_VALUE, scope.getValue());
    tableAcl.set(DbTableAcl.ROLE, role.name());

    return tableAcl;
  }

  /**
   * Create a new {@link DbTable} row entity.
   * 
   * @param table
   *          the {@link DbTable} relation.
   * @param rowId
   *          the id of the new row. May be null to auto generate.
   * @param modificationNumber
   *          the modification number for this row.
   * @param filter
   *          the scope of the filter. If null, the {@link Scope#EMPTY_SCOPE}
   *          will be applied.
   * @param values
   *          the values to set on the row.
   * @param columns
   *          the {@link DbColumnDefinitions} entities for the table
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   * @throws BadColumnNameException
   */
  public Entity newRowEntity(Relation table, String rowId, int modificationNumber, Scope filter,
      Map<String, String> values, List<Entity> columns, CallingContext cc)
      throws ODKDatastoreException, BadColumnNameException {
    Validate.notNull(table);
    Validate.isTrue(modificationNumber >= 0);
    if (filter == null)
      filter = Scope.EMPTY_SCOPE;
    Validate.noNullElements(values.keySet());
    Validate.noNullElements(columns);
    Validate.notNull(cc);

    if (rowId == null)
      rowId = CommonFieldsBase.newUri();
    else {
      boolean found = true;
      try {
        table.getEntity(rowId, cc);
      } catch ( ODKEntityNotFoundException e ) {
        found = false;
      }
      if ( found ) {
        throw new ODKEntityPersistException("Entity exists: " + rowId);
      }
    }

    Entity row = table.newEntity(rowId, cc);
    User user = cc.getCurrentUser();
    row.set(DbTable.CREATE_USER, user.getEmail());
    setRowFields(row, modificationNumber, user, filter, false, values, columns);
    return row;
  }

  /**
   * Create a collection of new {@link DbTable} entities
   * 
   * @param table
   *          the {@link DbTable} relation
   * @param rows
   *          the rows, see {@link Row#forInsert(String, String, Map)}
   * @param modificationNumber
   *          the modification number for the rows.
   * @param columns
   *          the {@link DbColumnDefinitions} entities for the table
   * @param cc
   * @return the created entities, not yet persisted
   * @throws ODKDatastoreException
   * @throws BadColumnNameException
   */
  public List<Entity> newRowEntities(Relation table, List<Row> rows, int modificationNumber,
      List<Entity> columns, CallingContext cc) throws ODKDatastoreException, BadColumnNameException {
    Validate.notNull(table);
    Validate.noNullElements(rows);
    Validate.isTrue(modificationNumber >= 0);
    Validate.noNullElements(columns);
    Validate.notNull(cc);

    List<Entity> entities = new ArrayList<Entity>();
    for (Row row : rows) {
      Entity entity = newRowEntity(table, row.getRowId(), modificationNumber, row.getFilterScope(),
          row.getValues(), columns, cc);
      entities.add(entity);
    }
    return entities;
  }

  /**
   * Update an existing {@link DbTable} entity.
   * 
   * @param table
   *          the {@link DbTable} relation
   * @param modificationNumber
   *          the modification number for this row
   * @param rowId
   *          the id of the row
   * @param currentEtag
   *          the current etag value
   * @param values
   *          the values to set
   * @param filter
   *          the filter to apply to this row. If null then the existing filter
   *          will not be changed.
   * @param columns
   *          the {@link DbColumnDefinitions} entities for the table
   * @param cc
   * @return the updated entity, not yet persisted
   * @throws ODKEntityNotFoundException
   *           if there is no entity with the given rowId
   * @throws EtagMismatchException
   *           if currentEtag does not match the etag of the row
   * @throws ODKDatastoreException
   * @throws BadColumnNameException
   */
  public Entity updateRowEntity(Relation table, int modificationNumber, String rowId,
      String currentEtag, Map<String, String> values, Scope filter, List<Entity> columns,
      CallingContext cc) throws ODKEntityNotFoundException, ODKDatastoreException,
      EtagMismatchException, BadColumnNameException {
    Validate.notNull(table);
    Validate.isTrue(modificationNumber >= 0);
    Validate.notEmpty(rowId);
    // if currentEtag is null we will catch it later
    Validate.noNullElements(values.keySet());
    // filter may be null
    Validate.noNullElements(columns);
    Validate.notNull(cc);

    Entity row = table.getEntity(rowId, cc);
    String rowEtag = row.getString(DbTable.ROW_VERSION);
    if (currentEtag == null || !currentEtag.equals(rowEtag)) {
      throw new EtagMismatchException(String.format("%s does not match %s for rowId %s",
          currentEtag, rowEtag, row.getId()));
    }

    setRowFields(row, modificationNumber, cc.getCurrentUser(), filter, false, values, columns);
    return row;
  }

  private void setRowFields(Entity row, int modificationNumber, User lastUpdatedUser,
      Scope filterScope, boolean deleted, Map<String, String> values, List<Entity> columns)
      throws BadColumnNameException {
    row.set(DbTable.ROW_VERSION, CommonFieldsBase.newUri());
    row.set(DbTable.MODIFICATION_NUMBER, modificationNumber);
    row.set(DbTable.LAST_UPDATE_USER, lastUpdatedUser.getEmail());

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

    for (Entry<String, String> entry : values.entrySet()) {
      String value = entry.getValue();
      String name = entry.getKey();
      Entity column = findColumn(name, columns);
      if (column == null)
        throw new BadColumnNameException("Bad column name " + name);
      row.setAsString(RUtil.convertIdentifier(column.getId()), value);
    }
  }

  private Entity findColumn(String elementKey, List<Entity> columns) {
    for (Entity entity : columns) {
      String colName = entity.getString(DbColumnDefinitions.ELEMENT_KEY);
      if (elementKey.equals(colName))
        return entity;
    }
    return null;
  }

  /**
   * Updates a collection of {@link DbTable} entities.
   * 
   * @param table
   *          the {@link DbTable} relation.
   * @param modificationNumber
   *          the modification number for the rows.
   * @param rows
   *          the rows to update, see {@link Row#forUpdate(String, String, Map)}
   * @param columns
   *          the {@link DbColumnDefinitions} entities for the table
   * @param cc
   * @return the updated entities, not yet persisted
   * @throws ODKEntityNotFoundException
   *           if one of the rows does not exist in the datastore
   * @throws EtagMismatchException
   *           if one of the row's etags does not match the etag for the row in
   *           the datastore
   * @throws ODKDatastoreException
   * @throws BadColumnNameException
   */
  public List<Entity> updateRowEntities(Relation table, int modificationNumber, List<Row> rows,
      List<Entity> columns, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException, EtagMismatchException, BadColumnNameException {
    Validate.notNull(table);
    Validate.isTrue(modificationNumber >= 0);
    Validate.noNullElements(rows);
    Validate.noNullElements(columns);
    Validate.notNull(cc);
    List<Entity> entities = new ArrayList<Entity>();
    for (Row row : rows) {
      entities.add(updateRowEntity(table, modificationNumber, row.getRowId(), row.getRowEtag(),
          row.getValues(), row.getFilterScope(), columns, cc));
    }
    return entities;
  }

  /**
   * Create a new {@link DbLogTable} row entity.
   * 
   * @param logTable
   *          the {@link DbLogTable} relation.
   * @param modificationNumber
   *          the modification number for the row.
   * @param row
   *          the row
   * @param columns
   *          the {@link DbColumnDefinitions} entities for the log table
   * @param cc
   * @return the created entity, not yet persisted
   * @throws ODKDatastoreException
   */
  public Entity newLogEntity(Relation logTable, int modificationNumber, Entity row,
      List<Entity> columns, CallingContext cc) throws ODKDatastoreException {
    Validate.notNull(logTable);
    Validate.isTrue(modificationNumber >= 0);
    Validate.notNull(row);
    Validate.noNullElements(columns);
    Validate.notNull(cc);

    Entity entity = logTable.newEntity(cc);
    entity.set(DbLogTable.ROW_ID, row.getId());
    entity.set(DbLogTable.ROW_VERSION, row.getString(DbTable.ROW_VERSION));
    entity.set(DbLogTable.MODIFICATION_NUMBER, modificationNumber);
    entity.set(DbLogTable.CREATE_USER, row.getString(DbTable.CREATE_USER));
    entity.set(DbLogTable.LAST_UPDATE_USER, row.getString(DbTable.LAST_UPDATE_USER));
    entity.set(DbLogTable.FILTER_TYPE, row.getString(DbTable.FILTER_TYPE));
    entity.set(DbLogTable.FILTER_VALUE, row.getString(DbTable.FILTER_VALUE));
    entity.set(DbLogTable.DELETED, row.getBoolean(DbTable.DELETED));

    for (Entity column : columns) {
      String idName = RUtil.convertIdentifier(column.getId());
      String value = row.getAsString(idName);
      entity.setAsString(idName, value);
    }
    return entity;
  }

  /**
   * Create a collection of new {@link DbLogTable} entities
   * 
   * @param logTable
   *          the {@link DbLogTable} relation
   * @param modificationNumber
   *          the modification number for the rows.
   * @param rows
   *          the rows
   * @param columns
   *          the {@link DbColumnDefinitions} entities for the table
   * @param cc
   * @return the created entities, not yet persisted
   * @throws ODKDatastoreException
   */
  public List<Entity> newLogEntities(Relation logTable, int modificationNumber, List<Entity> rows,
      List<Entity> columns, CallingContext cc) throws ODKDatastoreException {
    Validate.notNull(logTable);
    Validate.isTrue(modificationNumber >= 0);
    Validate.noNullElements(rows);
    Validate.noNullElements(columns);
    Validate.notNull(cc);
    List<Entity> entities = new ArrayList<Entity>();
    for (Entity row : rows) {
      Entity entity = newLogEntity(logTable, modificationNumber, row, columns, cc);
      entities.add(entity);
    }
    return entities;
  }
 
  
}