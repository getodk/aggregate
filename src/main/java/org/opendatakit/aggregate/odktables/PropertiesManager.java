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
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbKeyValueStore;
import org.opendatakit.aggregate.odktables.relation.DbKeyValueStore.DbKeyValueStoreEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry;
import org.opendatakit.aggregate.odktables.relation.DbTableEntry.DbTableEntryEntity;
import org.opendatakit.aggregate.odktables.relation.EntityConverter;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableProperties;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.CommonFieldsBase;
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
  private TablesUserPermissions userPermissions;
  private String appId;
  private String tableId;
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
  public PropertiesManager(String appId, String tableId, TablesUserPermissions userPermissions, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException {
    Validate.notEmpty(appId);
    Validate.notEmpty(tableId);
    Validate.notNull(cc);
    this.cc = cc;
    this.userPermissions = userPermissions;
    this.appId = appId;
    this.tableId = tableId;
    this.converter = new EntityConverter();
  }

  /**
   * @return the appId that this PropertiesManager was constructed with.
   */
  public String getAppId() {
    return appId;
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
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   * @throws ETagMismatchException
   */
  public TableProperties getProperties() throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException, ETagMismatchException {
    userPermissions.checkPermission(appId, tableId, TablePermission.READ_PROPERTIES);

    String schemaETag = null;
    String propertiesETag = null;
    List<DbKeyValueStoreEntity> kvsEntities = new ArrayList<DbKeyValueStoreEntity>();
    LockTemplate lock = new LockTemplate(tableId, ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      lock.acquire();
      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);
      schemaETag = entry.getSchemaETag();
      if ( schemaETag != null ) {
        propertiesETag = entry.getPropertiesETag();
        kvsEntities = DbKeyValueStore.getKVSEntries(tableId, propertiesETag, cc);
      }
    } finally {
      lock.release();
    }
    if ( schemaETag == null ) {
      throw new ETagMismatchException(String.format(
          "Unable to get table properties -- schema not defined for table with tableId %s", tableId));
    }
    return converter.toTableProperties(kvsEntities, tableId, schemaETag, propertiesETag);
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
   * @throws PermissionDeniedException
   */
  public TableProperties setProperties(TableProperties tableProperties)
      throws ODKTaskLockException, ODKDatastoreException, ETagMismatchException, PermissionDeniedException {

    userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_PROPERTIES);
    String schemaETag = null;
    // create new eTag
    String propertiesETag = CommonFieldsBase.newUri();

    // lock table
    LockTemplate lock = new LockTemplate(tableId, ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, cc);
    try {
      lock.acquire();

      // refresh entry
      DbTableEntryEntity entry = DbTableEntry.getTableIdEntry(tableId, cc);

      schemaETag = entry.getSchemaETag();
      if ( schemaETag == null ) {
        throw new ETagMismatchException(String.format(
            "Unable to set table properties -- schema not defined for table with tableId %s", tableId));
      }
      if ( !schemaETag.equals(tableProperties.getSchemaETag()) ) {
        throw new ETagMismatchException(String.format(
            "Unable to set table properties -- schemaETag does not match for table with tableId %s", tableId));
      }

      String oldPropertiesETag = entry.getPropertiesETag();

      // check etags
      String currentETag = tableProperties.getPropertiesETag();
      if ((currentETag == null && oldPropertiesETag != null) ||
          (currentETag != null && !currentETag.equals(oldPropertiesETag))) {
        throw new ETagMismatchException(String.format(
            "%s does not match %s for properties for table with tableId %s", currentETag,
            oldPropertiesETag, tableId));
      }

      // clean up any pending or stale state
      TableManager.deleteVersionedTable(entry, false, cc);

      // write it as a pending eTag
      entry.setPendingPropertiesETag(propertiesETag);
      entry.put(cc);

      EntityCreator creator = new EntityCreator();

      /**
       * Enter the new properties in under the new propertiesETag.
       * The old properties remain under the old propertiesETag
       */
      log.info("setProperties: before kvs stuff in set properties");
      List<OdkTablesKeyValueStoreEntry> kvsEntries = tableProperties.getKeyValueStoreEntries();
      OdkTablesKeyValueStoreEntry holderEntry = null;
      List<DbKeyValueStoreEntity> kvsEntities = new ArrayList<DbKeyValueStoreEntity>();
      try {
        for (OdkTablesKeyValueStoreEntry kvsEntry : kvsEntries) {
          holderEntry = kvsEntry;
          DbKeyValueStoreEntity kvsEntity = creator.newKeyValueStoreEntity(kvsEntry,
              propertiesETag, cc);
          kvsEntity.put(cc);
          kvsEntities.add(kvsEntity);
        }
      } catch (Exception e) {
        e.printStackTrace();
        // what is the deal.
        log.error("setProperties (" + holderEntry.partition + ", " + holderEntry.aspect + ", "
            + holderEntry.key + ") failed: " + e.toString());
        throw new ODKDatastoreException("Something went wrong in creating " + "key value "
            + "store entries: " + e.toString(), e);
      }

      // Wipe the existing kvsEntries.
      // Caution! See javadoc of {@link clearAllEntries} and note that this is
      // not done transactionally, so you could end up in a rough spot if your
      // pursuant call to add all the new entities fails.
      log.info("setProperties Made it past persist of pending kvsEntries");

      entry.setStalePropertiesETag(entry.getPropertiesETag());
      entry.setPropertiesETag(entry.getPendingPropertiesETag());
      entry.setPendingPropertiesETag(null);
      entry.put(cc);

      log.info("setProperties made it past persist of ETags");
      // set properties entity
      TableManager.deleteVersionedTable(entry, false, cc);
      log.info("setProperties cleaned up stale properties");

      return converter.toTableProperties(kvsEntities, tableId, schemaETag, propertiesETag);
    } finally {
      lock.release();
    }
  }
}
