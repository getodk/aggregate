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
import java.util.Collections;
import java.util.List;

import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions.DbColumnDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions.DbTableDefinitionsEntity;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class DbLogTable extends Relation {

  private DbLogTable(String namespace, String tableName, List<DataField> fields, CallingContext cc)
      throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
  }

  /**
   * NOTE: the PK of this table is the ROW_ETAG of the DbTable Row
   * who's state matches this log entry.
   */

  // RowID of the DbTable row corresponding to this log entry
  public static final DataField ROW_ID = new DataField("_ROW_ID", DataType.STRING, false)
      .setIndexable(IndexType.HASH);
  // Global sequence value that monotonically increased -- for change ordering
  public static final DataField SEQUENCE_VALUE = new DataField("_SEQUENCE_VALUE", DataType.STRING,
      false).setIndexable(IndexType.ORDERED);

  // ETag of the DbTable Row's state prior to this one (may be null if the row did not exist)
  public static final DataField PREVIOUS_ROW_ETAG = new DataField("_PREVIOUS_ROW_ETAG", DataType.STRING, true);
  // ETag in the TableEntry that tracks this modification (for eventual bulk updates)
  public static final DataField DATA_ETAG_AT_MODIFICATION = new DataField(
      "_DATA_ETAG_AT_MODIFICATION", DataType.STRING, false).setIndexable(IndexType.HASH);
  // UriUser that originally created the record
  public static final DataField CREATE_USER = new DataField("_CREATE_USER", DataType.STRING, true);
  // UriUser that last modified the record
  public static final DataField LAST_UPDATE_USER = new DataField("_LAST_UPDATE_USER",
      DataType.STRING, true);
  // Whether or not this DbTable Row is deleted.
  public static final DataField DELETED = new DataField("_DELETED", DataType.BOOLEAN, false);

  // The FormId of the form that was in use when this record was last saved.
  public static final DataField FORM_ID = new DataField(TableConstants.FORM_ID.toUpperCase(),
      DataType.STRING, true);
  // The locale that was active when this record was last saved.
  public static final DataField LOCALE = new DataField(TableConstants.LOCALE.toUpperCase(),
      DataType.STRING, true);
  // nanoseconds at the time the form was saved (on client).
  public static final DataField SAVEPOINT_TIMESTAMP = new DataField(
      TableConstants.SAVEPOINT_TIMESTAMP.toUpperCase(), DataType.STRING, true);
  // the creator of this row, as reported by the device (may be a remote SMS user)
  public static final DataField SAVEPOINT_CREATOR = new DataField(
      TableConstants.SAVEPOINT_CREATOR.toUpperCase(), DataType.STRING, true);

  // Access control filters accessible only on server (these may be useless)
  public static final DataField FILTER_TYPE = new DataField("_FILTER_TYPE", DataType.STRING, true);
  public static final DataField FILTER_VALUE = new DataField("_FILTER_VALUE", DataType.STRING, true)
      .setIndexable(IndexType.HASH);

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(ROW_ID);
    dataFields.add(SEQUENCE_VALUE);

    // metadata held only up at server
    dataFields.add(PREVIOUS_ROW_ETAG);
    dataFields.add(DATA_ETAG_AT_MODIFICATION);
    dataFields.add(CREATE_USER);
    dataFields.add(LAST_UPDATE_USER);
    dataFields.add(DELETED);

    // common metadata transmitted between server and device
    dataFields.add(FORM_ID);
    dataFields.add(LOCALE);
    dataFields.add(SAVEPOINT_TIMESTAMP);
    dataFields.add(SAVEPOINT_CREATOR);

    // Access control filters accessible only on server (these may be useless)
    dataFields.add(FILTER_TYPE);
    dataFields.add(FILTER_VALUE);
  }

  private static final EntityConverter converter = new EntityConverter();

  public static final String getDbLogTableName(String dataTableName) {
    return dataTableName + "_LOG";
  }

  public static DbLogTable getRelation(DbTableDefinitionsEntity entity, List<DbColumnDefinitionsEntity> entities, CallingContext cc)
      throws ODKDatastoreException {
    List<DataField> fields = converter.toFields(entities);
    fields.addAll(getStaticFields());
    return getRelation(getDbLogTableName(entity.getDbTableName()), fields, cc);
  }

  private static synchronized DbLogTable getRelation(String tableName, List<DataField> fields,
      CallingContext cc) throws ODKDatastoreException {
    DbLogTable relation = new DbLogTable(RUtil.NAMESPACE, tableName, fields, cc);
    return relation;
  }

  private static List<DataField> getStaticFields() {
    return Collections.unmodifiableList(dataFields);
  }

}
