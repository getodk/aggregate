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
 * Tracks the ETags associated with a given TableId.
 * The TableId is the PK for this table.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class DbTableEntry extends Relation {

  private DbTableEntry(String namespace, String tableName, List<DataField> fields, CallingContext cc)
      throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
  }

  private static final String RELATION_NAME = "TABLE_ENTRY2";

  private static final DataField DATA_ETAG = new DataField("DATA_ETAG", DataType.STRING, true);
  private static final DataField PROPERTIES_ETAG = new DataField("PROPERTIES_ETAG", DataType.STRING, true);
  private static final DataField SCHEMA_ETAG = new DataField("SCHEMA_ETAG", DataType.STRING, false);
  private static final DataField APRIORI_DATA_SEQUENCE_VALUE = new DataField(
      "APRIORI_DATA_SEQUENCE_VALUE", DataType.STRING, false);

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(DATA_ETAG);
    dataFields.add(PROPERTIES_ETAG);
    dataFields.add(SCHEMA_ETAG);
    dataFields.add(APRIORI_DATA_SEQUENCE_VALUE);
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

    // Primary Key
    public String getId() {
      return e.getId();
    }

    // Accessors

    public String getDataETag() {
      return e.getString(DATA_ETAG);
    }

    public void setDataETag(String value) {
      e.set(DATA_ETAG, value);
    }

    public String getPropertiesETag() {
      return e.getString(PROPERTIES_ETAG);
    }

    public void setPropertiesETag(String value) {
      e.set(PROPERTIES_ETAG, value);
    }

    public String getSchemaETag() {
      return e.getString(SCHEMA_ETAG);
    }

    public void setSchemaETag(String value) {
      e.set(SCHEMA_ETAG, value);
    }

    public String getAprioriDataSequenceValue() {
      return e.getString(APRIORI_DATA_SEQUENCE_VALUE);
    }

    public void setAprioriDataSequenceValue(String value) {
      e.set(APRIORI_DATA_SEQUENCE_VALUE, value);
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
