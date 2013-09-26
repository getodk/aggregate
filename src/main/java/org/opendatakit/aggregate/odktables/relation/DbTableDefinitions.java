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

import org.jboss.resteasy.logging.Logger;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Query;
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * Represents the TableDefinitions table in the datastore. Analogous to the
 * eponymous Table in the ODK Tables data model.
 * <p>
 * NB: This is NOT directly analogous to the {@link TableDefinition} object,
 * which represents the XML document defining a table by which ODKTables talks
 * to the server.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class DbTableDefinitions extends Relation {

  private DbTableDefinitions(String namespace, String tableName, List<DataField> fields,
      CallingContext cc) throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
  }

  // The name of the table/relation in the datastore.
  private static final String RELATION_NAME = "TABLE_DEFINITIONS";

  // Column names. Based on the ODK Tables Schema google doc for the
  // non client-local columns.
  public static final DataField TABLE_ID = new DataField("TABLE_ID", DataType.STRING, false)
      .setIndexable(IndexType.HASH);
  public static final DataField PROPERTIES_ETAG = new DataField("PROPERTIES_ETAG", DataType.STRING,
      false);
  public static final DataField DB_TABLE_NAME = new DataField("DB_TABLE_NAME", DataType.STRING,
      false);
  public static final DataField TYPE = new DataField("TYPE", DataType.STRING, false);
  public static final DataField TABLE_ID_ACCESS_CONTROLS = new DataField(
      "TABLE_ID_ACCESS_CONTROLS", DataType.STRING, true);

  private static final List<DataField> dataFields;
  static {
    dataFields = new ArrayList<DataField>();
    dataFields.add(TABLE_ID);
    dataFields.add(PROPERTIES_ETAG);
    dataFields.add(DB_TABLE_NAME);
    dataFields.add(TYPE);
    dataFields.add(TABLE_ID_ACCESS_CONTROLS);
  }

  public static class DbTableDefinitionsEntity {
    Entity e;

    DbTableDefinitionsEntity(Entity e) {
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

    public String getDbTableName() {
      return e.getString(DB_TABLE_NAME);
    }

    public void setDbTableName(String value) {
      e.set(DB_TABLE_NAME, value);
    }

    public String getType() {
      return e.getString(TYPE);
    }

    public void setType(String value) {
      e.set(TYPE, value);
    }

    public String getTableIdAccessControls() {
      return e.getString(TABLE_ID_ACCESS_CONTROLS);
    }

    public void setTableIdAccessControls(String value) {
      e.set(TABLE_ID_ACCESS_CONTROLS, value);
    }
  }

  private static DbTableDefinitions relation = null;

  public static synchronized final DbTableDefinitions getRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      relation = new DbTableDefinitions(RUtil.NAMESPACE, RELATION_NAME, dataFields, cc);
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
  public static DbTableDefinitionsEntity createNewEntity(CallingContext cc)
      throws ODKDatastoreException {
    return new DbTableDefinitionsEntity(getRelation(cc).newEntity(cc));
  }

  public static DbTableDefinitionsEntity getDefinition(String tableId, String propertiesEtag,
      CallingContext cc) throws ODKDatastoreException {
    Query query = getRelation(cc).query("DbTableDefinitions.getDefinition", cc);
    query.addFilter(TABLE_ID, FilterOperation.EQUAL, tableId);
    query.addFilter(PROPERTIES_ETAG, FilterOperation.EQUAL, propertiesEtag);

    List<Entity> list = query.execute();
    if (list.isEmpty()) {
      return null;
    }

    if (list.size() != 1) {
      Logger.getLogger(DbTableDefinitions.class).warn(
          "Multiple DbTableDefinitions records for table id " + tableId + " and propertiesEtag "
              + propertiesEtag);
    }

    Entity e = list.get(0);
    return new DbTableDefinitionsEntity(e);
  }
}
