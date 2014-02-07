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

package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.rest.KeyValueStoreConstants;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Query;
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * The relation in the datastore that maps to the Server KeyValueStore on the
 * phone.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class DbKeyValueStore extends Relation {

  private DbKeyValueStore(String namespace, String tableName, List<DataField> fields,
      CallingContext cc) throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
  }

  // The name of the table/relation in the datastore.
  private static final String RELATION_NAME = "KEY_VALUE_STORE2";

  // Column names.
  private static final DataField TABLE_ID = new DataField("TABLE_ID", DataType.STRING, false);
  private static final DataField PROPERTIES_ETAG = new DataField("PROPERTIES_ETAG",
      DataType.STRING, false);
  private static final DataField PARTITION = new DataField("PARTITION", DataType.STRING, false);
  private static final DataField ASPECT = new DataField("ASPECT", DataType.STRING, false);
  private static final DataField KEY = new DataField("KEY", DataType.STRING, false);
  private static final DataField TYPE = new DataField("TYPE", DataType.STRING, false);
  // NOTE: 19200L is due to a limitation in MySQL
  private static final DataField VALUE = new DataField("VALUE", DataType.STRING, true, 19200L);

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(TABLE_ID);
    dataFields.add(PROPERTIES_ETAG);
    dataFields.add(PARTITION);
    dataFields.add(ASPECT);
    dataFields.add(KEY);
    dataFields.add(TYPE);
    dataFields.add(VALUE);
  }

  public static class DbKeyValueStoreEntity {
    Entity e;

    DbKeyValueStoreEntity(Entity e) {
      this.e = e;
    }

    // Primary Key
    public String getId() {
      return e.getId();
    }

    public void put(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
      e.put(cc);
    }

    public void delete(CallingContext cc) throws ODKDatastoreException {
      e.delete(cc);
    }

    // Accessors

    public String getTableId() {
      return e.getString(TABLE_ID);
    }

    public void setTableId(String value) {
      e.set(TABLE_ID, value);
    }

    public String getPropertiesETag() {
      return e.getString(PROPERTIES_ETAG);
    }

    public void setPropertiesETag(String value) {
      e.set(PROPERTIES_ETAG, value);
    }

    public String getPartition() {
      return e.getString(PARTITION);
    }

    public void setPartition(String value) {
      e.set(PARTITION, value);
    }

    public String getAspect() {
      return e.getString(ASPECT);
    }

    public void setAspect(String value) {
      e.set(ASPECT, value);
    }

    public String getKey() {
      return e.getString(KEY);
    }

    public void setKey(String value) {
      e.set(KEY, value);
    }

    public String getType() {
      return e.getString(TYPE);
    }

    public void setType(String value) {
      e.set(TYPE, value);
    }

    public String getValue() {
      return e.getString(VALUE);
    }

    public void setValue(String value) {
      e.set(VALUE, value);
    }

  }

  private static DbKeyValueStore relation = null;

  public static synchronized final DbKeyValueStore getRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      relation = new DbKeyValueStore(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
    }
    return relation;
  }

  /**
   * Create a new row in this relation. The row is not yet persisted.
   *
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static DbKeyValueStoreEntity createNewEntity(CallingContext cc)
      throws ODKDatastoreException {
    return new DbKeyValueStoreEntity(getRelation(cc).newEntity(cc));
  }

  /**
   * Delete all the key value store entities for the given table.
   * <p>
   * NB: No logging is performed! Currently no notion of transactions, so if
   * this method is called and a subsequent add of new entities fails, there will
   * be no recourse to restore the state.
   *
   * @param tableId
   * @param propertiesETag
   * @param cc
   * @throws ODKDatastoreException
   */
  public static void clearAllEntries(String tableId, String propertiesETag, CallingContext cc)
      throws ODKDatastoreException {
    List<DbKeyValueStoreEntity> kvsEntities = getKVSEntries(tableId, propertiesETag, cc);
    for (DbKeyValueStoreEntity entity : kvsEntities) {
      entity.delete(cc);
    }
  }

  /**
   * Get a List of Entity objects representing all the entries in the key value
   * store for the given table.
   *
   * @param tableId
   * @param propertiesETag
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static List<DbKeyValueStoreEntity> getKVSEntries(String tableId, String propertiesETag,
      CallingContext cc) throws ODKDatastoreException {

    Query query = getRelation(cc).query("DbKeyValueStore.getKVSEntries", cc);
    query.addFilter(TABLE_ID, FilterOperation.EQUAL, tableId);
    query.addFilter(PROPERTIES_ETAG, FilterOperation.EQUAL, propertiesETag);

    List<Entity> list = query.execute();
    List<DbKeyValueStoreEntity> results = new ArrayList<DbKeyValueStoreEntity>();
    for (Entity e : list) {
      results.add(new DbKeyValueStoreEntity(e));
    }
    return results;
  }

  /**
   * Get the displayName of a given tableId
   *
   * @param tableId
   * @param propertiesETag
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static String getDisplayName(String tableId, String propertiesETag,
      CallingContext cc) throws ODKDatastoreException {

    Query query = getRelation(cc).query("DbKeyValueStore.getDisplayName", cc);
    query.addFilter(TABLE_ID, FilterOperation.EQUAL, tableId);
    query.addFilter(PROPERTIES_ETAG, FilterOperation.EQUAL, propertiesETag);
    query.addFilter(PARTITION, FilterOperation.EQUAL, KeyValueStoreConstants.PARTITION_TABLE);
    query.addFilter(ASPECT, FilterOperation.EQUAL, KeyValueStoreConstants.ASPECT_DEFAULT);
    query.addFilter(KEY, FilterOperation.EQUAL, KeyValueStoreConstants.TABLE_DISPLAY_NAME);

    List<Entity> list = query.execute();
    if ( list.size() != 1 ) {
      return "Ill-defined";
    } else {
      Entity e = list.get(0);
      return e.getString(VALUE);
    }
  }
}
