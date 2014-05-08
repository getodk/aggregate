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

import org.apache.commons.lang3.Validate;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions.DbColumnDefinitionsEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableDefinitions.DbTableDefinitionsEntity;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Query;
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

/**
 * Represents the schema for a user-defined (data, security, shortcut) table in
 * the database.
 *
 * @author dylan price
 * @author sudar.sam@gmail.com
 *
 */
public class DbTable extends Relation {

  private DbTable(String namespace, String tableName, List<DataField> fields, CallingContext cc)
      throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
  }

  /**
   * NOTE: the PK of this table is the ROW_ID of the DbLogTable entry
   * who's state matches this row.
   */

  public static final DataField ROW_ETAG = new DataField("_ROW_ETAG", DataType.STRING, false);
  /**
   * This should hold the TableEntry's data ETag at the time the row was modified/created.
   */
  public static final DataField DATA_ETAG_AT_MODIFICATION = new DataField(
      "_DATA_ETAG_AT_MODIFICATION", DataType.STRING, false);
  public static final DataField CREATE_USER = new DataField("_CREATE_USER", DataType.STRING, true);
  public static final DataField LAST_UPDATE_USER = new DataField("_LAST_UPDATE_USER",
      DataType.STRING, true);
  public static final DataField DELETED = new DataField("_DELETED", DataType.BOOLEAN, false);

  // limited to 10 characters
  public static final DataField FILTER_TYPE = new DataField(TableConstants.FILTER_TYPE.toUpperCase(),
      DataType.STRING, true, 10L);
  // limited to 50 characters
  public static final DataField FILTER_VALUE = new DataField(TableConstants.FILTER_VALUE.toUpperCase(),
      DataType.STRING, true, 50L).setIndexable(IndexType.HASH);
  // The FormId of the form that was in use when this record was last saved.
  // limited to 50 characters
  public static final DataField FORM_ID = new DataField(TableConstants.FORM_ID.toUpperCase(),
      DataType.STRING, true, 50L);
  // The locale that was active when this record was last saved.
  // limited to 10 characters
  public static final DataField LOCALE = new DataField(TableConstants.LOCALE.toUpperCase(),
      DataType.STRING, true, 10L);
  // limited to 10 characters
  public static final DataField SAVEPOINT_TYPE = new DataField(TableConstants.SAVEPOINT_TYPE.toUpperCase(),
      DataType.STRING, true, 10L);
  // nanoseconds at the time the form was saved (on client).
  // limited to 40 characters
  public static final DataField SAVEPOINT_TIMESTAMP = new DataField(
      TableConstants.SAVEPOINT_TIMESTAMP.toUpperCase(), DataType.STRING, true, 40L);
  // the creator of this row, as reported by the device (may be a remote SMS user)
  public static final DataField SAVEPOINT_CREATOR = new DataField(
      TableConstants.SAVEPOINT_CREATOR.toUpperCase(), DataType.STRING, true);

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();

    // metadata held only up at server
    dataFields.add(ROW_ETAG);
    dataFields.add(DATA_ETAG_AT_MODIFICATION);
    dataFields.add(CREATE_USER);
    dataFields.add(LAST_UPDATE_USER);
    dataFields.add(DELETED);

    // common metadata transmitted between server and device
    dataFields.add(FILTER_TYPE);
    dataFields.add(FILTER_VALUE);
    dataFields.add(FORM_ID);
    dataFields.add(LOCALE);
    dataFields.add(SAVEPOINT_TYPE);
    dataFields.add(SAVEPOINT_TIMESTAMP);
    dataFields.add(SAVEPOINT_CREATOR);
  }

  private static final EntityConverter converter = new EntityConverter();

  public static DbTable getRelation(DbTableDefinitionsEntity entity, List<DbColumnDefinitionsEntity> entities, CallingContext cc)
      throws ODKDatastoreException {
    List<DataField> fields = converter.toFields(entities);
    fields.addAll(getStaticFields());
    return getRelation(entity.getDbTableName(), fields, cc);
  }

  private static synchronized DbTable getRelation(String dbTableName, List<DataField> fields,
      CallingContext cc) throws ODKDatastoreException {
    DbTable relation = new DbTable(RUtil.NAMESPACE, dbTableName, fields, cc);
    return relation;
  }

  /**
   * This should only be called sparingly.
   *
   * @return
   */
  public static List<DataField> getStaticFields() {
    return Collections.unmodifiableList(dataFields);
  }

  /**
   * Retrieve a list of {@link DbTable} row entities.
   *
   * @param table
   *          the {@link DbTable} relation.
   * @param rowIds
   *          the ids of the rows to get.
   * @param cc
   * @return the row entities
   * @throws ODKEntityNotFoundException
   *           if one of the rows does not exist
   * @throws ODKDatastoreException
   */
  public static List<Entity> query(DbTable table, List<String> rowIds, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    Validate.notNull(table);
    Validate.noNullElements(rowIds);
    Validate.notNull(cc);

    Query query = table.query("DbTable.query", cc);
    query.include(CommonFieldsBase.URI_COLUMN_NAME, rowIds);
    List<Entity> entities = query.execute();
    return entities;
  }

}