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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.relation.DbKeyValueStore;
import org.opendatakit.aggregate.odktables.relation.DbKeyValueStore.DbKeyValueStoreEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions.DbTableDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry.DbTableEntryEntity;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableProperties;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

/**
 * Manages getting and setting table name and metadata.
 *
 * @author the.dylan.price@gmail.com
 * @author sudar.sam@gmail.com
 *
 */
public class PropertiesManager {
  private static final Log log = LogFactory.getLog(PropertiesManager.class);

  private CallingContext cc;
  private String tableId;
  private DbTableEntryEntity entry;
  private DbTableDefinitionsEntity definitionEntity;
  private List<DbKeyValueStoreEntity> kvsEntities;
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
    this.entry = DbTableEntry.getTableIdEntry(tableId, cc);
    String schemaETag = entry.getSchemaETag();
    this.definitionEntity = DbTableDefinitions.getDefinition(tableId, schemaETag, cc);
    String propertiesETag = entry.getPropertiesETag();
    this.kvsEntities = DbKeyValueStore.getKVSEntries(tableId, propertiesETag, cc);
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
    entry = DbTableEntry.getTableIdEntry(tableId, cc);
    String schemaETag = entry.getSchemaETag();
    definitionEntity = DbTableDefinitions.getDefinition(tableId, schemaETag, cc);

    String propertiesETag = entry.getPropertiesETag();
    kvsEntities = DbKeyValueStore.getKVSEntries(tableId, propertiesETag, cc);
    return converter.toTableProperties(kvsEntities, tableId, propertiesETag);
  }

  /**
   * Sets the table properties.
   *
   * @param tableProperties
   *          the table properties to set
   * @return
   * @throws ETagMismatchException
   *           if the given tableProperties' etag does not match the current
   *           properties etag.
   * @throws ODKTaskLockException
   * @throws ODKDatastoreException
   */
  public TableProperties setProperties(TableProperties tableProperties)
      throws ODKTaskLockException, ODKDatastoreException, ETagMismatchException {

    // create new eTag
    String propertiesETag = CommonFieldsBase.newUri();

    // lock table
    LockTemplate lock = new LockTemplate(tableId, ODKTablesTaskLockType.UPDATE_PROPERTIES, cc);
    try {
      lock.acquire();

      // refresh entry
      entry = DbTableEntry.getTableIdEntry(tableId, cc);

      String oldPropertiesETag = entry.getPropertiesETag();

      // check etags
      String currentETag = tableProperties.getPropertiesETag();
      if (currentETag == null || !currentETag.equals(oldPropertiesETag)) {
        throw new ETagMismatchException(String.format(
            "%s does not match %s for properties for table with tableId %s", currentETag,
            oldPropertiesETag, tableId));
      }

      EntityCreator creator = new EntityCreator();

      List<DbKeyValueStoreEntity> newKvsEntities = new ArrayList<DbKeyValueStoreEntity>();
      try {
        // TODO: we should probably be diff'ing somehow, so we don't have to
        // change all of the entries. However, it's not obvious how to do that
        // without giving all of them their own etags. So, for now just wipe
        // all the kvs entries and replace them.

        // TODO: we should perhaps also be dealing with any changes to the
        // TableDefinition here. However, we're going to have to pass on this
        // for now and assume that once you've synched to the server, the
        // definition is static and immutable.
        log.info("setProperties: before kvs stuff in set properties");
        List<OdkTablesKeyValueStoreEntry> kvsEntries = tableProperties.getKeyValueStoreEntries();
        OdkTablesKeyValueStoreEntry holderEntry = null;
        try {
          for (OdkTablesKeyValueStoreEntry kvsEntry : kvsEntries) {
            holderEntry = kvsEntry;
            DbKeyValueStoreEntity kvsEntity = creator.newKeyValueStoreEntity(kvsEntry,
                propertiesETag, cc);
            newKvsEntities.add(kvsEntity);
          }
        } catch (Exception e) {
          e.printStackTrace();
          // what is the deal.
          log.error("setProperties (" + holderEntry.partition + ", " + holderEntry.aspect + ", "
              + holderEntry.key + ") failed: " + e.toString());
          throw new ODKDatastoreException("Something went wrong in creating " + "key value "
              + "store entries: " + e.toString());
        }
        // Wipe the existing kvsEntries.
        // Caution! See javadoc of {@link clearAllEntries} and note that this is
        // not done transactionally, so you could end up in a rough spot if your
        // pursuant call to add all the new entities fails.
        log.info("setProperties Made it past add all to lists");
        // Now put all the entries.
        for (DbKeyValueStoreEntity e : newKvsEntities) {
          e.put(cc);
        }
        log.info("setProperties made it past persist of kvsEntities");
        // set properties entity

        // update tableEntry with new properties eTag
        entry.setPropertiesETag(propertiesETag);
        // write the entry out...
        entry.put(cc);

        kvsEntities = newKvsEntities;
        log.info("setProperties made it past update to propertiesETag");

        // everything was successfully committed -- we can now delete the old
        // values...
        DbKeyValueStore.clearAllEntries(tableId, oldPropertiesETag, cc);
        log.info("setProperties made it past clear");

      } catch (ODKEntityPersistException e) {
        // on failure, restore from the database (likely fails...)...
        getProperties();
        throw e;
      } catch (ODKOverQuotaException e) {
        // on failure, restore from the database (likely fails...)...
        getProperties();
        throw e;
      }
    } finally {
      lock.release();
    }

    return converter.toTableProperties(kvsEntities, tableId, propertiesETag);
  }
}
