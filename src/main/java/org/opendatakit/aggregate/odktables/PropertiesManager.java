package org.opendatakit.aggregate.odktables;

import org.apache.commons.lang3.Validate;
import org.opendatakit.aggregate.odktables.entity.TableProperties;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableProperties;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
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
  private EntityConverter converter;

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
    this.converter = new EntityConverter();
  }

  /**
   * @return the tableId that this PropertiesManager was constructed with.
   */
  public String getTableId() {
    return tableId;
  }

  /**
   * Retrieve the current table properties.
   * 
   * @return the current table properties.
   * @throws ODKDatastoreException
   */
  public TableProperties getProperties() throws ODKDatastoreException {
    // refresh entities
    entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);
    properties = DbTableProperties.getProperties(tableId, cc);

    return converter.toTableProperties(properties,
        entry.getAsString(DbTableEntry.PROPERTIES_MOD_NUM));
  }

  /**
   * Sets the table properties.
   * 
   * @param tableProperties
   *          the table properties to set
   * @return
   * @throws EtagMismatchException
   *           if the given tableProperties' etag does not match the current
   *           properties etag.
   * @throws ODKTaskLockException
   * @throws ODKDatastoreException
   */
  public TableProperties setProperties(TableProperties tableProperties)
      throws ODKTaskLockException, ODKDatastoreException, EtagMismatchException {

    // lock table
    LockTemplate lock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_PROPERTIES, cc);
    try {
      lock.acquire();

      // refresh entry
      entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);

      int modificationNumber = entry.getInteger(DbTableEntry.PROPERTIES_MOD_NUM);

      // check etags
      String currentEtag = tableProperties.getPropertiesEtag();
      String propertiesEtag = String.valueOf(modificationNumber);
      if (currentEtag == null || !currentEtag.equals(propertiesEtag)) {
        throw new EtagMismatchException(String.format(
            "%s does not match %s for properties for table with tableId %s", currentEtag,
            propertiesEtag, tableId));
      }

      // increment modification number
      modificationNumber++;
      entry.set(DbTableEntry.PROPERTIES_MOD_NUM, modificationNumber);

      // set properties entity
      properties.set(DbTableProperties.TABLE_NAME, tableProperties.getTableName());
      properties.set(DbTableProperties.TABLE_METADATA, tableProperties.getMetadata());

      // update db
      entry.put(cc);
      properties.put(cc);
    } finally {
      lock.release();
    }
    return converter.toTableProperties(properties,
        entry.getAsString(DbTableEntry.PROPERTIES_MOD_NUM));
  }
}
