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
import org.opendatakit.aggregate.odktables.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.entity.TableProperties;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.relation.DbKeyValueStore;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
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
  private Entity entry;
  private Entity definitionEntity;
  private List<Entity> kvsEntities;
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
    this.definitionEntity = DbTableDefinitions.getDefinition(tableId, cc);
    this.kvsEntities = DbKeyValueStore.getKVSEntries(tableId, cc);
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
    kvsEntities = DbKeyValueStore.getKVSEntries(tableId, cc);
    definitionEntity = DbTableDefinitions.getDefinition(tableId, cc);
    String tableKey =
        definitionEntity.getAsString(DbTableDefinitions.TABLE_KEY);
    String propertiesEtag = entry.getString(DbTableEntry.PROPERTIES_ETAG);
    return converter.toTableProperties(kvsEntities, tableKey,
        propertiesEtag);
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
    LockTemplate lock = new LockTemplate(tableId,
        ODKTablesTaskLockType.UPDATE_PROPERTIES, cc);
    try {
      lock.acquire();

      // refresh entry
      entry = DbTableEntry.getRelation(cc).getEntity(tableId, cc);

      String propertiesEtag =
          entry.getString(DbTableEntry.PROPERTIES_ETAG);

      // check etags
      String currentEtag = tableProperties.getPropertiesEtag();
      if (currentEtag == null || !currentEtag.equals(propertiesEtag)) {
        throw new EtagMismatchException(String.format(
            "%s does not match %s for properties for table with tableId %s",
            currentEtag, propertiesEtag, tableId));
      }

      // increment modification number
      propertiesEtag = Long.toString(System.currentTimeMillis());
      entry.set(DbTableEntry.PROPERTIES_ETAG, propertiesEtag);

      // TODO: we should probably be diff'ing somehow, so we don't have to
      // change all of the entries. However, it's not obvious how to do that
      // without giving all of them their own etags. So, for now just wipe
      // all the kvs entries and replace them.

      // TODO: we should perhaps also be dealing with any changes to the
      // TableDefinition here. However, we're going to have to pass on this
      // for now and assume that once you've synched to the server, the
      // definition is static and immutable.
      log.info("setProperties: before kvs stuff in set properties");
      List<OdkTablesKeyValueStoreEntry> kvsEntries =
          tableProperties.getKeyValueStoreEntries();
      EntityCreator creator = new EntityCreator();
      List<Entity> kvsEntities = new ArrayList<Entity>();
      OdkTablesKeyValueStoreEntry holderEntry = null;
      try {
      for (OdkTablesKeyValueStoreEntry kvsEntry : kvsEntries) {
        holderEntry = kvsEntry;
        Entity kvsEntity = creator.newKeyValueStoreEntity(kvsEntry, cc);
        kvsEntities.add(kvsEntity);
      }
      } catch (Exception e) {
        e.printStackTrace();
        // what is the deal.
        log.error("setProperties (" + holderEntry.partition + ", " +
                  holderEntry.aspect + ", " + holderEntry.key + ") failed: " + e.toString());
        throw new ODKDatastoreException("Something went wrong in creating " +
        		"key value " +
        		"store entries: " + e.toString());
      }
      // Wipe the existing kvsEntries.
      // Caution! See javadoc of {@link clearAllEntries} and note that this is
      // not done transactionally, so you could end up in a rough spot if your
      // pursuant call to add all the new entities fails.
      log.info("setProperties Made it past add all to lists");
      DbKeyValueStore.clearAllEntries(tableId, cc);
      log.info("setProperties made it past clear");
      // Now put all the entries.
      for (Entity kvsEntity : kvsEntities) {
        kvsEntity.put(cc);
      }

      // set properties entity
//      properties.set(DbTableProperties.TABLE_NAME, tableProperties.getTableName());
//      properties.set(DbTableProperties.TABLE_METADATA, tableProperties.getMetadata());

      // update db
      entry.put(cc);
//      properties.put(cc);
    } finally {
      lock.release();
    }
    return converter.toTableProperties(kvsEntities,
        definitionEntity.getString(DbTableDefinitions.TABLE_KEY),
        entry.getString(DbTableEntry.PROPERTIES_ETAG));
  }
}
