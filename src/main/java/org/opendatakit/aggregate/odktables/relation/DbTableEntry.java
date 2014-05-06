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

import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Query;
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * Tracks the ETags associated with a given TableId. The TableId is the PK for
 * this table.
 * <p>
 * Because the server avoids using database transactions, to maintain state, we use this table
 * as a two-phase commit record and pending-clean-up record.  The sequence to changing the
 * schema, properties or data is the same:
 * <ol>
 * <li>Gain mutex</li>
 * <li>Read DbTableEntry record for tableId</li>
 * <li>If the pending ETag != null then we need to roll back a pending change that did not complete.</li>
 * <li>If the stale ETag != null then we need to remove the state corresponding to the stale ETag.</li>
 * <li>Otherwise, the database state is valid and we can proceed</li>
 * <li>Release mutex</li>
 * <ol>
 * <p>Schema changes can take minutes or hours to complete. Schema rollback or cleanup only need to be
 * initiated if the designated schema task is in the failed state (or is non-existent). That task performs
 * schema rollback and cleanup in the same way -- by dropping the table and deleting all records in the table
 * (in that order, as PostgreSQL and MySQL have efficient table dropping actions, while GAE does not).
 * If there is a pending schema change, the task responsible for that will either complete successfully,
 * and change the schema ETag to match the pending ETag (and clear the pending ETag), or enter the failed
 * state which would be detected upon the next sync request and kick off the rollback actions.</p>
 * <p>Properties roll back corresponds to deleting all properties entries with the pending ETag. Once
 * complete, the Pending properties ETag is cleared.</p>
 * <p>Properties stale cleanup corresponds to deleting all properties entries with the stale ETag. Once
 * complete, the Stale properties ETag is cleared.</p>
 * <p>Data roll back corresponds to finding all DbLogTable entries with the pending ETag and systematically
 * recovering the most recent delta prior to that ETag. The DbTable entry for that row is then restored to
 * that earlier delta, and the DbLogTable entry is removed. Once all DbLogTable entries have been removed,
 * the Pending Data ETag is set to null.</p>
 * <p>Data stale cleanup is not applicable.</p>
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class DbTableEntry extends Relation {

  private DbTableEntry(String namespace, String tableName, List<DataField> fields, CallingContext cc)
      throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
  }

  private static final String RELATION_NAME = "TABLE_ENTRY4";

  /**
   * changes to row values that are in-progress but not complete will be tagged
   * with the pending data ETag value
   */
  private static final DataField PENDING_DATA_ETAG = new DataField("PENDING_DATA_ETAG",
      DataType.STRING, true);
  /**
   * Upon completion of a change, the data ETag will be assigned the value of the pending data ETag
   * and the pending data ETag will be set to null.
   */
  private static final DataField DATA_ETAG = new DataField("DATA_ETAG", DataType.STRING, true);
  // there is no STALE_DATA_ETAG, as we maintain a history of all changes to the data values

  /**
   * When asynchronous schema changes become supported, the task uri is saved in this field.
   */
  private static final DataField URI_SCHEMA_TASK = new DataField("URI_SCHEMA_TASK", DataType.STRING, true);
  /**
   * If the pending schema ETag != null, then there is a schema change in progress.
   */
  private static final DataField PENDING_SCHEMA_ETAG = new DataField("PENDING_SCHEMA_ETAG",
      DataType.STRING, true);
  /**
   * Upon completion, the stale schema ETag is given the value of the schema ETag, the schema ETag
   * is given the value of the pending schema ETag, and the pending schema ETag is set to null.
   * This signals that the schema has changed, but that the old schema needs to be cleaned up.
   */
  private static final DataField SCHEMA_ETAG = new DataField("SCHEMA_ETAG", DataType.STRING, true);
  /**
   * If the stale schema ETag != null, then all of the column definitions for it should be
   * removed, the table dropped, and the table definition record deleted. Once that is done, the
   * stale schema ETag should be set to null.
   */
  private static final DataField STALE_SCHEMA_ETAG = new DataField("STALE_SCHEMA_ETAG",
      DataType.STRING, true);

  /**
   * Since ETags are uuids, we need a way to sort and order them for row-level change history. An
   * internal sequence value in the DbLogTable is used for this. The sequence value here is a
   * sequence value that is guaranteed to compare less than all other sequence values (enabling
   * a client device to access all records in the table).
   */
  private static final DataField APRIORI_DATA_SEQUENCE_VALUE = new DataField(
      "APRIORI_DATA_SEQUENCE_VALUE", DataType.STRING, false);

  /**
   * The target number of rows returned at one time by queries on this table.
   * The actual number is moderated by the granularity of the data ETag value.
   * If batch modifications share one data ETag, we can only break our batch
   * when the data ETag changes.
   */
  private static final DataField TARGET_ROW_BATCH_SIZE = new DataField("TARGET_ROW_BATCH_SIZE",
      DataType.INTEGER, true);

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(PENDING_DATA_ETAG);
    dataFields.add(DATA_ETAG);
    dataFields.add(URI_SCHEMA_TASK);
    dataFields.add(PENDING_SCHEMA_ETAG);
    dataFields.add(SCHEMA_ETAG);
    dataFields.add(STALE_SCHEMA_ETAG);
    dataFields.add(APRIORI_DATA_SEQUENCE_VALUE);
    dataFields.add(TARGET_ROW_BATCH_SIZE);
  }

  public static class DbTableEntryEntity {
    Entity e;

    DbTableEntryEntity(Entity e) {
      this.e = e;
    }

    public void put(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
      e.put(cc);
    }

    public void delete(CallingContext cc) throws ODKDatastoreException {
      e.delete(cc);
    }

    // Primary Key -- the tableId
    public String getId() {
      return e.getId();
    }

    // Accessors

    public String getPendingDataETag() {
      return e.getString(PENDING_DATA_ETAG);
    }

    public void setPendingDataETag(String value) {
      e.set(PENDING_DATA_ETAG, value);
    }

    public String getDataETag() {
      return e.getString(DATA_ETAG);
    }

    public void setDataETag(String value) {
      e.set(DATA_ETAG, value);
    }

    public String getUriSchemaTask() {
      return e.getString(URI_SCHEMA_TASK);
    }

    public void setUriSchemaTask(String value) {
      e.set(URI_SCHEMA_TASK, value);
    }

    public String getPendingSchemaETag() {
      return e.getString(PENDING_SCHEMA_ETAG);
    }

    public void setPendingSchemaETag(String value) {
      e.set(PENDING_SCHEMA_ETAG, value);
    }

    public String getSchemaETag() {
      return e.getString(SCHEMA_ETAG);
    }

    public void setSchemaETag(String value) {
      e.set(SCHEMA_ETAG, value);
    }

    public String getStaleSchemaETag() {
      return e.getString(STALE_SCHEMA_ETAG);
    }

    public void setStaleSchemaETag(String value) {
      e.set(STALE_SCHEMA_ETAG, value);
    }

    public String getAprioriDataSequenceValue() {
      return e.getString(APRIORI_DATA_SEQUENCE_VALUE);
    }

    public void setAprioriDataSequenceValue(String value) {
      e.set(APRIORI_DATA_SEQUENCE_VALUE, value);
    }

    public Long getTargetRowBatchSize() {
      return e.getLong(TARGET_ROW_BATCH_SIZE);
    }

    public void setTargetRowBatchSize(Long value) {
      e.set(TARGET_ROW_BATCH_SIZE, value);
    }
  }

  private static DbTableEntry relation = null;

  public static synchronized final DbTableEntry getRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      relation = new DbTableEntry(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
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
  public static DbTableEntryEntity createNewEntity(String tableId, CallingContext cc)
      throws ODKDatastoreException {
    return new DbTableEntryEntity(getRelation(cc).newEntity(tableId, cc));
  }

  public static DbTableEntryEntity getTableIdEntry(String tableId, CallingContext cc)
      throws ODKOverQuotaException, ODKEntityNotFoundException, ODKDatastoreException {

    return new DbTableEntryEntity(getRelation(cc).getEntity(tableId, cc));
  }

  public static List<DbTableEntryEntity> query(CallingContext cc) throws ODKDatastoreException {
    Query query = getRelation(cc).query("DbTableEntry.query", cc);

    List<Entity> list = query.execute();
    List<DbTableEntryEntity> results = new ArrayList<DbTableEntryEntity>();
    for (Entity e : list) {
      results.add(new DbTableEntryEntity(e));
    }
    return results;
  }

}
