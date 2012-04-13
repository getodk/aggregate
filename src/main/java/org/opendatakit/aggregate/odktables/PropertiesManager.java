package org.opendatakit.aggregate.odktables;

import org.apache.commons.lang.Validate;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableProperties;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

/**
 * Manages getting and setting table name and metadata.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class PropertiesManager {

  private CallingContext cc;
  private String tableId;
  private Entity entry;
  private Entity properties;

  /**
   * Construct a new PropertiesManager.
   * 
   * @param tableId
   *          the unique identifier of the table
   * @param cc
   *          the calling context
   * @throws ODKEntityNotFoundException
   *           if no table with the given id exists
   * @throws ODKDatastoreException
   *           if there is an internal error in the datastore
   */
  public PropertiesManager(String tableId, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException {
    Validate.notEmpty(tableId);
    Validate.notNull(cc);
    this.cc = cc;
    this.tableId = tableId;
    this.entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);
    this.properties = DbTableProperties.getProperties(tableId, cc);
  }

  /**
   * @return the current name of the table
   */
  public String getTableName() {
    return properties.getString(DbTableProperties.TABLE_NAME);
  }

  /**
   * @return the current metadata stored with the table
   */
  public String getTableMetadata() {
    return properties.getString(DbTableProperties.TABLE_METADATA);
  }

  /**
   * @param tableName
   *          the new name of the table
   * @throws ODKTaskLockException
   * @throws ODKDatastoreException
   */
  public void setTableName(String tableName) throws ODKTaskLockException, ODKDatastoreException {
    Validate.notEmpty(tableName);
    setPropertiesField(DbTableProperties.TABLE_NAME, tableName);
  }

  /**
   * @param metadata
   *          the new metadata for the table
   * @throws ODKTaskLockException
   * @throws ODKDatastoreException
   */
  public void setTableMetadata(String metadata) throws ODKTaskLockException, ODKDatastoreException {
    setPropertiesField(DbTableProperties.TABLE_METADATA, metadata);
  }

  private void setPropertiesField(String fieldName, String value) throws ODKTaskLockException,
      ODKDatastoreException {

    // lock table
    LockTemplate lock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_PROPERTIES, cc);
    try {
      lock.acquire();

      // refresh entry
      entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);

      // increment modification number
      int modificationNumber = entry.getInteger(DbTableEntry.PROPERTIES_MOD_NUM);
      modificationNumber++;
      entry.set(DbTableEntry.PROPERTIES_MOD_NUM, modificationNumber);

      // set properties entity
      properties.set(fieldName, value);

      // update db
      entry.put(cc);
      properties.put(cc);
    } finally {
      lock.release();
    }
  }
}
